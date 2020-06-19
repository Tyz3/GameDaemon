package ru.Baalberith.GameDaemon.Warps;

import ru.Baalberith.GameDaemon.GDPlayer;

public class ConfiguredLimit {
	
	public String permission;
	private int whitelistLimit;
	private int blacklistLimit;
	private int totalLimit;
	private int publicLimit;
	private int privateLimit;
	
	public ConfiguredLimit(String permission, int whitelistLimit, int blacklistLimit, int totalLimit, int publicLimit, int privateLimit) {
		this.permission = permission;
		this.whitelistLimit = whitelistLimit;
		this.blacklistLimit = blacklistLimit;
		this.totalLimit = totalLimit;
		this.publicLimit = publicLimit;
		this.privateLimit = privateLimit;
	}
	
	public int getWhitelistLimit() {
		return whitelistLimit;
	}
	
	public int getBlacklistLimit() {
		return blacklistLimit;
	}
	
	public boolean checkPerm(GDPlayer p) {
		return p.hasPermission(permission);
	}
	
	public boolean reachedWhitelistLimit(int now) {
		return whitelistLimit <= now ? true : false;
	}
	
	public boolean reachedBlacklistLimit(int now) {
		return blacklistLimit <= now ? true : false;
	}
	
	public boolean reachedTotalLimit(int now) {
		return totalLimit <= now ? true : false;
	}
	
	public boolean reachedPublicLimit(int now) {
		return publicLimit <= now ? true : false;
	}
	
	public boolean reachedPrivateLimit(int now) {
		return privateLimit <= now ? true : false;
	}
}
