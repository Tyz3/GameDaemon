package ru.Baalberith.GameDaemon.LightLevelingSystem;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitTask;

import ru.Baalberith.GameDaemon.ConfigsDaemon;
import ru.Baalberith.GameDaemon.GD;
import ru.Baalberith.GameDaemon.GDPlayer;
import ru.Baalberith.GameDaemon.Utils.CountingPattern;
import ru.Baalberith.GameDaemon.Utils.MathOperation;
import ru.Baalberith.GameDaemon.Utils.ThreadDaemon;

public class ExpBoosters implements Listener, Runnable {
	
	private static List<ExpBooster> boosters = new ArrayList<ExpBooster>();
	private static CountingPattern countingPattern;
	private BukkitTask task;
	
	public void reload() {
		try {
			if (task != null) ThreadDaemon.cancelTask(task);
			task = ThreadDaemon.asyncTimer(this, 0, 20*60);
			boosters.clear();
			
			countingPattern = new CountingPattern(ConfigsDaemon.mainConfig.get().getString("lightLevelingSystem.boosterExpiresNotify"));
			ConfigurationSection c = ConfigsDaemon.mainConfig.getConfigurationSection("lightLevelingSystem.boosters");
			
			Set<String> keys = c.getKeys(false);
			for (String k : keys) {
				Material type = Material.matchMaterial(c.getString(k+".type"));
				if (type == null) continue;
				short data = (short) c.getInt(k+".data", 0);
				double bonus = c.getDouble(k+".bonus", 0.0D);
				long durationMillis = c.getLong(k+".duration", 0L)*1000;
				
				boosters.add(new ExpBooster(type, data, bonus, durationMillis));
			}
			
			GD.log("[LiLS] Loaded "+boosters.size()+" experience boosters.");
		} catch (Exception e) {e.printStackTrace();}
	}
	
	@EventHandler
	public void onBoosterUse(PlayerInteractEvent e) {
		if (e.getItem() == null) return;
		
		GDPlayer p = GD.getGDPlayer(e.getPlayer());
		boosters.stream().forEach(b -> b.giveBoost(p, e.getItem()));
	}

	@Override
	public void run() {
		
		for (GDPlayer p : GD.online) {
			if (p.getExpBoostRemainingTime() < 0) {
				Message.booster_ended.replace("{xpReceived}", String.valueOf(p.getExpReceivedWithBooster())).send(p);
				p.setExpBoostRemainingTime(0);
				p.removeExpBoost();
			} else if (p.hasExpBoost() && countingPattern.contains(p.getExpBoostRemainingTime())) {
				Message.booster_notify
					.replace("{expBoost}", String.valueOf(p.getExpBoostBonus()))
					.replace("{time}", MathOperation.makeTimeToString(Message.booster_time.get(), p.getExpBoostRemainingTime()))
					.send(p);
			}
		}
	}
}
