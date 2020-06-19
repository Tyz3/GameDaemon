package ru.Baalberith.GameDaemon.Clans.Dungeons;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import com.gmail.filoghost.holograms.api.Hologram;
import com.gmail.filoghost.holograms.api.HolographicDisplaysAPI;

import ru.Baalberith.GameDaemon.GD;
import ru.Baalberith.GameDaemon.GDPlayer;
import ru.Baalberith.GameDaemon.Linkage.HDAPI;
import ru.Baalberith.GameDaemon.Utils.LocationManager;
import ru.Baalberith.GameDaemon.Utils.MathOperation;
import ru.Baalberith.GameDaemon.Utils.ThreadDaemon;
import ru.Baalberith.GameDaemon.Utils.Utils;

public class Dungeon {
	
	private String id;
	private String name;
	private boolean enabled;
	private int timeTopSize;
	
	private Location portal;
	private Location returnSpawn;
	private Location hologramLoc;
	private Location firstLayerSpawn;
	
	private int heightOffset;
	
	private ItemStack key;
	private ItemStack reward;
	private int joinCooldown;
	private String exitCommand;
	
	private List<Layer> layers = new ArrayList<Layer>();
	private List<Level> levels = new ArrayList<Level>();
	
	private Hologram hologram;
	private Map<String, Long> topTime = new LinkedHashMap<String, Long>();
	
	public Dungeon(boolean enabled, String id, String name, String portalSrc, String returnSpawnSrc, String hologramLocSrc,
			ItemStack key, ItemStack reward, int joinCooldown, int timeTopSize,
			List<Layer> layers, List<Level> levels, Map<String, Long> topTime, String spawnSrc, int heightOffset, String exitCommand) {
		this.enabled = enabled;
		this.timeTopSize = timeTopSize;
		this.id = id;
		this.name = name;
		this.portal = LocationManager.deserializeLocation(portalSrc);
		this.returnSpawn = LocationManager.deserializeLocation(returnSpawnSrc);
		this.hologramLoc = LocationManager.deserializeLocation(hologramLocSrc);
		this.firstLayerSpawn = LocationManager.deserializeLocation(spawnSrc);
		this.key = key;
		this.reward = reward;
		this.joinCooldown = joinCooldown;
		this.heightOffset = heightOffset;
		this.exitCommand = exitCommand;
		
		this.layers = layers == null ? this.layers : layers;
		this.levels = levels == null ? this.levels : levels;
		this.topTime = topTime == null ? this.topTime : topTime;
		
		createHologram();
		updateHologram();
	}
	
	private void createHologram() {
		if (!HDAPI.check()) return;
		if (hologramLoc == null || timeTopSize == -1) return;
		hologram = HolographicDisplaysAPI.createHologram(GD.inst, hologramLoc);
	}
	
	public void updateHologram() {
		if (hologram == null) return;
		if (timeTopSize == -1) {
			deleteHologram();
			return;
		}
		hologram.clearLines();
		hologram.addLine(Message.hologram_title.replace("{name}", name).get().concat(enabled?"":" §c§lOFF"));
		hologram.addLine(Message.hologram_status.replace("{now}", busyLayers()).replace("{max}", getEnabledLayers()).get());
		if (timeTopSize != 0) {
			hologram.addLine("");
			hologram.addLine(Message.hologram_topTitle.replace("{size}", timeTopSize).get());
			for (Entry<String, Long> top : topTime.entrySet()) {
				long sec = top.getValue() / 1000;
				long ms = top.getValue() % 1000;
				hologram.addLine(Message.hologram_topFormat.replace("{player}", top.getKey()).replace("{time}", sec).replace("{millis}", ms).get());
			}
		}
		ThreadDaemon.sync(() -> hologram.update());
	}
	
	public void setExitCommand(String exitCommand) {
		this.exitCommand = exitCommand;
	}
	
	public String getExitCommand() {
		return exitCommand;
	}
	
	public void runExitCommand(String player) {
		if (exitCommand == null) return;
		GD.dispatchCommand(exitCommand.replace("{player}", player));
	}
	
	public void setTopSize(int topSize) {
		timeTopSize = topSize;
		updateHologram();
	}
	
	public void deleteHologram() {
		if (hologram != null) {
			hologram.delete();
			hologram = null;
		}
	}
	
	public void setJoinCooldown(int joinCooldown) {
		this.joinCooldown = joinCooldown;
	}
	
	public void removeWayback(int index) {
		for (Layer layer : layers) {
			layer.removeWayback(index);
		}
	}
	
	public void setKey(ItemStack item) {
		key = item;
	}
	
	public void setReward(ItemStack item) {
		reward = item;
	}
	
	public Layer getLayerByNumber(int number) {
		for (Layer layer : layers) {
			if (layer.number() == number) return layer;
		}
		return null;
	}
	
	public void setHeightOffset(int heightOffset) {
		this.heightOffset = heightOffset;
	}
	
	public Location getFirstLayerSpawn() {
		return firstLayerSpawn;
	}
	
	public void setName(String name) {
		if (name.length() > 32) name = name.substring(0, 32);
		this.name = name;
	}
	
	public int getHeightOffset() {
		return heightOffset;
	}
	
	public boolean hasLevel(int number) {
		for (Level l : levels) {
			if (l.number() == number) return true;
		}
		return false;
	}
	
	public boolean hasLayer(int number) {
		for (Layer l : layers) {
			if (l.number() == number) return true;
		}
		return false;
	}
	
	public void setFirstLayerSpawn(Location loc) {
		firstLayerSpawn = loc;
		for (Layer layer : layers) {
			layer.setSpawnLocation(loc.clone());
		}
	}
	
	public void addRegion(String world, String x1, String y1, String z1, String x2, String y2, String z2) {
		// world x1 z1 x2 z2 y1 y2
		String locSrc = world.concat(" ").concat(x1).concat(" ").concat(y1).concat(" ").concat(z1).concat(" ").concat(x2).concat(" ").concat(y2).concat(" ").concat(z2);
		for (Layer layer : layers) {
			layer.newRegion(locSrc);
		}
	}
	
	public void removeRegion(int index) {
		for (Layer layer : layers) {
			layer.removeRegion(index);
		}
	}
	
	public void setReturnSpawn(Location loc) {
		returnSpawn = loc;
	}
	
	public void setPortal(Location loc) {
		portal = loc;
	}
	
	public List<Layer> getLayers() {
		return layers;
	}
	
	public Layer getFirstLayer() {
		for (Layer layer : layers) {
			if (layer.number() == 1) return layer;
		}
		return layers.get(0);
	}
	
	public List<Layer.Region> getRegionsByFirstLayer() {
		return getFirstLayer().getRegions();
	}
	
	public List<Level> getLevels() {
		return levels;
	}
	
	public void addRunTimeToTop(String ownerName, long remTime, int level) {
		StringBuilder sb = new StringBuilder();
		sb.append("(").append(level).append(")§f ").append(ownerName);
		String key = sb.toString();
		if (topTime.containsKey(key)) {
			long time = topTime.get(key);
			if (time < remTime) remTime = time;
		}
		topTime.put(key, remTime);
		topTime = Utils.sortByValue(topTime, false, timeTopSize);
	}
	
	public void saveTopTimeToFile() {
		DungeonEngine.dungeonStorage.set("dungeons.".concat(id), null);
		for (Entry<String, Long> e : topTime.entrySet()) {
			DungeonEngine.dungeonStorage.set("dungeons.".concat(id).concat(".").concat(e.getKey()), e.getValue());
		}
		DungeonEngine.dungeonStorage.save();
	}
	
	public void giveReward(List<GDPlayer> players, int level) {
		if (reward == null) return;
		int gived = 0;
		for (GDPlayer p : players) {
			p.giveItem(reward);
			Message.rewarded.replace("{name}", name).send(p);
			gived++;
		}
		
		if (gived < level) {
			players.get(0).giveItem(reward, level-gived);
			Message.rewardedFromAbsent.replace("{amount}", level-gived).get();
		}
	}
	
	private boolean hasEnabledLayers() {
		for (Layer layer : layers) {
			if (layer.isEnabled()) return true;
		}
		return false;
	}
	
	private boolean hasEnabledLevels() {
		for (Level level : levels) {
			if (level.isEnabled()) return true;
		}
		return false;
	}
	
	public boolean on() {
		if (!hasEnabledLayers() || !hasEnabledLevels() || heightOffset == -1 || firstLayerSpawn == null ||
				portal == null || returnSpawn == null) return false;
		
		return (enabled = true);
	}
	
	public void off() {
		enabled = false;
	}
	
	public boolean isEnabled() {
		return enabled;
	}
	
	public int getTopSize() {
		return timeTopSize;
	}
	
	public int busyLayers() {
		int amount = 0;
		for (Layer layer : layers) {
			if (layer.isBusy()) amount++;
		}
		return amount;
	}
	
	public Location getHologramLocation() {
		return hologramLoc;
	}
	
	public void setHologramLocation(Location loc) {
		hologramLoc = loc;
	}
	
	public Location getPortal() {
		return portal;
	}
	
	public boolean isPortal(Location loc) {
		return portal.getWorld().equals(loc.getWorld()) && MathOperation.distance3D(portal, loc) == 0;
	}
	
	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}
	
	public ItemStack getKey() {
		return key;
	}
	
	public ItemStack getReward() {
		return reward;
	}

	public int getJoinCooldown() {
		return joinCooldown;
	}
	
	public Layer getFreeLayer() {
		for (Layer layer : layers) {
			if (layer.isEnabled() && !layer.isBusy()) return layer;
		}
		return null;
	}
	
	public int getEnabledLayers() {
		int amount = 0;
		for (Layer layer : layers) {
			if (layer.isEnabled()) amount++;
		}
		return amount;
	}
	
	public Location getReturnLocation() {
		return returnSpawn;
	}
	
	public boolean hasKey(GDPlayer p) {
		return key == null || p.hasItem(key) != -1;
	}
	
	public boolean hasDistance(GDPlayer p) {
		return MathOperation.distance3D(portal, p.getLocation()) <= DungeonEngine.distance;
	}
	
	public Level getLevelByPartySize(int partySize) {
		for (Level lvl : levels) {
			if (lvl.number() == partySize) return lvl;
		}
		return null;
	}
	
}
