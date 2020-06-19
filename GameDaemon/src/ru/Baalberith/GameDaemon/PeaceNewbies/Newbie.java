package ru.Baalberith.GameDaemon.PeaceNewbies;

import org.bukkit.Statistic;

import ru.Baalberith.GameDaemon.GD;
import ru.Baalberith.GameDaemon.GDPlayer;

public class Newbie {
	
	private String name;
	private long expireTime;
	
	public Newbie(String name, long expireTime) {
		this.name = name;
		this.expireTime = expireTime;
	}
	
	public Newbie(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}

	public long getExpireTime() {
		return expireTime;
	}
	
	public boolean isExpired(GDPlayer p) {
//		return expireTime < new Date().getTime() ? true : false;
		return Newbies.EXPIRE_TIME > p.p.getStatistic(Statistic.PLAY_ONE_TICK) * 50 ? false : true;
	}
	
	public GDPlayer getGDPlayer() {
		return GD.getGDPlayer(name);
	}
	
	public void sendMessage(String msg) {
		getGDPlayer().sendMessage(msg);
	}
	
	public long getResidual() {
		GDPlayer p = GD.getGDPlayer(name);
		long residual = Newbies.EXPIRE_TIME - p.p.getStatistic(Statistic.PLAY_ONE_TICK) * 50;
		return residual < 0 ? 0 : residual;
	}
}
