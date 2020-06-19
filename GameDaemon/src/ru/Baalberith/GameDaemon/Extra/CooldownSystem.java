package ru.Baalberith.GameDaemon.Extra;

import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitTask;

import ru.Baalberith.GameDaemon.ConfigsDaemon;
import ru.Baalberith.GameDaemon.GDPlayer;
import ru.Baalberith.GameDaemon.Utils.ThreadDaemon;

public class CooldownSystem {
	
	BukkitTask task;
	
	private static ConcurrentHashMap<ActionObject, ConcurrentHashMap<String, Long>> cooldowns;
	
	public CooldownSystem() {
		cooldowns = new ConcurrentHashMap<ActionObject, ConcurrentHashMap<String, Long>>();
	}
	
	public void reload() {
		ThreadDaemon.cancelTask(task);
		task = ThreadDaemon.asyncTimer(() -> clear(), 0, 20*20);
	}
	
	private void clear() {
		long now = System.currentTimeMillis();
		for (ActionObject co : ActionObject.values()) {
			ConcurrentHashMap<String, Long> hm = cooldowns.get(co);
			if (hm == null || hm.isEmpty()) continue;
			for (Entry<String, Long> e : hm.entrySet()) {
				if (now < e.getValue()) continue;
				hm.remove(e.getKey());
			}
		}
	}
	
	/**
	@param cooldown Время в секундах, которое даётся игроку после выполнения действия.
	*/
	public static void add(ActionObject cObject, GDPlayer p, long cooldown) {
		long expires = System.currentTimeMillis() + cooldown*1000;
		ConcurrentHashMap<String, Long> hm = cooldowns.get(cObject);
		if (hm == null) hm = new ConcurrentHashMap<String, Long>();
		hm.put(p.getName(), expires);
		cooldowns.put(cObject, hm);
	}
	
	/**
	@param cooldown Время в секундах, которое даётся игроку после выполнения действия.
	*/
	public static void add(ActionObject cObject, CommandSender p, long cooldown) {
		long expires = System.currentTimeMillis() + cooldown*1000;
		ConcurrentHashMap<String, Long> hm = cooldowns.get(cObject);
		if (hm == null) hm = new ConcurrentHashMap<String, Long>();
		hm.put(p.getName(), expires);
		cooldowns.put(cObject, hm);
	}
	
	public static boolean isExpired(ActionObject cObject, GDPlayer p) {
		// Если игрок
		if (p == null) return true;
		long now = System.currentTimeMillis();
		ConcurrentHashMap<String, Long> hm = cooldowns.get(cObject);
		if (hm == null) return true;
		Long time = hm.get(p.getName());
		if (time == null) return true;
		if (now >= time) return true;
		p.sendMessage(ConfigsDaemon.hasCooldown.replace("{S}", String.valueOf((time - now)/1000)));
		return false;
	}
	
	public static boolean isExpired(ActionObject cObject, CommandSender p) {
		// Если игрок
		if (p == null) return true;
		long now = System.currentTimeMillis();
		ConcurrentHashMap<String, Long> hm = cooldowns.get(cObject);
		if (hm == null) return true;
		Long time = hm.get(p.getName());
		if (time == null) return true;
		if (now >= time) return true;
		p.sendMessage(ConfigsDaemon.hasCooldown.replace("{S}", String.valueOf((time - now)/1000)));
		return false;
	}
}
