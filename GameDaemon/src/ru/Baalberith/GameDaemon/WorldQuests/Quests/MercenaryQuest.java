package ru.Baalberith.GameDaemon.WorldQuests.Quests;

import java.util.Set;
import java.util.Map.Entry;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import ru.Baalberith.GameDaemon.Utils.ItemDaemon;
import ru.Baalberith.GameDaemon.Utils.LocationManager;
import ru.Baalberith.GameDaemon.Utils.MathOperation;
import ru.Baalberith.GameDaemon.Utils.ThreadDaemon;
import ru.Baalberith.GameDaemon.Utils.Utils;
import ru.Baalberith.GameDaemon.WorldQuests.Message;
import ru.Baalberith.GameDaemon.WorldQuests.WorldQuest;

public class MercenaryQuest extends WorldQuest {
	
	@Override
	public void reload(ConfigurationSection c) {
		clear();
		
		// Общие настройки
		this.id = c.getName();
		this.type = WorldQuestType.mercenary;
		this.name = c.getString("name", Message.supplier_name.get()); 
		this.hologramLocation = LocationManager.deserializeLocation(c.getString("hologram"));
		this.duration = c.getInt("duration", 420);
		
		for (String src : c.getStringList("rewards")) {
			ItemStack reward = ItemDaemon.deSerializeItem(src);
			this.rewards.add(reward);
		}
		
		for (String src : c.getStringList("description")) {
			this.description.add(src.replace("&", "§"));
		}
		
		// Настройки этого квеста
		this.topSize = c.getInt("topSize", 10);
		this.topPlaces = c.getInt("topPlaces", 3);
		
		Set<String> areas = c.getConfigurationSection("areas").getKeys(false);
		for (String area : areas) {
			Location center = LocationManager.deserializeLocation(c.getString("areas."+area+".center"));
			if (center == null) continue;
			int diameter = c.getInt("areas."+area+".diameter", -1);
			int height = c.getInt("areas."+area+".height", -1);
			if (diameter == -1 || height == -1) continue;
			
			addArea(center, diameter, diameter, height);
		}
		
	}

	@Override
	public void updateHologram() {
		if (hologram == null) return;
		
		hologram.clearLines();
		hologram.addLine(Message.mercenary_hologram_title.replace("{name}", name).get());
		hologram.addLine(MathOperation.makeTimeToString(Message.mercenary_hologram_duration.get(), expiresMillis-System.currentTimeMillis()));
		description.forEach(desc -> hologram.addLine(desc));
		hologram.addLine(Message.mercenary_hologram_topTitle.replace("{size}", topSize).get());
		int currPlace = 1;
		for (Entry<String, Long> e : Utils.sortByValue(getScores(), true, topSize).entrySet()) {
			String line = (currPlace <= topPlaces ? Message.mercenary_hologram_topPrize : Message.mercenary_hologram_topRaw)
					.replace("{place}", currPlace)
					.replace("{player}", e.getKey())
					.replace("{score}", e.getValue()).get();
			currPlace++;
			hologram.addLine(line);
		}

		ThreadDaemon.sync(() -> hologram.update());
	}

	@Override
	public void giveRewardsViaMail() {
		// Выдаём награду всем игрокам попавшим в топ игроков, отправкой предметов в почту.
		for (Entry<String, Long> e : Utils.sortByValue(getScores(), true, topSize).entrySet()) {
			giveRewards(e.getKey());
		}
		
	}

}
