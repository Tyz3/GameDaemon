package ru.Baalberith.GameDaemon.Clans.Dungeons;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import ru.Baalberith.GameDaemon.GDPlayer;
import ru.Baalberith.GameDaemon.Utils.LocationManager;
import ru.Baalberith.GameDaemon.Utils.MathOperation;

public class Layer {

	private boolean enabled = false;
	private List<Region> regions = new ArrayList<Layer.Region>();
	private List<Wayback> waybacks = new ArrayList<Layer.Wayback>();
	private Location spawn;
	private boolean busy = false;
	private int layerNumber;
	private int heightOffset;
	
	public Layer(boolean enabled, List<String> regions, List<String> waybacks, String spawn, int heightOffset, int layerNumber) {
		this.enabled = enabled;
		
		regions.stream().forEach(reg -> this.regions.add(createRegion(reg, heightOffset, layerNumber - 1)));
		waybacks.stream().forEach(wb -> this.waybacks.add(createWayback(wb, heightOffset, layerNumber - 1)));
		
		// world x y z yaw pitch
		this.spawn = LocationManager.deserializeLocation(spawn);
		if (this.spawn == null) throw new NullPointerException("Spawn location is null!");
		this.spawn.setY(this.spawn.getY() + heightOffset*(layerNumber - 1));
		
		this.layerNumber = layerNumber;
		this.heightOffset = heightOffset;
	}
	
	private Region createRegion(String source, int heightOffset, int layerNumber) {
		return new Region(source, heightOffset, layerNumber);
	}
	
	private Wayback createWayback(String source, int heightOffset, int layerNumber) {
		return new Wayback(source, heightOffset, layerNumber);
	}
	
	public void newWayback(Location loc) {
		waybacks.add(new Wayback(loc, heightOffset, layerNumber-1));
	}
	
	public void newRegion(String locSrc) {
		regions.add(new Region(locSrc, heightOffset, layerNumber-1));
	}
	
	public void removeWayback(int index) {
		waybacks.remove(index);
	}
	
	public void removeRegion(int index) {
		regions.remove(index);
	}
	
	public List<Wayback> getWaybacks() {
		return waybacks;
	}
	
	public List<Region> getRegions() {
		return regions;
	}
	
	public void loadChunks() {
		regions.stream().forEach(reg -> reg.loadChunks());
	}
	
	public void despawnEntities() {
		regions.stream().forEach(reg -> reg.despawnEntities());
	}
	
	public int number() {
		return layerNumber;
	}
	
	public void teleportToSpawn(GDPlayer p) {
		p.teleportSync(spawn);
	}
	
	public boolean isBusy() {
		return busy;
	}
	
	public void setBusy(boolean mode) {
		busy = mode;
	}
	
	public Location getSpawnLocation() {
		return spawn;
	}
	
	public void setSpawnLocation(Location loc) {
		loc.setY(loc.getY() + heightOffset*(layerNumber - 1));
		spawn = loc;
	}
	
	public boolean isWaybackByLocation(Location loc) {
		for (Wayback wb : waybacks) {
			if (wb.equals(loc)) return true;
		}
		return false;
	}
	
	public class Wayback {
		
		private Location loc;
		
		public Wayback(String source, int heightOffset, int layerNumber) {
			// world x y z
			loc = LocationManager.deserializeLocation(source);
			if (loc == null) throw new NullPointerException("Wayback location is null!");
			loc.setY(loc.getY() + heightOffset*layerNumber);
		}
		
		public Wayback(Location loc, int heightOffset, int layerNumber) {
			// world x y z
			this.loc = loc;
			if (this.loc == null) throw new NullPointerException("Wayback location is null!");
			this.loc.setY(loc.getY() + heightOffset*layerNumber);
		}
		
		@Override
		public String toString() {
			return LocationManager.serializeLocation(loc);
		}
		
		public Location getLocation() {
			return loc;
		}
		
		public boolean equals(Location loc) {
			return MathOperation.distance3D(this.loc, loc) == 0;
		}
	}
	
	public class Region {
		
		private World world;
		private Location pos1;
		private Location pos2;
		
		public Region(String source, int heightOffset, int layerNumber) throws IllegalArgumentException {
			// world x1 z1 x2 z2 y1 y2
			String[] args = source.split(" ");
			if (args.length != 7) throw new IllegalArgumentException("Invalid region args.");
			
			String worldName = args[0];
			int x1 = Integer.parseInt(args[1]);
			int z1 = Integer.parseInt(args[2]);
			int x2 = Integer.parseInt(args[3]);
			int z2 = Integer.parseInt(args[4]);
			int y1 = Integer.parseInt(args[5]) + layerNumber*heightOffset;
			int y2 = Integer.parseInt(args[6]) + layerNumber*heightOffset; // y1 + heightOffset + layerNumber*heightOffset;
			
			world = Bukkit.getWorld(worldName);
			if (world == null) throw new IllegalArgumentException("Invalid world name in region info.");
			pos1 = new Location(world, x1, y1, z1);
			pos2 = new Location(world, x2, y2, z2);
		}
		
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append(world.getName()).append(" ").append(pos1.getBlockX()).append(" ").append(pos1.getBlockZ()).append(" ")
				.append(pos2.getBlockX()).append(" ").append(pos2.getBlockZ()).append(" ")
				.append(pos1.getBlockY()).append(" ").append(pos2.getBlockY());
			return sb.toString();
		}
		
		public String getWorldName() {
			return world.getName();
		}
		
		public Location getPos1() {
			return pos1;
		}
		
		public Location getPos2() {
			return pos2;
		}
		
		public void loadChunks() {
			loadMap(world);
		}
		
		public void despawnEntities() {
			List<Chunk> chunks = loadMap(world);
			for (Chunk chunk : chunks) {
				for (Entity e : chunk.getEntities()) {
					if(!checkType(e)) continue;
					if(!checkDistance(e)) continue;
					e.remove();
				}
			}
		}
		
		private List<Chunk> loadMap(World w) {
			List<Chunk> chunks = new ArrayList<Chunk>();
			for (int x = pos1.getBlockX(); x < pos2.getBlockX() + 16; x += 16) {
				for (int z = pos1.getBlockZ(); z < pos2.getBlockZ() + 16; z += 16) {
					Chunk c = w.getChunkAt(x >> 4, z >> 4);
					chunks.add(c);
					if(!c.isLoaded()) c.load();
				}
			}
			return chunks;
		}
		
		private boolean checkDistance(Entity e) {
			Location loc = e.getLocation();
			return MathOperation.containsCuboid(pos1, pos2, loc);
		}
		
		private boolean checkType(Entity e) {
			if (e instanceof Player) return false;
			if (e instanceof LivingEntity) return true;
			EntityType type = e.getType();
			if (type == EntityType.DROPPED_ITEM || type == EntityType.ARROW || type == EntityType.BOAT || type == EntityType.MINECART
					|| type == EntityType.EXPERIENCE_ORB) return true;
			return false;
		}
	}
	
	public boolean on() {
		if (spawn == null) return false;
		return (enabled = true);
	}
	
	public void off() {
		enabled = false;
	}
	
	public boolean isEnabled() {
		return enabled;
	}
}
