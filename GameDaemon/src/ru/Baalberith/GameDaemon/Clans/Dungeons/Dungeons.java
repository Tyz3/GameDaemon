package ru.Baalberith.GameDaemon.Clans.Dungeons;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import ru.Baalberith.GameDaemon.ConfigsDaemon;
import ru.Baalberith.GameDaemon.GD;
import ru.Baalberith.GameDaemon.Clans.Dungeons.Layer.Region;
import ru.Baalberith.GameDaemon.Clans.Dungeons.Layer.Wayback;
import ru.Baalberith.GameDaemon.Clans.Dungeons.Game.Sessions;
import ru.Baalberith.GameDaemon.Utils.ItemDaemon;
import ru.Baalberith.GameDaemon.Utils.LocationManager;
import ru.Baalberith.GameDaemon.Utils.ThreadDaemon;
import ru.Baalberith.GameDaemon.Utils.Utils;

public class Dungeons {
	
	public static CopyOnWriteArrayList<Dungeon> dungeons = new CopyOnWriteArrayList<Dungeon>();
	
	private static BukkitTask task;
	
	public static void reload() {
		ThreadDaemon.cancelTask(task);
		task = ThreadDaemon.asyncTimer(() -> saveTopTimeDungeons(), 0, DungeonEngine.saveShedulerTimeSeconds);
		
		clearHolograms();
		dungeons.clear();
		
		ConfigsDaemon.dungeonsConfig.reload();
		ConfigsDaemon.messagesConfig.reload();
		
		ConfigurationSection d = ConfigsDaemon.dungeonsConfig.get();
		ConfigurationSection m = ConfigsDaemon.messagesConfig.getConfigurationSection("dungeons");
		
		Message.load(m, m.getString("dungeons.label", "Dungeons"));
		
		Set<String> keys = d.getKeys(false);
		for (String k : keys) {
			reloadExact(k);
			
		}
	}
	
	public static void reloadExact(String id) {
		ConfigurationSection d = ConfigsDaemon.dungeonsConfig.get();
		
		Dungeon dung = getDungeonById(id);
		if (dung != null) {
			Sessions.closeSessionsWithDungeon(dung);
			dungeons.remove(dung);
			dung.deleteHologram();
		}
		
		String name = d.getString(id+".name", id).replace("&", "§");
		boolean enabled = d.getBoolean(id+".enabled", false);
		
		int timeTopSize = d.getInt(id+".timeTopSize", 0);
		
		String portalSrc = d.getString(id+".portal");
		if (portalSrc == null) enabled = false;
		
		String returnSpawnSrc = d.getString(id+".returnSpawn");
		if (returnSpawnSrc == null) enabled = false;
		
		String hologramSrc = d.getString(id+".hologram");
		
		ItemStack key = ItemDaemon.fromString(d.getString(id+".key"));
		
		ItemStack reward = ItemDaemon.fromString(d.getString(id+".reward"));
		
		String exitCommand = d.getString(id+".exitCommand");
		
		int joinCooldown = d.getInt(id+".joinCooldown", 0);
		int heightOffset = d.getInt(id+".heightOffset", -1);
		if (heightOffset < 0) enabled = false;
		
		List<String> commands = d.getStringList(id+".commands");
		if (commands == null) commands = new ArrayList<String>();
		
		String spawn = d.getString(id+".spawn");
		if (spawn == null) enabled = false;
		
		List<String> regions = d.getStringList(id+".region");
		if (regions == null) regions = new ArrayList<String>();
		
		List<String> waybacks = d.getStringList(id+".wayback");
		if (waybacks == null) waybacks = new ArrayList<String>();
		
		
		List<Layer> layers = new ArrayList<Layer>();
		if (d.contains(id+".layers")) {
			Set<String> lakeys = d.getConfigurationSection(id+".layers").getKeys(false);
			for (String lk : lakeys) {
				boolean lEnabled = d.getBoolean(id+".layers."+lk+".enabled", false);
				layers.add(new Layer(lEnabled, regions, waybacks, spawn, heightOffset, Integer.parseInt(lk)));
			}
		}
		if (layers.isEmpty()) enabled = false;
		
		
		List<Level> levels = new ArrayList<Level>();
		if (d.contains(id+".levels")) {
			Set<String> lvlkeys = d.getConfigurationSection(id+".levels").getKeys(false);
			for (String lk : lvlkeys) {
				boolean lEnabled = d.getBoolean(id+".levels."+lk+".enabled", false);
				int time = d.getInt(id+".levels."+lk+".time", 60);
				levels.add(new Level(lEnabled, commands, Integer.parseInt(lk), time, layers.size(), heightOffset));
			}
		}
		if (levels.isEmpty()) enabled = false;
		
		
		// Чистка файла с данными о топе групп по времени прохождения данжа.
		updateLifeCycleTopTime();
		// Загрузка информации о топе игроков.
		Map<String, Long> topTime = timeTopSize == -1 ? new HashMap<String, Long>() : getTopTimeFromFile(id, timeTopSize);
		
		GD.log("[Dungeons] Loaded dungeon '"+id+"', layers: "+layers.size()+", levels: "+levels.size()+", enabled: "+enabled+".");

		dungeons.add(new Dungeon(enabled, id, name, portalSrc, returnSpawnSrc, hologramSrc, key, reward, joinCooldown, timeTopSize, layers, levels, topTime, spawn, heightOffset, exitCommand));
	}
	
	public static Dungeon createNew(boolean enabled, String id, String name, String portalSrc, String returnSpawnSrc, String hologramLocSrc,
			ItemStack key, ItemStack reward, int joinCooldown, int timeTopSize,
			List<Layer> layers, List<Level> levels, Map<String, Long> topTime, String spawnSrc, int heightOffset, String exitCommand) {
		return new Dungeon(enabled, id, name, portalSrc, returnSpawnSrc, hologramLocSrc, key, reward, joinCooldown, timeTopSize, layers, levels, topTime, spawnSrc, heightOffset, exitCommand);
	}
	
	public static Level createNewLevel(boolean enabled, List<String> commands, int levelNumber, int time, int layersAmount, int heightOffset) {
		return new Level(enabled, commands, levelNumber, time, layersAmount, heightOffset);
	}
	
	public static Layer createNewLayer(boolean enabled, List<String> regions, List<String> waybacks, String spawn, int heightOffset, int layerNumber) {
		return new Layer(enabled, regions, waybacks, spawn, heightOffset, layerNumber);
	}
	
	public static void saveTopTimeDungeons() {
		dungeons.stream().forEach(d -> d.saveTopTimeToFile());
	}
	
	public static void saveDungeonsToFile() {
		dungeons.stream().forEach(dung -> saveDungeonToFile(dung));
	}
	
	public static void saveDungeonToFile(Dungeon d) {
		FileConfiguration file = ConfigsDaemon.dungeonsConfig.get();
		
		String id = d.getId();
		file.set(id+".name", d.getName().replace("§", "&"));
		file.set(id+".enabled", d.isEnabled());
		
		file.set(id+".portal", LocationManager.serializeLocation(d.getPortal()));
		file.set(id+".returnSpawn", LocationManager.serializeLocation(d.getReturnLocation()));
		file.set(id+".hologram", LocationManager.serializeLocation(d.getHologramLocation()));
		file.set(id+".spawn", LocationManager.serializeLocation(d.getFirstLayerSpawn()));
		
		file.set(id+".key", ItemDaemon.toString(d.getKey()));
		file.set(id+".reward", ItemDaemon.toString(d.getReward()));
		
		file.set(id+".joinCooldown", d.getJoinCooldown());
		file.set(id+".heightOffset", d.getHeightOffset());
		file.set(id+".timeTopSize", d.getTopSize());
		file.set(id+".exitCommand", d.getExitCommand());
		
		if (!d.getLayers().isEmpty()) {
			Layer fl = d.getFirstLayer();
			
			List<String> regions = new ArrayList<String>();
			for (Region reg : fl.getRegions()) {
				regions.add(reg.toString());
			}
			file.set(id+".region", regions);
			
			List<String> waybacks = new ArrayList<String>();
			for (Wayback wb : fl.getWaybacks()) {
				waybacks.add(wb.toString());
			}
			file.set(id+".wayback", waybacks);
		}
		
		file.set(id+".levels", null);
		for (Level level : d.getLevels()) {
			file.set(id+".levels."+level.number()+".enabled", level.isEnabled());
			file.set(id+".levels."+level.number()+".time", level.getTime());
		}
		
		file.set(id+".layers", null);
		for (Layer layer : d.getLayers()) {
			file.set(id+".layers."+layer.number()+".enabled", layer.isEnabled());
		}
		
		ConfigsDaemon.dungeonsConfig.save();
	}
	
	public static int getAmount() {
		return dungeons.size();
	}
	
	public static void removeDungeon(Dungeon dung) {
		dungeons.remove(dung);
		FileConfiguration file = ConfigsDaemon.dungeonsConfig.get();
		
		dung.deleteHologram();
		Sessions.closeSessionsWithDungeon(dung);
		
		file.set(dung.getId(), null);
	}
	
	public static void clearHolograms() {
		dungeons.forEach(d -> d.deleteHologram());
	}
	
	public static Dungeon getDungeonByName(String name) {
		for (Dungeon dungeon : dungeons) {
			if (dungeon.getName().equalsIgnoreCase(name)) return dungeon;
		}
		return null;
	}
	
	public static Dungeon getDungeonById(String id) {
		for (Dungeon dungeon : dungeons) {
			if (dungeon.getId().equals(id)) return dungeon;
		}
		return null;
	}
	
	private static Map<String, Long> getTopTimeFromFile(String id, int timeTopSize) {
		Map<String, Long> topTime = new LinkedHashMap<String, Long>();
		HashMap<String, Object> hm = DungeonEngine.dungeonStorage.getMap("dungeons."+id);
		
		if (hm == null) return topTime;
		
		for (Entry<String, Object> e : hm.entrySet()) {
			topTime.put(e.getKey(), (Long) e.getValue());
		}
		return (topTime = Utils.sortByValue(topTime, false, timeTopSize));
	}
	
	private static void updateLifeCycleTopTime() {
		int updateWeek = DungeonEngine.dungeonStorage.getInt("updateWeek", -1);
		Calendar calendar = Calendar.getInstance();
		int currentWeek = calendar.get(Calendar.WEEK_OF_YEAR);
		if (updateWeek == currentWeek) {
			;
		} else if (updateWeek == -1) {
			DungeonEngine.dungeonStorage.set("updateWeek", currentWeek);
		} else if (updateWeek != currentWeek) {
			DungeonEngine.dungeonStorage.set("updateWeek", currentWeek);
			DungeonEngine.dungeonStorage.set("dungeons", null);
		}
	}
}
