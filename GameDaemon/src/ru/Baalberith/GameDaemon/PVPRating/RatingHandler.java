package ru.Baalberith.GameDaemon.PVPRating;

import java.util.List;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import ru.Baalberith.GameDaemon.ConfigsDaemon;
import ru.Baalberith.GameDaemon.GD;
import ru.Baalberith.GameDaemon.GDPlayer;
import ru.Baalberith.GameDaemon.Utils.ThreadDaemon;

public class RatingHandler implements Listener {
	
	private ConfigurationSection c;
	private List<String> worlds;
	
	public void reload() {
		c = ConfigsDaemon.mainConfig.getConfigurationSection("pvp");
		worlds = c.getStringList("worlds-enabled");
	}
	
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent e) {
		
		if (!worlds.contains(e.getEntity().getWorld().getName())) return;
		
		if (!(e.getEntity().getKiller() instanceof Player)) return;
		GDPlayer killer = GD.getGDPlayer(e.getEntity().getKiller());
		GDPlayer victim = GD.getGDPlayer(e.getEntity().getPlayer());
		
		if (killer == null || !(killer instanceof GDPlayer)) return;
		
		victim.addDeathsAmount(1);
		killer.addKillsAmount(1);
		
		ThreadDaemon.async(() -> RatingEngine.inst.process(killer, victim));
	}
}
