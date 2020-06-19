package ru.Baalberith.GameDaemon.PVPRating;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import ru.Baalberith.GameDaemon.ConfigsDaemon;

public class Rewards {
	
	private ConfigurationSection r;
	public List<Reward> rewards = new ArrayList<Reward>();
	
	public static TopEngine engine = new TopEngine();
	public static Rewards inst;
	
	public Rewards() {
		inst = this;
	}
	
	public void reload() {
		r = ConfigsDaemon.pvpRewardsConfig.get();
		rewards.clear();
		engine.reload();
		
		load();
	}
	
	private void load() {
		for (String k : r.getKeys(false)) {
			
			String item = r.getString(k+".item");
			if (item == null) continue;
			List<String> lore = r.getStringList(k+".lore");
			if (lore == null) continue;
			List<String> enchs = r.getStringList(k+".enchants");
			if (enchs == null) continue;
			
			rewards.add(new Reward(item, lore, enchs));
		}
		
		Bukkit.getLogger().info("[TRPGRating] Loaded "+rewards.size()+" reward items for pvp-rating.");
	}
	
	public List<ItemStack> getItems() {
		List<ItemStack> items = new ArrayList<ItemStack>();
		rewards.forEach(r -> items.add(r.getItem()));
		return items;
	}
	
}
