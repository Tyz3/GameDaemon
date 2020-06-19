package ru.Baalberith.GameDaemon.StuffSynchronizer;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import ru.Baalberith.GameDaemon.ConfigsDaemon;
import ru.Baalberith.GameDaemon.GD;
import ru.Baalberith.GameDaemon.StuffSynchronizer.ReplaceItem.ReplaceItemType;
import ru.Baalberith.GameDaemon.Utils.ItemDaemon;

public class SynchronizeEngine {
	
	
	private ConfigurationSection c;
	private List<ReplaceItem> replaceItems = new ArrayList<ReplaceItem>();
	
	public static SynchronizeEngine inst;
	
	
	public SynchronizeEngine() {
		inst = this;
		Bukkit.getPluginManager().registerEvents(new SynchronizeHandler(), GD.inst);
	}
	
	public void reload() {
		if (!ConfigsDaemon.stuffSynchronize) return;
		replaceItems.clear();
		c = ConfigsDaemon.synchronizerConfig.get();
		
		Set<String> keys = c.getKeys(false);
		for (String k : keys) {
			try {
				ReplaceItemType type = ReplaceItemType.valueOf(c.getString(k+".type", "replaceAll"));
				if (type == ReplaceItemType.addAll) {
					// FROM
					Material fromMaterial = Material.matchMaterial(c.getString(k+".from.material"));
					if (fromMaterial == null) continue;
					short fromDurability = (short) c.getInt(k+".from.data", -1);
					String fromDisplayName = c.getString(k+".from.displayName");
					List<String> fromLore = c.getStringList(k+".from.lore");
					ItemDaemon.setColorCodes(fromLore);
					Map<Enchantment, Integer> fromEnchantments = ItemDaemon.listToEnchanments(c.getStringList(k+".from.enchantments"));
					
					// TO
					List<String> toLore = c.getStringList(k+".to.lore");
					ItemDaemon.setColorCodes(toLore);
					Map<Enchantment, Integer> toEnchantments = ItemDaemon.listToEnchanments(c.getStringList(k+".to.enchantments"));
					if (toLore == null && toEnchantments == null) continue;
					replaceItems.add(new ReplaceItem(type, fromMaterial, fromDurability, fromDisplayName, fromLore, fromEnchantments, null, (short) 0, null, toLore, toEnchantments));
					
				} else if (type == ReplaceItemType.replaceAll) {
					// FROM
					Material fromMaterial = Material.matchMaterial(c.getString(k+".from.material"));
					if (fromMaterial == null) continue;
					short fromDurability = (short) c.getInt(k+".from.data", -1);
					String fromDisplayName = c.getString(k+".from.displayName");
					List<String> fromLore = c.getStringList(k+".from.lore");
					ItemDaemon.setColorCodes(fromLore);
					Map<Enchantment, Integer> fromEnchantments = ItemDaemon.listToEnchanments(c.getStringList(k+".from.enchantments"));
					
					// TO
					Material toMaterial = Material.matchMaterial(c.getString(k+".to.material"));
					short toDurability = (short) c.getInt(k+".to.data", -1);
					String toDisplayName = c.getString(k+".to.displayName");
					List<String> toLore = c.getStringList(k+".to.lore");
					ItemDaemon.setColorCodes(toLore);
					Map<Enchantment, Integer> toEnchantments = ItemDaemon.listToEnchanments(c.getStringList(k+".to.enchantments"));
					if (toMaterial == null && toDisplayName == null && toLore == null && toEnchantments == null) continue;
					replaceItems.add(new ReplaceItem(type, fromMaterial, fromDurability, fromDisplayName, fromLore, fromEnchantments, toMaterial, toDurability, toDisplayName, toLore, toEnchantments));
					
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		GD.log("[StuffSynchronizer] Loaded "+replaceItems.size()+" synchronize items.");
	}
	
	public boolean processChange(ItemStack... items) {
		if (items == null || items.length == 0) return false;
		long time1 = new Date().getTime();
		boolean changed = false;
		for (ItemStack i : items) {
			if (i == null) continue;
			for (ReplaceItem ri : replaceItems) {
				if (!ri.compare(i)) continue;
				ri.changeItem(i);
				changed = true;
			}
		}
		long time2 = new Date().getTime() - time1;
		GD.log("StuffSynchronizer.SynchronizeEngine.processChange(), ["+time2+" ms]");
		return changed;
	}
}
