package ru.Baalberith.GameDaemon.Summoning;

import java.util.Date;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import ru.Baalberith.GameDaemon.GDPlayer;

public class SummonItem {
	
	private ItemStack item;
	private long expiredDuration = 0;
	private int globalCooldown = 0;
	private int warmup;
	private Location summonLocation;
	private String permission;
	private List<String> summons;
	private String startGlobalMessage;
	private String endGlobalMessage;
	private boolean hasGlobalMessage;
	
	public SummonItem(ItemStack item, int warmup, String permission, Location summonLocation, List<String> summons) {
		this.item = item;
		this.warmup = warmup;
		this.permission = permission;
		this.summonLocation = summonLocation;
		this.summons = summons;
		this.startGlobalMessage = null;
		this.endGlobalMessage = null;
		this.hasGlobalMessage = false;
	}
	
	public SummonItem(ItemStack item, int globalCooldown, int warmup, String permission, Location summonLocation, List<String> summons, String startGlobalMessage, String endGlobalMessage, boolean hasGlobalMessage) {
		this(item, warmup, permission, summonLocation, summons);
		this.globalCooldown = globalCooldown;
		this.startGlobalMessage = startGlobalMessage;
		this.endGlobalMessage = endGlobalMessage;
		this.hasGlobalMessage = hasGlobalMessage;
	}
	
	public ItemStack getItem() {
		return item;
	}
	
	public int getWarmup() {
		return warmup;
	}
	
	public void setReload() {
		this.expiredDuration = new Date().getTime() + (long) (globalCooldown * 1000);
	}
	
	public void removeReload() {
		this.expiredDuration = 0;
	}
	
	public boolean isReloaded() {
		long date = new Date().getTime();
		return date >= expiredDuration ? true : false;
	}
	
	public boolean hasPermission(GDPlayer player) {
		return permission.isEmpty() || player.hasPermission(permission);
	}
	
	public Location getSummonLocation() {
		return summonLocation;
	}
	
	public List<String> getSummons() {
		return summons;
	}

	public boolean hasGlobalMessage() {
		return hasGlobalMessage;
	}

	public String getEndGlobalMessage() {
		return endGlobalMessage;
	}

	public String getStartGlobalMessage() {
		return startGlobalMessage;
	}
}
