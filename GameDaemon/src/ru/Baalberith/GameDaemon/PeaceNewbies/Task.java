package ru.Baalberith.GameDaemon.PeaceNewbies;


import java.util.Date;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import ru.Baalberith.GameDaemon.GD;
import ru.Baalberith.GameDaemon.GDPlayer;
import ru.Baalberith.GameDaemon.Utils.ThreadDaemon;

public class Task {

	public static long TASK_TIMER;
	private BukkitTask bukkitTask;
	
	
	public void reload() {
		if (bukkitTask != null) Bukkit.getScheduler().cancelTask(bukkitTask.getTaskId());
		bukkitTask = ThreadDaemon.asyncTimer(() -> run(), 20, TASK_TIMER);
		
	}
	
	// Удаляет статус нуба у игрока.
	private void run() {
		long time1 = new Date().getTime();
		// Добавляем нубов из очереди в общий список.
		Newbies.inst.newbies.removeAll(Newbies.inst.waitingToRemove);
		Newbies.inst.waitingToRemove.clear();
		Newbies.inst.newbies.addAll(Newbies.inst.waitingToAdd);
		Newbies.inst.waitingToAdd.clear();
		
		for (GDPlayer p : GD.online) {
			Newbie newbie = Newbies.inst.getNewbie(p.getName());
			if (newbie != null) {
				// TODO Доделать спавн частиц у новичка
				if (newbie.isExpired(p)) {
					Newbies.inst.removeNewbie(newbie.getName());
					newbie.sendMessage(Newbies.MSG_EXPIRED);
					broadcast(newbie.getName());
				}
			}
		}

		long time2 = new Date().getTime() - time1;
		GD.log("PeaceNewbies.Task.run(), ["+time2+" ms]");
	}
	
	
	public void stop() {
		if (bukkitTask != null) Bukkit.getScheduler().cancelTask(bukkitTask.getTaskId());
	}

	private void broadcast(String player) {
		GD.broadcast(Newbies.MSG_BROADCAST.replace("{player}", player));
	}
	
	
}
