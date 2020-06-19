package ru.Baalberith.GameDaemon.CNPSsFactions;

import org.bukkit.inventory.ItemStack;

public class FactionItem {
	
	private ItemStack item;
	private boolean increaseMode;
	private int points;
	private int factionId;
	
	
	FactionItem(ItemStack item, boolean increaseMode, int points, int factionId) {
		this.item = item;
		this.increaseMode = increaseMode;
		this.points = points;
		this.factionId = factionId;
	}


	public ItemStack getItem() {
		return item;
	}


	public String getIncreaseMode() {
		return (increaseMode) ? "add" : "subtract";
	}


	public int getPoints() {
		return points;
	}


	public int getFactionId() {
		return factionId;
	}
}
