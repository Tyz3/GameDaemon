package ru.Baalberith.GameDaemon.Extra;

import java.util.Arrays;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitTask;

import ru.Baalberith.GameDaemon.ConfigsDaemon;
import ru.Baalberith.GameDaemon.GD;
import ru.Baalberith.GameDaemon.GDPlayer;
import ru.Baalberith.GameDaemon.GDSender;
import ru.Baalberith.GameDaemon.Utils.ThreadDaemon;
import ru.Baalberith.GameDaemon.Utils.Utils;

public class WaitingSystem implements CommandExecutor, Runnable {
	

	private static ConcurrentHashMap<ActionObject, ConcurrentHashMap<String, Task>> waitingRequest = new ConcurrentHashMap<ActionObject, ConcurrentHashMap<String, Task>>();;
	private static BukkitTask task;
	private static long defaultLifeTime = 60;
	private static String cmd = "/request";
	
	public void reload() {
		ThreadDaemon.cancelTask(task);
		task = ThreadDaemon.asyncTimer(this, 0, 20*20);
	}
	
	public WaitingSystem() {
		GD.inst.getCommand("request").setExecutor(this);
	}
	
	/**
	 * Создаёт запрос с временем ожидания 60 секунд. Запрос приходит в виде сообщения в tellraw формате.
	 * @param wObject индикатор запроса для целевой активации.
	 * @param sender получатель запроса.
	 * @param r ваш код, который будет выполнен после подтверждения запроса на кнопку.
	 * @param description описание запроса, которое будет выводиться как hover на кнопке.
	 * Использует базовый формат запроса из конфига.
	 */
	public static void createRequest(ActionObject wObject, GDSender sender, Runnable r, String description, boolean touchClear) {
		ConcurrentHashMap<String, Task> hm = waitingRequest.get(wObject);
		if (hm == null) hm = new ConcurrentHashMap<String, WaitingSystem.Task>();
		hm.put(sender.getName(), new Task(r, touchClear));
		waitingRequest.put(wObject, hm);
		String[] args = description.split("\\n");
		if (args.length > 1) description = Utils.rawArray(Arrays.asList(args));
		
		sender.sendMessage(ConfigsDaemon.jsonWaitingRequest
				.replace("{description}", description)
				.replace("{command}", cmd+" "+wObject.name()
			));
	}
	
	/**
	 * Создаёт запрос с временем ожидания lifeTime. Запрос приходит в виде сообщения в tellraw формате.
	 * @param wObject индикатор запроса для целевой активации.
	 * @param sender получатель запроса.
	 * @param r ваш код, который будет выполнен после подтверждения запроса на кнопку.
	 * @param description описание запроса, которое будет выводиться как hover на кнопке.
	 * @param lifeTime время жизни запроса.
	 * Использует базовый формат запроса из конфига.
	 */
	public static void createRequest(ActionObject wObject, GDSender sender, Runnable r, String description, int lifeTime, boolean touchClear) {
		ConcurrentHashMap<String, Task> hm = waitingRequest.get(wObject);
		if (hm == null) hm = new ConcurrentHashMap<String, WaitingSystem.Task>();
		hm.put(sender.getName(), new Task(r, lifeTime, touchClear));
		waitingRequest.put(wObject, hm);
		String[] args = description.split("\\n");
		if (args.length > 1) description = Utils.rawArray(Arrays.asList(args));
		
		sender.sendMessage(ConfigsDaemon.jsonWaitingRequest
				.replace("{description}", description)
				.replace("{command}", cmd+" "+wObject.name()
			));
	}
	
	/**
	 * Создаёт запрос с временем ожидания 60 секунд. Запрос приходит в виде сообщения в tellraw формате.
	 * @param wObject индикатор запроса для целевой активации.
	 * @param sender получатель запроса.
	 * @param r ваш код, который будет выполнен после подтверждения запроса на кнопку.
	 * @param description описание запроса, которое будет выводиться как hover на кнопке.
	 * @param message кастомное сообщение с запроса. Плейсхолдеры: {description}, !{command}.
	 */
	public static void createRequest(ActionObject wObject, GDSender sender, Runnable r, String description, String message, boolean touchClear) {
		ConcurrentHashMap<String, Task> hm = waitingRequest.get(wObject);
		if (hm == null) hm = new ConcurrentHashMap<String, WaitingSystem.Task>();
		hm.put(sender.getName(), new Task(r, touchClear));
		waitingRequest.put(wObject, hm);
		String[] args = description.split("\\n");
		if (args.length > 1) description = Utils.rawArray(Arrays.asList(args));
		
		sender.sendMessage(message
				.replace("{description}", description)
				.replace("{command}", cmd+" "+wObject.name()
			));
	}
	
	/**
	 * Создаёт запрос с временем ожидания 60 секунд. Запрос приходит в виде сообщения в tellraw формате.
	 * @param wObject индикатор запроса для целевой активации.
	 * @param sender получатель запроса.
	 * @param r ваш код, который будет выполнен после подтверждения запроса на кнопку.
	 * @param description описание запроса, которое будет выводиться как hover на кнопке.
	 * @param message кастомное сообщение с запроса. Плейсхолдеры: {description}, !{command}.
	 * @param lifeTime время жизни запроса.
	 */
	public static void createRequest(ActionObject wObject, GDSender sender, Runnable r, String description, String message, int lifeTime, boolean touchClear) {
		ConcurrentHashMap<String, Task> hm = waitingRequest.get(wObject);
		if (hm == null) hm = new ConcurrentHashMap<String, WaitingSystem.Task>();
		hm.put(sender.getName(), new Task(r, lifeTime, touchClear));
		waitingRequest.put(wObject, hm);
		String[] args = description.split("\\n");
		if (args.length > 1) description = Utils.rawArray(Arrays.asList(args));
		
		sender.sendMessage(message
				.replace("{description}", description)
				.replace("{command}", cmd+" "+wObject.name()
			));
	}
	
	
	@Override
	public void run() {
		// Удаляет неотвеченные запросы.
		for (ActionObject wObject : ActionObject.values()) {
			ConcurrentHashMap<String, Task> hm = waitingRequest.get(wObject);
			if (hm == null || hm.isEmpty()) continue;
			for (Entry<String, Task> e : hm.entrySet()) {
				Task task = e.getValue();
				String taskName = e.getKey(); // попробовать оставить только e.getKey() вместо taskName
				
				if (task.hasTouchClear() && task.isEnded()) {
					hm.remove(taskName);
				}
				
				if (task.isExpired()) {
					hm.remove(taskName);
					GDPlayer p = GD.getGDPlayer(taskName);
					if (p != null && task.touchClear) p.sendMessage(ConfigsDaemon.requestCancelled);
				}
			}
		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length != 1) return false;
		ActionObject wObject = ActionObject.valueOf(args[0]);
		if (wObject == null) return false;
		ConcurrentHashMap<String, Task> hm = waitingRequest.get(wObject);
		if (hm == null) return false;
		Task t = hm.get(sender.getName());
		if (t == null) return false;
		t.run();
		return true;
	}
	
	private static class Task {
		
		private Runnable r;
		private long expireTime;
		private boolean ended = false;
		private boolean touchClear = true;
		
		public Task(Runnable r, boolean touchClear) {
			this.r = r;
			this.expireTime = System.currentTimeMillis() + defaultLifeTime*1000;
			this.touchClear = touchClear;
		}
		
		public Task(Runnable r, int lifeTime, boolean touchClear) {
			this.r = r;
			this.expireTime = System.currentTimeMillis() + lifeTime*1000;
			this.touchClear = touchClear;
		}
		
		public void run() {
			r.run();
			ended = true;
		}
		
		public boolean isEnded() {
			return ended;
		}
		
		public boolean hasTouchClear() {
			return touchClear;
		}
		
		public boolean isExpired() {
			return System.currentTimeMillis() >= expireTime ? true : false;
		}
	}
	
	public static void removeRequest(String playerName, ActionObject wObject) {
		ConcurrentHashMap<String, Task> hm = waitingRequest.get(wObject);
		if (hm == null || hm.isEmpty()) return;
		hm.remove(playerName);
	}
}
