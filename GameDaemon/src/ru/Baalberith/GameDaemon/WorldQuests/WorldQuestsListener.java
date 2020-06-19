package ru.Baalberith.GameDaemon.WorldQuests;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import ru.Baalberith.GameDaemon.GD;
import ru.Baalberith.GameDaemon.GDSender;
import ru.Baalberith.GameDaemon.Clans.Dungeons.DungeonEngine;
import ru.Baalberith.GameDaemon.WorldQuests.Quests.ArtisanQuest;
import ru.Baalberith.GameDaemon.WorldQuests.Quests.MercenaryQuest;
import ru.Baalberith.GameDaemon.WorldQuests.Quests.MinerQuest;
import ru.Baalberith.GameDaemon.WorldQuests.Quests.SupplierQuest;

public class WorldQuestsListener implements Listener {
	
	public WorldQuestsListener() {
		Bukkit.getPluginManager().registerEvents(this, GD.inst);
	}
	
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent e) {
		if (!WorldQuestsManager.hasActiveQuest()) return;
		
		Block b = e.getBlock();
		for (WorldQuest quest : WorldQuestsManager.activeQuestTask.getActiveQuests()) {
			switch (quest.type) {
			case miner:
				MinerQuest mq = (MinerQuest) quest;
				if (!mq.isRightBlock(b.getState().getData().toItemStack())) continue;
				if (!mq.containsInArea(b.getLocation())) continue;
				mq.increaseScore(e.getPlayer().getName(), 1);
				break;
			default: break;
			}
		}
	}
	
	@EventHandler
	public void onEntityKill(EntityDeathEvent e) {
		LivingEntity target = e.getEntity();
		if (target instanceof Player) return;

		if (!WorldQuestsManager.hasActiveQuest()) return;
		
		Player killer = e.getEntity().getKiller();
		if (killer == null) return;
		
		for (WorldQuest quest : WorldQuestsManager.activeQuestTask.getActiveQuests()) {
			switch (quest.type) {
			case mercenary:
				MercenaryQuest mq = (MercenaryQuest) quest;
				if (!mq.containsInArea(killer.getLocation())) continue;
				mq.increaseScore(killer.getName(), 1);
				break;
			default: break;
			}
		}
	}
	
	@EventHandler
	public void onPlayerDropCargo(PlayerInteractEvent e) {
		Player p = e.getPlayer();
		
		if (p.getItemInHand() == null) return;
		
		ItemStack hand = p.getItemInHand();
		
		for (WorldQuest quest : WorldQuestsManager.activeQuestTask.getActiveQuests()) {
			switch (quest.type) {
			case supplier:
				SupplierQuest sq = (SupplierQuest) quest;
				if (!sq.containsInArea(p.getLocation())) continue;
				if (!sq.checkItemInHand(hand)) {
					Message.supplier_noLocation.send(p);
					continue;
				}
				sq.increaseStatus(p, hand);
				break;
			default: break;
			}
		}
	}
	
	public static void onCraftItem(String player, String handicraftName, int stage) {

		for (WorldQuest quest : WorldQuestsManager.activeQuestTask.getActiveQuests()) {
			switch (quest.type) {
			case artisan:
				ArtisanQuest aq = (ArtisanQuest) quest;
				if (!aq.checkFilter(handicraftName, stage)) continue;
				aq.increaseScore(player, 1);
				break;
			default: break;
			}
		}
		
	}
	
	@EventHandler
	public void onSetCoordsByBlockClick(PlayerInteractEvent e) {
		GDSender p = GD.getGDSender(e.getPlayer());
		if (!DungeonEngine.waitingBlockClick.containsKey(p)) return;
		if (e.getClickedBlock() == null) return;
		DungeonEngine.coordsClick_(p, e.getClickedBlock().getLocation(), DungeonEngine.waitingBlockClick.get(p));
	}
}
