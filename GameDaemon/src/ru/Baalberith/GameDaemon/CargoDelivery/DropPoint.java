package ru.Baalberith.GameDaemon.CargoDelivery;

import org.bukkit.Location;

public class DropPoint {
	private Location loc;
	private double salePrice;
	private int dropRadius;
	
	public DropPoint(Location loc, int dropRadius, double salePrice) {
		this.loc = loc;
		this.dropRadius = dropRadius;
		this.salePrice = salePrice;
	}
	
	public Location getLocation() {
		return loc;
	}
	
	public int getDropRadius() {
		return dropRadius;
	}

	public double getSalePrice() {
		return salePrice;
	}
	
	public boolean withinPointRadius(Location pLoc) {
		return (pLoc.getBlockX() <= loc.getBlockX()+dropRadius && pLoc.getBlockX() >= loc.getBlockX()-dropRadius
				&& pLoc.getBlockY() <= loc.getBlockY()+dropRadius && pLoc.getBlockY() >= loc.getBlockY()-dropRadius
				&& pLoc.getBlockZ() <= loc.getBlockZ()+dropRadius && pLoc.getBlockZ() >= loc.getBlockZ()-dropRadius);
	}
}
