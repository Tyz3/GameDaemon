package ru.Baalberith.GameDaemon.ExperienceExchange;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import ru.Baalberith.GameDaemon.ConfigsDaemon;
import ru.Baalberith.GameDaemon.GD;
import ru.Baalberith.GameDaemon.GDPlayer;
import ru.Baalberith.GameDaemon.Utils.ThreadDaemon;

public class BottledExpEngine implements Listener {
	
	public BottledExpEngine() {
		Bukkit.getPluginManager().registerEvents(this, GD.inst);
	}
	private ItemStack[] converterBlocks;
	private List<ExpBottle> expBottles = new ArrayList<ExpBottle>();
	
	public void reload() {
		try {
			ConfigurationSection c = ConfigsDaemon.mainConfig.getConfigurationSection("expToBottle");
			Message.load(ConfigsDaemon.messagesConfig.getConfigurationSection("expToBottle"));
			
			// Инициализация блоков-конвертеров.
			List<String> blocks = c.getStringList("blocks");
			converterBlocks = new ItemStack[blocks.size()];
			for (int i = 0; i < converterBlocks.length; i++) {
				String[] args = blocks.get(i).split("\\:");
				Material m = Material.matchMaterial(args[0]);
				if (m == null) continue;
				short damage = 0;
				if (args.length == 2) damage = Short.parseShort(args[1]);
				converterBlocks[i] = new ItemStack(m, 1, damage);
			}
			
			// Инициализация бутылок под опыт.
			expBottles.clear();
			Set<String> keys = c.getConfigurationSection("items").getKeys(false);
			for (String k : keys) {
				String itemIn = k;
				String[] argsIn = itemIn.split("\\:");
				Material typeIn = Material.matchMaterial(argsIn[0]);
				if (typeIn == null) continue;
				short damageIn = 0;
				if (argsIn.length == 2) damageIn = Short.parseShort(argsIn[1]);
				
				ItemStack in = new ItemStack(typeIn, 1, damageIn);
				
				String itemOut = c.getString("items."+k+".output");
				String[] argsOut = itemOut.split("\\:");
				Material typeOut = Material.matchMaterial(argsOut[0]);
				if (typeOut == null) continue;
				short damageOut = 0;
				if (argsOut.length == 2) damageOut = Short.parseShort(argsOut[1]);
				
				ItemStack out = new ItemStack(typeOut, 1, damageOut);
				
				int exp = c.getInt("items."+k+".exp", 0);
				expBottles.add(new ExpBottle(in, out, exp));
			}

			GD.log("[ExpToBottle] Loaded "+converterBlocks.length+" converter blocks.");
			GD.log("[ExpToBottle] Loaded "+expBottles.size()+" bottles.");
		} catch (Exception e) { e.printStackTrace(); }
	}
	
	private boolean isConvertBlock(Block block) {
		for (int i = 0; i < converterBlocks.length; i++) {
			if (block.getType() == converterBlocks[i].getType() && block.getData() == converterBlocks[i].getDurability()) return true;
		}
		return false;
	}
	
	@EventHandler
	public void onPlayerRightClick(PlayerInteractEvent e) {

		if (e.getAction() == Action.LEFT_CLICK_BLOCK || e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.PHYSICAL || e.getAction() == Action.RIGHT_CLICK_AIR) return;
		
		Block block = e.getClickedBlock();
		
		if (!isConvertBlock(block)) return;
		
		ThreadDaemon.async(() -> process(e));
	}
	
	private void process(PlayerInteractEvent e) {
		GDPlayer p = GD.getGDPlayer(e.getPlayer());
		
		ItemStack hand = e.getItem();
		if (hand == null || hand.getType() == Material.AIR) return;
		ExpBottle bot = getExpBottleByItem(hand);
		if (bot == null) {
			Message.notEnoughBottles.send(p);
			return;
		}
		
		int botAmount = hand.getAmount();
		int totalExp = p.getTotalExperience();
		
		int maxBottles = totalExp/bot.exp;
		if (maxBottles == 0) {
			Message.notEnoughExp.send(p);
			return;
		}
		if (botAmount < maxBottles) {
			p.takeItem(hand, botAmount);
			p.takeExperience(botAmount*bot.exp);
			p.giveItem(bot.out, botAmount);
			Message.success
				.replace("{exp}", String.valueOf(botAmount*bot.exp))
				.replace("{bottles}", String.valueOf(botAmount))
				.send(p);
		} else {
			p.takeItem(hand, maxBottles);
			p.takeExperience(maxBottles*bot.exp);
			p.giveItem(bot.out, maxBottles);
			Message.success
				.replace("{exp}", String.valueOf(maxBottles*bot.exp))
				.replace("{bottles}", String.valueOf(maxBottles))
				.send(p);
		}
		p.p.updateInventory();
	}
	
	private ExpBottle getExpBottleByItem(ItemStack hand) {
		for (ExpBottle bot : expBottles) {
			if (bot.in.getType() == hand.getType() && bot.in.getDurability() == hand.getDurability())
				return bot;
		}
		return null;
	}
	
}
