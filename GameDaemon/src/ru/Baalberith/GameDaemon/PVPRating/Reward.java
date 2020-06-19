package ru.Baalberith.GameDaemon.PVPRating;

import java.util.List;

import org.bukkit.inventory.ItemStack;

import ru.Baalberith.GameDaemon.Utils.ItemDaemon;

public class Reward {
	
	private String itemString;
	
	public Reward(String itemString, List<String> lore, List<String> enchants) {
		this.itemString = itemString
				.replace("{lore}", ItemDaemon.listToString(lore, "@"))
				.replace("{enchs}", ItemDaemon.listToString(enchants, "@")).replace("&", "\u00a7");
	}

	public String getItemString() {
		return itemString;
	}
	
	public ItemStack getItem() {
		return ItemDaemon.deSerializeItem(itemString);
	}
}
