package ru.Baalberith.GameDaemon.CargoDelivery;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import ru.Baalberith.GameDaemon.Utils.MathOperation;

public class CargoItem {
	
	private ItemStack item;
	private List<PotionEffect> effects;
	private List<DropPoint> dropPoints;
	
	
	public CargoItem(ItemStack item, List<PotionEffect> effects, List<DropPoint> dropPoints) {
		this.item = item;
		this.effects = effects;
		this.dropPoints = dropPoints;
	}
	
	public ItemStack getItem() {
		return item;
	}


	public List<PotionEffect> getEffects() {
		return effects;
	}
	
	public DropPoint getPointByLocation(Location playerLocation) {
		for (DropPoint dp : dropPoints) {
			if (dp.withinPointRadius(playerLocation)) return dp;
		}
		return null;
	}
	
	public Location getNearestPoint(Location playerLocation) {
		double distance = -1;
		Location nLoc = null;
		for (DropPoint dp : dropPoints) {
			if (distance == -1) {
				distance = MathOperation.distance3D(playerLocation, dp.getLocation());
				nLoc = dp.getLocation();
				continue;
			}
			double newDist = MathOperation.distance3D(playerLocation, dp.getLocation());
			if (newDist < distance) {
				distance = newDist;
				nLoc = dp.getLocation();
			}
		}
		return nLoc;
	}
						
}
