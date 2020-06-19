package ru.Baalberith.GameDaemon.MuteDaemon;

import java.util.concurrent.CopyOnWriteArrayList;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import ru.Baalberith.GameDaemon.GD;
import ru.Baalberith.GameDaemon.GDPlayer;
import ru.Baalberith.GameDaemon.Utils.MathOperation;

public class MuteHandler implements Listener {

	private static CopyOnWriteArrayList<String> mutedPlayers = new CopyOnWriteArrayList<String>();
	
	public MuteHandler() {
		Bukkit.getPluginManager().registerEvents(this, GD.inst);
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onSendMessage(AsyncPlayerChatEvent e) {
		GDPlayer p = GD.getGDPlayer(e.getPlayer());
		if (!mutedPlayers.contains(p.getName())) return;
		Message.alert.replace("{time}", MathOperation.makeTimeToString(MuteEngine.timeFormat, p.getMuteTimeLeft())).send(p);
		e.setCancelled(true);
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onUseCommand(PlayerCommandPreprocessEvent e) {
		GDPlayer p = GD.getGDPlayer(e.getPlayer());
		if (!mutedPlayers.contains(p.getName())) return;
		for (String cmd : MuteEngine.blockedCmds) {
			if (!e.getMessage().startsWith(cmd)) continue;
			Message.alert.replace("{time}", MathOperation.makeTimeToString(MuteEngine.timeFormat, p.getMuteTimeLeft())).send(p);
			e.setCancelled(true);
		}
	}
	
	public static void addPlayerToMute(String name) {
		if (!mutedPlayers.contains(name)) mutedPlayers.add(name);
	}
	
	public static void removePlayerFromMute(String name) {
		mutedPlayers.remove(name);
	}
	
}
