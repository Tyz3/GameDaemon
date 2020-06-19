package ru.Baalberith.GameDaemon.LightLevelingSystem.BasicTraits;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import ru.Baalberith.GameDaemon.GDPlayer;
import ru.Baalberith.GameDaemon.LightLevelingSystem.Message;
import ru.Baalberith.GameDaemon.LightLevelingSystem.Trait;

public class Health extends Trait {

	private int healthBoost;
	
	public Health(TraitType type, int cost, int availableLevel, int threshold, boolean immutable, boolean autoReward,
			int autoRewardAmount, int autoRewardEvery, int position, ItemStack icon, String displayName) {
		super(type, cost, availableLevel, threshold, immutable, autoReward, autoRewardAmount, autoRewardEvery, position, icon, displayName);
		
	}

	@Override
	public void reload(ConfigurationSection c) {
		healthBoost = c.getInt("healthBoost", 1);
	}
	
	public int getHealth(GDPlayer p) {
		return healthBoost * p.getTraitLevel(type);
	}

	@Override
	public ItemStack getIcon(GDPlayer p) {
		List<String> lore = new ArrayList<String>();
		for (String s : icon.getItemMeta().getLore()) {
			lore.add(s.replace("{currentBonus}", String.valueOf(getHealth(p)))
				.replace("{boostValue}", String.valueOf(healthBoost))
				.replace("{ownLevel}", String.valueOf(p.getTraitLevel(type))));
		}
		if (p.hasLevel(availableLevel))
			lore.add(Message.description_common_available_yes.get());
		else lore.add(Message.description_common_available_no
				.replace("{requiredLvl}", String.valueOf(availableLevel))
				.replace("{currentLvl}", String.valueOf(p.getLevel()))
				.get());
		
		ItemStack item = icon.clone();
		ItemMeta meta = item.getItemMeta();
		meta.setLore(lore);
		item.setItemMeta(meta);
		return item;
	}

}
