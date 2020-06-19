package ru.Baalberith.GameDaemon.CargoDelivery;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import ru.Baalberith.GameDaemon.GD;
import ru.Baalberith.GameDaemon.Utils.ThreadDaemon;
import ru.Baalberith.GameDaemon.GDPlayer;

public class Task {
	
	private BukkitTask task;
	
	public void reload() {
		if (task != null)
			Bukkit.getScheduler().cancelTask(task.getTaskId());
		task = ThreadDaemon.asyncTimer(() -> start(), 20, 200);
	}
	
	private void start() {
		for (GDPlayer p : GD.online) {
			for (CargoItem ca : CargoEngine.inst.cargoItems.cargoItems) {
				if (p.hasItem(ca.getItem()) != -1) {
					p.addPotionEffects(ca.getEffects());
				}
			}
		}
	}
	
}
