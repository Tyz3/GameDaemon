package ru.Baalberith.GameDaemon.LightLevelingSystem.BasicTraits;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import ru.Baalberith.GameDaemon.GDPlayer;
import ru.Baalberith.GameDaemon.LightLevelingSystem.Message;
import ru.Baalberith.GameDaemon.LightLevelingSystem.Trait;
import ru.Baalberith.GameDaemon.LightLevelingSystem.Traits;

public class Brief extends Trait {

	public Brief(TraitType type, int cost, int availableLevel, int threshold, boolean immutable, boolean autoReward,
			int autoRewardAmount, int autoRewardEvery, int position, ItemStack icon, String displayName) {
		super(type, cost, availableLevel, threshold, immutable, autoReward, autoRewardAmount, autoRewardEvery, position, icon,
				displayName);
	}

	@Override
	public void reload(ConfigurationSection c) {
	}

	@Override
	public ItemStack getIcon(GDPlayer p) {
		List<String> lore = new ArrayList<String>();
		
		lore.addAll(Arrays.asList(Message.description_brief_common
				.replace("{freePoints}", String.valueOf(p.getFreeTraitPoints()))
				.replace("{symbol}", Message.symbol.get())
				.replace("{powerLevel}", String.valueOf(p.getPowerLevel()))
				.gets()));
		
		for (Entry<TraitType, Trait> t : Traits.traits.entrySet()) {
			if (t.getKey() == TraitType.BRIEF) continue;
			lore.add(Message.description_brief_trait
					.replace("{traitName}", t.getValue().getDisplayName())
					.replace("{ownLevel}", String.valueOf(p.getTraitLevel(type)))
					.replace("{threshold}", String.valueOf(t.getValue().getThreshold()))
					.get());
		}
		
		ItemStack item = icon.clone();
		ItemMeta meta = item.getItemMeta();
		meta.setLore(lore);
		item.setItemMeta(meta);
		return item;
	}

}
