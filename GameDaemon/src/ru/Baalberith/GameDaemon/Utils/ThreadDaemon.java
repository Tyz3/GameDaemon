package ru.Baalberith.GameDaemon.Utils;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

import ru.Baalberith.GameDaemon.GD;

public class ThreadDaemon {
	
	private static BukkitScheduler shed = Bukkit.getScheduler();
	
	public static BukkitTask sync(Runnable r) {
		return shed.runTask(GD.inst, r);
	}
	
	public static BukkitTask syncTimer(Runnable r, long start, long repeate) {
		return shed.runTaskTimer(GD.inst, r, start, repeate);
	}
	
	public static BukkitTask syncLater(Runnable r, long delay) {
		return shed.runTaskLater(GD.inst, r, delay);
	}
	
	public static BukkitTask async(Runnable r) {
		return shed.runTaskAsynchronously(GD.inst, r);
	}
	
	public static BukkitTask asyncTimer(Runnable r, long start, long repeate) {
		return shed.runTaskTimerAsynchronously(GD.inst, r, start, repeate);
	}
	
	public static BukkitTask asyncLater(Runnable r, long delay) {
		return shed.runTaskLaterAsynchronously(GD.inst, r, delay);
	}
	
	public static void cancelTask(BukkitTask task) {
		if (task != null) Bukkit.getScheduler().cancelTask(task.getTaskId());
	}
}
