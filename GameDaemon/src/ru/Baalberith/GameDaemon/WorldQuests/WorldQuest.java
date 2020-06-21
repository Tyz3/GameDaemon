package ru.Baalberith.GameDaemon.WorldQuests;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import com.gmail.filoghost.holograms.api.Hologram;
import com.gmail.filoghost.holograms.api.HolographicDisplaysAPI;

import ru.Baalberith.GameDaemon.GD;
import ru.Baalberith.GameDaemon.GDPlayer;
import ru.Baalberith.GameDaemon.CloudMail.MailboxAPI;
import ru.Baalberith.GameDaemon.Linkage.HDAPI;
import ru.Baalberith.GameDaemon.Utils.Utils;

public abstract class WorldQuest {
	
	public enum WorldQuestType {
		supplier, miner, artisan, mercenary;
	}
	
	protected WorldQuestType type;
	protected String name;
	protected String id;
	
	protected int duration;
	protected Hologram hologram;
	protected Location hologramLocation;
	
	protected List<ItemStack> rewards = new ArrayList<ItemStack>();
	protected List<String> description = new ArrayList<String>();
	
	protected int topSize;
	protected int topPlaces;

	protected List<EllipsoidArea> areas = new ArrayList<EllipsoidArea>();
	protected Filter filter = new Filter();
	
	// variable data
	public boolean using = false;
	public long expiresMillis = 0; // Нужно для контроля времени действия квеста.
	
	private Map<String, Long> scores = new LinkedHashMap<String, Long>(); // Счёт игроков
	
	public abstract void reload(ConfigurationSection c);
	public abstract void updateHologram();
	public abstract void giveRewardsViaMail();
	
	public void clear() {
		rewards.clear();
		description.clear();
		areas.clear();
		filter.clearFilters();
		scores.clear();
	}
	
	public void restoreDefaultState() {
		using = false;
		expiresMillis = 0;
		scores.clear();
	}
	
	public void startUsing() {
		if (expiresMillis != 0) return;
		using = true;
		expiresMillis = System.currentTimeMillis() + duration*1000;
	}
	
	public void createHologram() {
		if (!HDAPI.check() || hologramLocation == null) return;
		hologram = HolographicDisplaysAPI.createHologram(GD.inst, hologramLocation);
	}
	
	public void deleteHologram() {
		if (hologram == null) return;
		hologram.delete();
		hologram = null;
	}
	
	public void rebuildHologram() {
		deleteHologram();
		createHologram();
		updateHologram();
	}

	public String getName() {
		return name;
	}
	
	public String getId() {
		return id;
	}

	public int getDuration() {
		return duration;
	}

	public WorldQuestType getType() {
		return type;
	}
	
	public void giveRewards(GDPlayer p) {
		p.giveItems(rewards);
	}
	
	public void giveRewards(String receiver) {
		MailboxAPI.sendBoxLetter("WorldQuests", receiver, Message.rewardMailMessage.get(), rewards);
		GDPlayer p = GD.getGDPlayer(receiver);
		if (p == null || !p.isOnline()) return;
		Message.rewardReceived.send(p);
	}

	public List<String> getDescription() {
		return description;
	}
	
	public boolean isExpired() {
		return expiresMillis < System.currentTimeMillis();
	}
	
	// Модули настройки условий квестов.
	
	// Фильтрация неправильных крафтов.
	public class Filter {
		
		private Map<String, Integer> filters = new HashMap<String, Integer>();
		
		public void addFilter(String key, int value) {
			filters.put(key, value);
		}
		
		public void clearFilters() {
			filters.clear();
		}
		
		public boolean check(String key, int value) {
			return filters.containsKey(key) ? value >= filters.get(key) : false;
		}
		
		public Map<String, Integer> getFilters() {
			return filters;
		}
	}
	
	public boolean checkFilter(String key, int value) {
		return filter.check(key, value);
	}
	
	public void addFilter(String key, int value) {
		filter.addFilter(key, value);
	}
	
	public void removeFilter(String key) {
		filter.filters.remove(key);
	}
	
	public void clearFilters() {
		filter.clearFilters();
	}
	
	public boolean hasFilters() {
		return !filter.filters.isEmpty();
	}
	
	// Проверка нахождения в нужно территории (области).
	public boolean containsInArea(Location point) {
		for (EllipsoidArea area : this.areas) {
			if (!area.containsPoint(point)) return false;
		}
		return true;
	}
	
	public void addArea(Location center, int width, int length, int height) {
		areas.add(new EllipsoidArea(center, width, length, height));
	}
	
	public void addArea(Location center) {
		addArea(center, 6, 6, 6);
	}
	
	public void removeArea(int index) {
		if (index >= areas.size()) return;
		areas.remove(index);
	}
	
	public boolean hasAreas() {
		return !areas.isEmpty();
	}
	
	public void clearAreas() {
		areas.clear();
	}
	
	// Работа счётом игроков.
	
	public void increaseScore(String name, long value) {
		long score = scores.containsKey(name) ? scores.get(name) : 0;
		scores.put(name, score + value);
		scores = Utils.sortByValue(scores, true);
	}
	
	public Map<String, Long> getScores() {
		return scores;
	}
	
	public void setScores(Map<String, Long> scores) {
		this.scores = scores;
	}
}
