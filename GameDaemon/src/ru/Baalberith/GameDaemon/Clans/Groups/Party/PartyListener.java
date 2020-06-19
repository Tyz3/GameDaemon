package ru.Baalberith.GameDaemon.Clans.Groups.Party;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerLevelChangeEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.scoreboard.DisplaySlot;

import ru.Baalberith.GameDaemon.GD;
import ru.Baalberith.GameDaemon.GDPlayer;
import ru.Baalberith.GameDaemon.Extra.TeamDaemon.Objective;
import ru.Baalberith.GameDaemon.Extra.TeamDaemon.Trigger;
import ru.Baalberith.GameDaemon.Utils.ThreadDaemon;

public class PartyListener implements Listener {
	
	public static void onJoin(GDPlayer p) {
		ThreadDaemon.async(() -> {
			updateHealthScoreboardBelowName();
			updateLevelScoreboardInTab();
		});
		Party party = Party.getByPlayer(p.getName());
		if (party == null || p.inDungeon()) return;
		p.party = party;
		// TODO Проверить, зачем здесь два создания потока
		ThreadDaemon.async(() -> {
			party.updateSidebar();
			party.updateTeam();
			p.getTeamDaemon().getScoreboard().send(p.p);
		});
	}
	
	public static void onQuit(GDPlayer p) {
		Party party = Party.getByPlayer(p.getName());
		if (party == null) return;
		p.party = null;
		//
		ThreadDaemon.async(() -> party.updateSidebar());
	}
	
	@EventHandler
	public void onHealthRegain(EntityRegainHealthEvent e) {
		if (!(e.getEntity() instanceof Player)) return;
		GDPlayer p = GD.getGDPlayer((Player) e.getEntity());
		if (p.party == null || p.inDungeon()) return;
		ThreadDaemon.async(() -> p.party.updateSidebar());
	}
	
	@EventHandler
	public void onGetDamage(EntityDamageEvent e) {
		if (!(e.getEntity() instanceof Player)) return;
		GDPlayer p = GD.getGDPlayer((Player) e.getEntity());
		if (p.party == null || p.inDungeon()) return;
		ThreadDaemon.async(() -> p.party.updateSidebar());
	}
	
	@EventHandler
	public void onLevelChange(PlayerLevelChangeEvent e) {
		updateLevelScoreboardInTab();
	}
	
	@EventHandler
	public void onRespawn(PlayerRespawnEvent e) {
		GDPlayer p = GD.getGDPlayer(e.getPlayer());
		updateLevelScoreboardInTab();
		if (p.party == null) return;
		ThreadDaemon.async(() -> p.party.updateSidebar());
	}
	
	private static void updateHealthScoreboardBelowName() {
		for (int i = 0; i < GD.online.size(); i++) {
			for (int j = 0; j < GD.online.size(); j++) {
				Objective o = GD.online.get(i).getTeamDaemon().getOrRegisterObjective(DisplaySlot.BELOW_NAME, PartyEngine.sidebarHealthDisplay, Trigger.health);
				GDPlayer p = GD.online.get(j);
				o.setScore(p.getName(), (int) p.getHealth());
			}
		}
	}
	
	private static void updateLevelScoreboardInTab() {
		for (int i = 0; i < GD.online.size(); i++) {
			for (int j = 0; j < GD.online.size(); j++) {
				Objective o = GD.online.get(i).getTeamDaemon().getOrRegisterObjective(DisplaySlot.PLAYER_LIST, "LEVEL", Trigger.level);
				GDPlayer p = GD.online.get(j);
				o.setScore(p.getName(), p.getLevel());
			}
		}
	}
	
	@EventHandler
	public void onAsyncChat(AsyncPlayerChatEvent e) {
		GDPlayer p = GD.getGDPlayer(e.getPlayer());
		if (!p.partyChat()) return;
		
		if (p.party == null) {
			p.setPartyChat(false);
			return;
		}
		
		e.setCancelled(true);
		Message msg = p.party.getOwner().equals(p.getName()) ? Message.chatOwnerFormat : Message.chatMemberFormat;
		String m = msg.replace("{party}", p.party.getName()).replace("{player}", p.getName()).replace("{message}", e.getMessage()).get();
		p.party.sendMessage(m);
		PartyEngine.spySenders.forEach(ss -> ss.sendMessage("§c[SPY] ".concat(m)));
	}
}
