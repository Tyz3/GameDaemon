package ru.Baalberith.GameDaemon.Extra;

import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitTask;

import ru.Baalberith.GameDaemon.ConfigsDaemon;
import ru.Baalberith.GameDaemon.GD;
import ru.Baalberith.GameDaemon.GDPlayer;
import ru.Baalberith.GameDaemon.GDSender;
import ru.Baalberith.GameDaemon.Utils.CountingPattern;
import ru.Baalberith.GameDaemon.Utils.MathOperation;
import ru.Baalberith.GameDaemon.Utils.ThreadDaemon;

public class WarmUpSystem implements Listener {

	private static ConcurrentHashMap<ActionObject, ConcurrentHashMap<String, Task>> tasks;
	private BukkitTask task;
	
	public void reload() {
		ThreadDaemon.cancelTask(task);
		task = ThreadDaemon.asyncTimer(() -> clear(), 0, 20*20);
		ThreadDaemon.syncTimer(() -> onMove(), 20, 10);
	}
	
	public WarmUpSystem() {
		tasks = new ConcurrentHashMap<ActionObject, ConcurrentHashMap<String,Task>>();
		Bukkit.getPluginManager().registerEvents(this, GD.inst);
	}
	
	public static void startWarmUp(ActionObject wuObject, GDSender sender, long delay, Runnable r, boolean canMove) {
		if (sender.isOp() || sender.hasPermission("gsm.warmupall.bypass")) delay = 0;
		
		ConcurrentHashMap<String, Task> hm = tasks.get(wuObject);
		if (hm == null) hm = new ConcurrentHashMap<String, WarmUpSystem.Task>();
		else {
			Task t = hm.get(sender.getName());
			if (t != null) t.stop();
		}
		hm.put(sender.getName(), new Task(r, sender, delay, canMove, null));
		tasks.put(wuObject, hm);
	}
	
	public static void startWarmUp(ActionObject wuObject, GDSender sender, long delay, Runnable r, boolean canMove, CountingPattern pattern) {
		if (sender.isOp() || sender.hasPermission("gsm.warmupall.bypass")) delay = 0;

		ConcurrentHashMap<String, Task> hm = tasks.get(wuObject);
		if (hm == null) hm = new ConcurrentHashMap<String, WarmUpSystem.Task>();
		else {
			Task t = hm.get(sender.getName());
			if (t != null) t.stop();
		}
		hm.put(sender.getName(), new Task(r, sender, delay, canMove, pattern));
		tasks.put(wuObject, hm);
	}
	
	private void clear() {
		for (ActionObject wuo : ActionObject.values()) {
			ConcurrentHashMap<String, Task> hm = tasks.get(wuo);
			if (hm == null || hm.isEmpty()) continue;

			for (Entry<String, Task> e : hm.entrySet()) {
				if (e.getValue().isEnded())
					hm.remove(e.getKey());
			}
		}
	}
	
	private static class Task implements Runnable {
		private Runnable r;
		private GDPlayer p;
		private long delay;
		private BukkitTask bukkitTask;
		private boolean canMove;
		private CountingPattern pattern;
		private Location activationPlace;
		private boolean cancelled = false;
		
		public Task(Runnable r, GDSender sender, long delay, boolean canMove, CountingPattern pattern) {
			this.r = r;
			this.p = (GDPlayer) sender;
			this.delay = delay;
			this.canMove = canMove;
			this.pattern = pattern;
			this.activationPlace = p.getLocation();
			
			bukkitTask = ThreadDaemon.asyncTimer(this, 0, 20);
		}
		
		@Override
		public void run() {
			if (delay <= 0) {
				r.run();
				stop();
				return;
			}
			
			if (p == null) stop();
			if (pattern == null || pattern.contains((int) delay))
				p.sendMessage(ConfigsDaemon.warmUpSession.replace("{S}", String.valueOf(delay)));
			delay--;
		}
		
		private void stop() {
			ThreadDaemon.cancelTask(bukkitTask);
			cancelled = true;
		}
		
		public void onMove() {
			if (!cancelled && MathOperation.distance2D(activationPlace, p.getLocation()) > 0 && !canMove) {
				stop();
				p.sendMessage(ConfigsDaemon.warmUpCancelled);
			}
		}
		
		public void onTeleport() {
			if (cancelled || canMove) return;
			stop();
			p.sendMessage(ConfigsDaemon.warmUpCancelled);
		}
		
		public void onQuit() {
			if (!cancelled) stop();
		}
		
		public boolean isEnded() {
			return delay <= 0 ? true : false;
		}
	}
	
	private void onMove() {
		for (Entry<ActionObject, ConcurrentHashMap<String, Task>> t : tasks.entrySet()) {
			for (Entry<String, Task> e : t.getValue().entrySet()) {
				e.getValue().onMove();
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onTeleport(PlayerTeleportEvent e) {
		for (Entry<ActionObject, ConcurrentHashMap<String, Task>> t : tasks.entrySet()) {
			Task task = t.getValue().get(e.getPlayer().getName());
			if (task == null) return;
			task.onTeleport();
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onQuit(PlayerQuitEvent e) {
		for (Entry<ActionObject, ConcurrentHashMap<String, Task>> t : tasks.entrySet()) {
			Task task = t.getValue().get(e.getPlayer().getName());
			if (task == null) return;
			task.onQuit();
		}
	}
}
