package ru.Baalberith.GameDaemon.StuffSynchronizer;

import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import ru.Baalberith.GameDaemon.Utils.ItemDaemon;

public class ReplaceItem {
	
	enum ReplaceItemType {
		replaceAll, addAll;
	}
	
	private ReplaceItemType type;
	
	private Material fromMaterial;
	private short fromDurability;
	private String fromDisplayName;
	private List<String> fromLore;
	private Map<Enchantment, Integer> fromEnchantments;
	
	private Material toMaterial;
	private short toDurability;
	private String toDisplayName;
	private List<String> toLore;
	private Map<Enchantment, Integer> toEnchantments;
	
	public ReplaceItem(ReplaceItemType type,
			Material fromMaterial,
			short fromDurability,
			String fromDisplayName,
			List<String> fromLore,
			Map<Enchantment, Integer> fromEnchantments,
			Material toMaterial,
			short toDurability,
			String toDisplayName,
			List<String> toLore,
			Map<Enchantment, Integer> toEnchantments) {
		this.type = type;
		
		this.fromMaterial = fromMaterial;
		this.fromDurability = fromDurability;
		this.fromDisplayName = fromDisplayName != null ? fromDisplayName.replace("&", "§") : fromDisplayName;
		this.fromLore = fromLore;
		this.fromEnchantments = fromEnchantments;
		
		this.toMaterial = toMaterial;
		this.toDurability = toDurability;
		this.toDisplayName = toDisplayName != null ? toDisplayName.replace("&", "§") : toDisplayName;
		this.toLore = toLore;
		this.toEnchantments = toEnchantments;
	}
	
	public void changeItem(ItemStack item) {
		if (type == ReplaceItemType.addAll) {
			if (toEnchantments != null && !containsEnchants(item, toEnchantments)) item.addUnsafeEnchantments(toEnchantments);
			if (toLore != null && !containsLore(item, toLore)) ItemDaemon.addLore(item, toLore);
			
		} else if (type == ReplaceItemType.replaceAll) {
			if (toMaterial != null) item.setType(toMaterial);
			if (toDurability != -1) item.setDurability(toDurability);
			if (toEnchantments != null) ItemDaemon.setEnchantments(item, toEnchantments);
			ItemMeta meta = item.getItemMeta();
			if (toDisplayName != null) meta.setDisplayName(toDisplayName);
			if (toLore != null) meta.setLore(toLore);
			item.setItemMeta(meta);
		}
	}
	
	public boolean compare(ItemStack item) {
		if (!found(item)) return false;
		if (type == ReplaceItemType.addAll) {
			if (equalsDisplayName(item, toDisplayName) && containsLore(item, toLore) && containsEnchants(item, toEnchantments)) return false;
			if ( (equalsDisplayName(item, toDisplayName) && containsLore(item, toLore) && containsEnchants(item, toEnchantments)) || 
					(containsLore(item, fromLore) && containsEnchants(item, fromEnchantments))) return true;
		} else if (type == ReplaceItemType.replaceAll) {
			if (item.getType() == toMaterial && equalsDisplayName(item, toDisplayName) && equalsLore(item, toLore) && equalsEnchants(item, toEnchantments)) return false;
			if ( (equalsDisplayName(item, toDisplayName) && equalsLore(item, toLore) && equalsEnchants(item, toEnchantments)) || 
					(equalsDisplayName(item, fromDisplayName) && equalsLore(item, fromLore) && equalsEnchants(item, fromEnchantments))) return true;
		}
		return false; // цель
	}
	
	private boolean found(ItemStack item) {
		return (item.getType() == fromMaterial && ((fromDurability != -1) ? item.getDurability() == fromDurability : true)) ? true : false;
	}
	
	private boolean equalsDisplayName(ItemStack item, String displayName) {
		if (displayName == null) return true;
		if (!item.hasItemMeta()) return false;
		if (!item.getItemMeta().hasDisplayName()) return false;
		return item.getItemMeta().getDisplayName().equalsIgnoreCase(displayName);
	}
	
	private boolean containsLore(ItemStack item, List<String> lore) {
		if (lore == null || lore.isEmpty()) return true;
		return ItemDaemon.containsLore(item, lore);
	}
	
	private boolean containsEnchants(ItemStack item, Map<Enchantment, Integer> enchantments) {
		if (enchantments == null || enchantments.isEmpty()) return true;
		return ItemDaemon.containsEnchants(item, enchantments);
	}
	
	private boolean equalsLore(ItemStack item, List<String> lore) {
		if (lore == null || lore.isEmpty()) return true;
		return ItemDaemon.containsLoreExact(item, lore);
	}
	
	private boolean equalsEnchants(ItemStack item, Map<Enchantment, Integer> enchantments) {
		if (enchantments == null || enchantments.isEmpty()) return true;
		return ItemDaemon.containsEnchantsExact(item, enchantments);
	}
}
