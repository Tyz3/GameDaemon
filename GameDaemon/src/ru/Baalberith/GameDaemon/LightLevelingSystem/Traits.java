package ru.Baalberith.GameDaemon.LightLevelingSystem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import ru.Baalberith.GameDaemon.ConfigsDaemon;
import ru.Baalberith.GameDaemon.GD;
import ru.Baalberith.GameDaemon.LightLevelingSystem.BasicTraits.Attack;
import ru.Baalberith.GameDaemon.LightLevelingSystem.BasicTraits.Brief;
import ru.Baalberith.GameDaemon.LightLevelingSystem.BasicTraits.Defense;
import ru.Baalberith.GameDaemon.LightLevelingSystem.BasicTraits.Health;
import ru.Baalberith.GameDaemon.LightLevelingSystem.BasicTraits.Inspiration;
import ru.Baalberith.GameDaemon.LightLevelingSystem.Trait.TraitType;
import ru.Baalberith.GameDaemon.Utils.ItemDaemon;

public class Traits {
	
	public static HashMap<TraitType, Trait> traits = new HashMap<Trait.TraitType, Trait>();
	
	public static void reload() {
		traits.clear();
		
		ConfigurationSection c = ConfigsDaemon.mainConfig.getConfigurationSection("lightLevelingSystem.options");
		
		Set<String> keys = c.getKeys(false);
		for (String k : keys) {
			try {
				TraitType type = TraitType.valueOf(k.toUpperCase());
				int cost = c.getInt(k+".cost", -1);
				int availableLevel = c.getInt(k+".availableLevel", 0);
				int threshold = c.getInt(k+".threshold", 100);
				boolean immutable = c.getBoolean(k+".immutable", false);
				
				boolean autoReward = c.getBoolean(k+".autoReward.enable", false);
				int autoRewardAmount = c.getInt(k+".autoReward.amount", 1);
				int autoRewardEvery = c.getInt(k+".autoReward.every", LiLSEngine.rewardEvery);
				
				int position = c.getInt(k+".menuPosition");
				
				ItemStack icon = ItemDaemon.fromString(c.getString(k+".icon"));
				if (icon == null) continue;
				String displayName = c.getString(k+".displayName", type.name()).replace("&", "§");
				
				boolean special = c.getBoolean(k+".special", false);
				setName(icon, displayName, special);
				if (!special) 
					setDescription(icon, type, immutable, autoReward, threshold, cost, autoRewardEvery, autoRewardAmount);
				
				
				Trait trait = null;
				switch (type) {
				case ATTACK:
					trait = new Attack(type, cost, availableLevel, threshold, immutable, autoReward, autoRewardAmount, autoRewardEvery, position, icon, displayName);
					break;
				case DEFENSE:
					trait = new Defense(type, cost, availableLevel, threshold, immutable, autoReward, autoRewardAmount, autoRewardEvery, position, icon, displayName);
					break;
				case HEALTH:
					trait = new Health(type, cost, availableLevel, threshold, immutable, autoReward, autoRewardAmount, autoRewardEvery, position, icon, displayName);
					break;
				case INSPIRATION:
					trait = new Inspiration(type, cost, availableLevel, threshold, immutable, autoReward, autoRewardAmount, autoRewardEvery, position, icon, displayName);
					break;
				case BRIEF:
					trait = new Brief(type, cost, availableLevel, threshold, immutable, autoReward, autoRewardAmount, autoRewardEvery, position, icon, displayName);
					break;
				}
				if (trait == null) continue;
				trait.reload(c);
				
				traits.put(type, trait);
				
			} catch (Exception e) {e.printStackTrace();}
		}
		
		GD.log("[LiLS] Loaded "+traits.size()+" trait(s).");
	}
	
	public static Trait getTraitByPosition(int clickedSlot) {
		for (Entry<TraitType, Trait> t : traits.entrySet()) {
			if (t.getValue().position == clickedSlot) return t.getValue();
		}
		return null;
	}
	
	private static void setName(ItemStack item, String displayName, boolean special) {
		ItemMeta meta = item.getItemMeta();
		if (special) meta.setDisplayName(displayName);
		else meta.setDisplayName(Message.description_common_title.get().replace("{title}", displayName));
		item.setItemMeta(meta);
	}
	
	private static void setDescription(ItemStack item, TraitType type, boolean immutable, boolean autoReward, int threshold, int cost, int amount, int every) {
		ItemMeta meta = item.getItemMeta();
		List<String> lore = new ArrayList<String>();
		
		// Ограничения развития навыка.
		lore.add(Message.description_common_level.get().replace("{threshold}", ""+threshold));
		
		// Стоимость навыка
		if (cost == -1)
			lore.add(Message.forbidden.get());
		else lore.add(Message.description_common_cost.get().replace("{cost}", ""+cost).replace("{symbol}", Message.symbol.get()));
		
		// Установка описания навыка как авто-улучшаемый.
		if (autoReward)
			lore.addAll(Arrays.asList(Message.description_common_autoReward.replace("{amount}", ""+amount).replace("{every}", ""+every).gets()));
		
		// Установка кастомного описания каждого навыка.
		switch (type) {
		case ATTACK:
			lore.addAll(Arrays.asList(Message.description_attack.gets()));
			break;
		case DEFENSE:
			lore.addAll(Arrays.asList(Message.description_defense.gets()));
			break;
		case HEALTH:
			lore.addAll(Arrays.asList(Message.description_health.gets()));
			break;
		case INSPIRATION:
			lore.addAll(Arrays.asList(Message.description_inspiration.gets()));
			break;
		default:
			break;
		}
		
		if (immutable) lore.addAll(Arrays.asList(Message.description_common_immutable.gets()));
		lore.add("");
		meta.setLore(lore);
		item.setItemMeta(meta);
	}
	
}



