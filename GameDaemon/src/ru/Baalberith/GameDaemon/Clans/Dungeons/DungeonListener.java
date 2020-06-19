package ru.Baalberith.GameDaemon.Clans.Dungeons;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import ru.Baalberith.GameDaemon.GD;
import ru.Baalberith.GameDaemon.GDPlayer;
import ru.Baalberith.GameDaemon.GDSender;
import ru.Baalberith.GameDaemon.Clans.Dungeons.Game.Session;
import ru.Baalberith.GameDaemon.Clans.Dungeons.Game.Sessions;

public class DungeonListener implements Listener {
	
	public DungeonListener() {
		Bukkit.getPluginManager().registerEvents(this, GD.inst);
	}
	
	public static void onQuit(GDPlayer p) {
		if (Sessions.sessions.isEmpty()) return;
		String name = p.getName();
		Session s = Sessions.getSessionWithPlayer(name);
		if (s != null) s.tempDeleteGDPlayer(p);
	}
	
	public static void onJoin(GDPlayer p) {
		if (Sessions.sessions.isEmpty()) return;
		String name = p.getName();
		Session s = Sessions.getSessionWithPlayer(name);
		if (s != null) s.restorePlayer(p);
	}
	
	@EventHandler
	public void onSetCoordsByBlockClick(PlayerInteractEvent e) {
		GDSender p = GD.getGDSender(e.getPlayer());
		if (!DungeonEngine.waitingBlockClick.containsKey(p)) return;
		if (e.getClickedBlock() == null) return;
		DungeonEngine.coordsClick_(p, e.getClickedBlock().getLocation(), DungeonEngine.waitingBlockClick.get(p));
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onDeath(PlayerDeathEvent e) {
		if (DungeonEngine.deathInDungeon(e)) {
			e.setKeepInventory(true);
			e.setKeepLevel(true);
			e.setDroppedExp(0);
		}
	}
	
	@EventHandler
	public void onClick(PlayerInteractEvent e) {
		if (e.getAction() == Action.LEFT_CLICK_BLOCK || e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.PHYSICAL) return;
		
		if (e.getClickedBlock() == null) return;
		Location clickBlock = e.getClickedBlock().getLocation();
		GDPlayer p = GD.getGDPlayer(e.getPlayer());
		
		// Добавить оптимизацию на клики, сделать последовательную активацию
		DungeonEngine.dungeonGateProcess(clickBlock, p);
		DungeonEngine.dungeonWaybackProcess(clickBlock, p);
		
	}
}	
