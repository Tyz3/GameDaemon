package ru.Baalberith.GameDaemon.Summoning;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.ConfigurationSection;

import ru.Baalberith.GameDaemon.ConfigsDaemon;
import ru.Baalberith.GameDaemon.GDPlayer;
import ru.Baalberith.GameDaemon.Utils.ThreadDaemon;

public class SummonEngine {
private Map<String, Long> cooldown = new HashMap<>();
	
	private Map<GDPlayer, Task> tasks = new HashMap<>();
	
	private ConfigurationSection m;
	
	public void reload() {
		m = ConfigsDaemon.messagesConfig.getConfigurationSection("summon-items");
	}
	
	public void activate(GDPlayer p, SummonItem s) {
		Task task = getTask(p);
		if(task != null) {
			// Сообщение об отмене телепортации
			task.cancel();
			if(task.getSummonItem().equals(s)) return; // if same scroll, just cancel
		}
		
		int time = getCooldown(p.getName());
		if(time > 0) {
			p.sendMessage(m.getString("summon-cooldown").replace("{time}", String.valueOf(time)).replace("&", "\u00a7"));
			return;
		}
		
		task = new Task(p, s);
		tasks.put(p, task);
		task.start();
	}
	
	public void stop() {
		for (Task t : tasks.values()) t.cancel();
		tasks.clear();
		cooldown.clear();
	}
	
	protected void removeTask(Task t) {
		tasks.remove(t.getPlayer());
	}
	
	public void cooldownPlayer(String p) {
		int time = SummonItems.inst.getCooldown();
		cooldown.put(p, System.currentTimeMillis() + time * 1000);
		ThreadDaemon.syncLater(() -> removeCooldown(p), time * 20);
	}
	
	public void removeCooldown(String p) {
		cooldown.remove(p);
	}
	
	public int getCooldown(String p) {
		if(!cooldown.containsKey(p)) return 0;
		long time = cooldown.get(p).longValue();
		long diff = time - System.currentTimeMillis();
		if(diff <= 0) {
			cooldown.remove(p);
			return 0;
		}
		return (int) (diff / 1000);
	}
	
	public Task getTask(GDPlayer player) {
		return tasks.get(player);
	}
}
