package ru.Baalberith.GameDaemon.WorldQuests.Quests;

import java.util.Set;
import java.util.Map.Entry;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import ru.Baalberith.GameDaemon.Utils.ItemDaemon;
import ru.Baalberith.GameDaemon.Utils.LocationManager;
import ru.Baalberith.GameDaemon.Utils.MathOperation;
import ru.Baalberith.GameDaemon.Utils.ThreadDaemon;
import ru.Baalberith.GameDaemon.Utils.Utils;
import ru.Baalberith.GameDaemon.WorldQuests.Message;
import ru.Baalberith.GameDaemon.WorldQuests.WorldQuest;

public class ArtisanQuest extends WorldQuest {
	
	@Override
	public void reload(ConfigurationSection c) {
		clear();
		
		// Общие настройки
		this.id = c.getName();
		this.type = WorldQuestType.artisan;
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
		
		Set<String> keys = c.getConfigurationSection("filters").getKeys(false);
		for (String k : keys) {
			String key = c.getString("filters."+k+".handicraft");
			if (key == null) continue;
			int value = c.getInt("filters."+k+".stage", 0);
			addFilter(key, value);
		}
	}
	
	@Override
	public void updateHologram() {
		if (hologram == null) return;

		hologram.clearLines();
		hologram.addLine(Message.artisan_hologram_title.replace("{name}", name).get());
		hologram.addLine(MathOperation.makeTimeToString(Message.artisan_hologram_duration.get(), expiresMillis-System.currentTimeMillis()));
		description.forEach(desc -> hologram.addLine(desc));
		hologram.addLine(Message.artisan_hologram_topTitle.replace("{size}", topSize).get());
		int currPlace = 1;
		for (Entry<String, Long> e : Utils.sortByValue(getScores(), true, topSize).entrySet()) {
			String line = (currPlace <= topPlaces ? Message.artisan_hologram_topPrize : Message.artisan_hologram_topRaw)
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
