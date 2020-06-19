package ru.Baalberith.GameDaemon.Summoning;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.scheduler.BukkitTask;

import ru.Baalberith.GameDaemon.ConfigsDaemon;
import ru.Baalberith.GameDaemon.GD;
import ru.Baalberith.GameDaemon.GDPlayer;
import ru.Baalberith.GameDaemon.Utils.ThreadDaemon;

public class Task implements Runnable {
	private GDPlayer p;
	private SummonItem s;
	
	private int ticked = 0;
	private Location loc;
	
	private int time;
	
	private BukkitTask task;
	
	private ConfigurationSection m = ConfigsDaemon.messagesConfig.getConfigurationSection("summon-items");

	public Task(GDPlayer p, SummonItem s) {
		this.p = p;
		this.s = s;
		this.time = s.getWarmup();
		this.loc = s.getSummonLocation();
	}
	
	public void start() {
		if(this.loc == null) {
			// Нет локации для спавна
			p.sendMessage(m.getString("location-not-set").replace("&", "\u00a7"));
			cancel();
			return;
		}
		
		if(!checkArea()) {
			// Нельзя активировать предмет здесь
			p.sendMessage(m.getString("check-area").replace("&", "\u00a7"));
			cancel();
			return;
		}
		
		task = ThreadDaemon.syncTimer(this, 0, 20);
		// Активация предмета, ожидание в {time}
		p.sendMessage(m.getString("summon-activated").replace("{time}", String.valueOf(time)).replace("&", "\u00a7"));
		if (s.hasGlobalMessage()) GD.broadcast(s.getStartGlobalMessage()
				.replace("{S}", String.valueOf(time)).replace("{player}", p.getName())
				.replace("{X}", String.valueOf(loc.getBlockX())).replace("{Y}", String.valueOf(loc.getBlockY()))
				.replace("{Z}", String.valueOf(loc.getBlockZ())));
		s.setReload();
	}
	
	@Override
	public void run() {
		int remain = time - ticked;
		
		if(!checkItem()) {
			cancel();
			// Телепортация отменена
			p.sendMessage(m.getString("summon-cancelled").replace("&", "\u00a7"));
			return;
		}
		
		if(remain <= 0) {
			cancel();
			process();
			return;
		}
		
		if(SummonItems.inst.isMessageIn(remain)) {
			// Уведомление о телепортации
			p.sendMessage(m.getString("summon-timing").replace("{time}", String.valueOf(remain)).replace("&", "\u00a7"));
		}
		
		ticked++;
	}
	
	private void process() {
		if(!p.takeItem(s.getItem())) {
			s.removeReload();
			p.sendMessage(m.getString("summon-cancelled").replace("&", "\u00a7"));
			return;
		}

		// Действие на сервере
		execCmd(p);
		SummonItems.inst.engine.cooldownPlayer(p.getName());
		p.sendMessage(m.getString("summon-release").replace("&", "\u00a7"));
		if (s.hasGlobalMessage()) GD.broadcast(s.getEndGlobalMessage()
				.replace("{player}", p.getName()).replace("{X}", String.valueOf(loc.getBlockX()))
				.replace("{Y}", String.valueOf(loc.getBlockY())).replace("{Z}", String.valueOf(loc.getBlockZ())));
	}
	
	private void execCmd(GDPlayer p) {
		List<String> summons = s.getSummons();
		for (String cmd : summons) {
			GD.dispatchCommand(cmd);
		}
	}
	
	private boolean checkArea() {
		Location pLoc = p.getLocation();
		pLoc.setY(pLoc.getBlockY() - 1);
		return SummonItems.inst.isSummonArea(pLoc, loc);
	}
	
	private boolean checkItem() {
		return p.hasItem(s.getItem()) != -1;
	}
	
	public void cancel() {
		s.removeReload();
		if (task != null)
			Bukkit.getScheduler().cancelTask(task.getTaskId());
		task = null;
		SummonItems.inst.engine.removeTask(this);
	}
	
	public GDPlayer getPlayer() {
		return p;
	}
	
	public SummonItem getSummonItem() {
		return s;
	}
}
