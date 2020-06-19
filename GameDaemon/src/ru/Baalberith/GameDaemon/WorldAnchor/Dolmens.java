package ru.Baalberith.GameDaemon.WorldAnchor;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import ru.Baalberith.GameDaemon.ConfigsDaemon;
import ru.Baalberith.GameDaemon.Utils.ItemDaemon;
import ru.Baalberith.GameDaemon.Utils.LocationManager;

public class Dolmens {
	
	private List<Dolmen> dolmens;
	
	public Dolmens() {
		dolmens = new ArrayList<Dolmen>();
	}
	
	public void reload() {
		dolmens.clear();
		
		ConfigurationSection a = ConfigsDaemon.anchorsConfig.get();
		Set<String> keys = a.getKeys(false);
		for (String k : keys) {

			String title = a.getString(k+".title");
			if (title == null) continue;
			String startIn = a.getString(k+".startIn");
			if (startIn == null) continue;
			Location loc = LocationManager.deserializeLocation(a.getString(k+".center"));
			if (loc == null) continue;
			
			if (!a.contains(k+".phases")) continue;
			List<Phase> phases = new ArrayList<Phase>();
			Set<String> pKeys = a.getConfigurationSection(k+".phases").getKeys(false);
			for (String p : pKeys) {
				boolean hasBoss = a.getBoolean(k+".phases."+p+".hasBoss", false);
				int maxDuration = a.getInt(k+".phases."+p+".maxDuration", 120);
				List<String> commands = a.getStringList(k+".phases."+p+".commands");
				if (commands == null) continue;
				List<ItemStack> rewards = new ArrayList<ItemStack>();
				List<String> list = a.getStringList(k+".phases."+p+".rewards");
				if (list != null && !list.isEmpty()) {
					list.forEach(s -> rewards.add(ItemDaemon.deSerializeItem(s)));
				}
				phases.add(new Phase(p, commands, maxDuration, hasBoss, rewards, loc));
			}
			if (phases.size() == 0) continue;
			
			dolmens.add(new Dolmen(title, loc, startIn, phases));
		}
		Bukkit.getLogger().info("[WorldAnchors] Loaded "+dolmens.size()+" dolmens.");
	}
	
	public List<Dolmen> getDolmens() {
		return dolmens;
	}
	
	public Dolmen getTimelyDolmen(String format) {
		for (Dolmen dolmen : dolmens) {
			if (dolmen.getStartIn().contains(format)) {
//				Bukkit.getLogger().info("[WorldAnchors] Getting timely dolmen '"+dolmen.getTitle()+"' which starts on "+format+".");
				return dolmen;
			}
		}
		return null;
	}
}
