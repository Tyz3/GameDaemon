package ru.Baalberith.GameDaemon;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Projectile;

import ru.Baalberith.GameDaemon.Utils.MathOperation;

public class GDWorld {
	
	public static List<GDPlayer> getNearestPlayers3D(Location center, int radius) {
		List<GDPlayer> nearest = new ArrayList<GDPlayer>();
		GD.online.stream().forEach(o -> {
			if (center.getWorld().equals(o.getWorld()) && MathOperation.distance3D(center, o.getLocation()) <= radius)
				nearest.add(o);
		});
		return nearest;
	}
	
	public static List<GDPlayer> getNearestPlayers2D(Location center, int radius) {
		List<GDPlayer> nearest = new ArrayList<GDPlayer>();
		GD.online.stream().forEach(o -> {
			if (center.getWorld().equals(o.getWorld()) && MathOperation.distance2D(center, o.getLocation()) <= radius)
				nearest.add(o);
		});
		return nearest;
	}
	
	public static int amountOfNearestPlayers3D(Location center, int radius) {
		int amount = 0;
		for (GDPlayer o : GD.online) {
			if (center.getWorld().equals(o.getWorld()) && MathOperation.distance3D(center, o.getLocation()) <= radius)
				amount++;
		}
		return amount;
	}
	
	public static int amountOfNearestPlayers2D(Location center, int radius) {
		int amount = 0;
		for (GDPlayer o : GD.online) {
			if (center.getWorld().equals(o.getWorld()) && MathOperation.distance2D(center, o.getLocation()) <= radius)
				amount++;
		}
		return amount;
	}
	
	public static void sendMessageToNearest3D(String msg, Location center, int radius) {
		GD.online.stream().forEach(o -> {
			if (center.getWorld().equals(o.getWorld()) && MathOperation.distance3D(center, o.getLocation()) <= radius)
				o.sendMessage(msg);
		});
	}
	
	public static void sendMessageToNearest2D(String msg, Location center, int radius) {
		GD.online.stream().forEach(o -> {
			if (center.getWorld().equals(o.getWorld()) && MathOperation.distance2D(center, o.getLocation()) <= radius)
				o.sendMessage(msg);
		});
	}
	
	public static List<Chunk> getChunks(Location center, int radius) {
		List<Chunk> chunks = new ArrayList<Chunk>();
		World w = center.getWorld();
		int X = center.getBlockX();
		int Z = center.getBlockZ();
		int x = (int) Math.ceil(X < 0 ? X/16 - 1 : X/16);
		int z = (int) Math.ceil(Z < 0 ? Z/16 - 1 : Z/16);
		for (int i = x - radius; i < x + radius; i++) {
			for (int j = z - radius; j < z + radius; j++) {
				chunks.add(w.getChunkAt(i, j));
			}
		}
		return chunks;
	}
	
	public static List<Entity> getEntitiesInChunkFilter1(Chunk c) {
		List<Entity> entities = new ArrayList<Entity>();
		for (Entity e : c.getEntities()) {
			if (!(e instanceof HumanEntity ||  e instanceof Item ||  e instanceof Animals ||  e instanceof ExperienceOrb
				|| e instanceof Minecart ||  e instanceof Boat || e instanceof Projectile))
				entities.add(e);
		}
		return entities;
	}
}
