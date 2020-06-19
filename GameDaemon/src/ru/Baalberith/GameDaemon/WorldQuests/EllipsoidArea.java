package ru.Baalberith.GameDaemon.WorldQuests;

import org.bukkit.Location;

import ru.Baalberith.GameDaemon.Utils.LocationManager;

public class EllipsoidArea {
	
	private Location center;
	
	private String world;
	private double x0;
	private double y0;
	private double z0;
	
	private int widthHalf;
	private int lengthHalf;
	private int heightHalf;
	
	public EllipsoidArea(Location center, int width, int length, int height) {
		setCenter(center);
		
		setWidth(width);
		setLength(length);
		setHeight(height);
	}
	
	public EllipsoidArea(String centerSrc, int width, int length, int height) {
		this(LocationManager.deserializeLocation(centerSrc), width, length, height);
	}
	
	public void setCenter(Location center) {
		this.world = center.getWorld().getName();
		this.x0 = center.getX();
		this.y0 = center.getY();
		this.z0 = center.getZ();
	}
	
	public void setWidth(int width) {
		this.widthHalf = width >> 1;
	}
	
	public void setLength(int length) {
		this.lengthHalf = length >> 1;
	}
	
	public void setHeight(int height) {
		this.heightHalf = height >> 1;
	}
	
	public int getLength() {
		return lengthHalf << 1;
	}
	
	public int getWidth() {
		return widthHalf << 1;
	}
	
	public int getHeight() {
		return heightHalf << 1;
	}
	
	public Location getCenter() {
		return center;
	}
	
	public boolean containsPoint(Location point) {
		return containsPoint(point.getWorld().getName(), point.getX(), point.getY(), point.getZ());
	}
	
	public boolean containsPoint(String world, double x, double y, double z) {
		if (!this.world.equals(world)) return false;
		
		return 1 >= ( Math.pow((x - x0)/lengthHalf, 2) + Math.pow((y - y0)/heightHalf, 2) + Math.pow((z - z0)/widthHalf, 2) );
	}
}
