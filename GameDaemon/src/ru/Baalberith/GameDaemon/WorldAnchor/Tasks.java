package ru.Baalberith.GameDaemon.WorldAnchor;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bukkit.scheduler.BukkitTask;

import ru.Baalberith.GameDaemon.Utils.ThreadDaemon;

public class Tasks implements Runnable {

	private List<Task> tasks = new ArrayList<Task>();
	private BukkitTask bukkitTask;
	private Dolmens dolmens;
	private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
	
	public void reload(Dolmens dolmens) {
		clearTasks();
		
		this.dolmens = dolmens;
		ThreadDaemon.cancelTask(bukkitTask);
		bukkitTask = ThreadDaemon.asyncTimer(this, 0, DolmenEngine.checkTime*20);
	}
	
	@Override
	public void run() {
		if (!tasks.isEmpty()) {
			for (int i = 0; i < tasks.size(); i++) {
				if (!tasks.get(i).isRunning()) cancelTask(tasks.get(i));
			}
		}
		// Получаем первый подходящий данж по времени запуска.
		String format = sdf.format(new Date());
		Dolmen dolmen = dolmens.getTimelyDolmen(format);
		if (dolmen == null) return;
		// Проверяем запущен ли якорь уже, если время провкерки < 60 секунд.
		if (alreadyStart(dolmen)) return;
		DolmenEngine.broadcast(dolmen, DolmenEngine.waitingWarmup);
		
		tasks.add(new Task(dolmen));
	}
	
	private boolean alreadyStart(Dolmen dolmen) {
		for (Task task : tasks) {
			if (task.equals(dolmen)) return true;
		}
		return false;
	}
	
	private void cancelTask(Task task) {
		task.stop();
		tasks.remove(task);
	}
	
	public void clearTasks() {
		tasks.stream().forEach((t) -> t.stop());
		tasks.clear();
	}
}
