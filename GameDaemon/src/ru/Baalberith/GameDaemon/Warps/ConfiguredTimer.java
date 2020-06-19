package ru.Baalberith.GameDaemon.Warps;

import ru.Baalberith.GameDaemon.GDPlayer;

public class ConfiguredTimer {
	
	private String permission;
	private int cooldown;
	private int warmUp;
	
	public ConfiguredTimer(String permission, int cooldown, int warmUp) {
		this.permission = permission;
		this.cooldown = cooldown;
		this.warmUp = warmUp;
	}
	
	public boolean checkPerm(GDPlayer sender) {
		return sender.hasPermission(permission);
	}

	public int getCooldown() {
		return cooldown;
	}

	public int getWarmUp() {
		return warmUp;
	}
	
}
