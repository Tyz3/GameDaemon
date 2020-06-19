package ru.Baalberith.GameDaemon.WorldAnchor;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Location;

public class Dolmen {

	
	private String title;
	private List<String> startIn;
	private List<Phase> phases;
	private Location center;
	
	public Dolmen(String title, Location center, String startIn, List<Phase> phases) {
		// Формат startIn: "16:00,20:00"
		this.title = title;
		this.center = center;
		this.startIn = Arrays.asList(startIn.split(","));
		this.phases = phases;
	}
	
	public Phase getFirstPhase() {
		return phases.get(0);
	}

	public Location getCenter() {
		return center;
	}
	
	public int getX() {
		return center.getBlockX();
	}
	
	public int getY() {
		return center.getBlockY();
	}
	
	public int getZ() {
		return center.getBlockZ();
	}

	public String getTitle() {
		return title;
	}
	
	public int getPhasesAmount() {
		return phases.size();
	}

	public List<String> getStartIn() {
		return startIn;
	}
	
	public Phase getPhaseById(int id) {
		return (phases.size() > id) ? phases.get(id) : null;
	}
	
	public String getWorld() {
		return center.getWorld().getName();
	}
	
}
