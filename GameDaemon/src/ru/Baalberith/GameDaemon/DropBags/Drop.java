package ru.Baalberith.GameDaemon.DropBags;

import org.bukkit.inventory.ItemStack;

import ru.Baalberith.GameDaemon.GDPlayer;
import ru.Baalberith.GameDaemon.Utils.Utils;

public class Drop {

	private double chance;
	private int minAmount;
	private int maxAmount;
	private boolean hasBroadcast;
	private ItemStack drop;
	private String rawLore;
	private String rawEnchants;
	private String displayName;
	private String permission;
	
	public Drop(ItemStack drop,
			double chance,
			int minAmount,
			int maxAmount,
			boolean hasBroadcast,
			String displayName,
			String permission) {
		this.chance = chance;
		this.minAmount = minAmount;
		this.maxAmount = maxAmount;
		this.hasBroadcast = hasBroadcast;
		this.drop = drop;
		this.displayName = displayName;
		this.permission = permission;
		if (drop.hasItemMeta() && drop.getItemMeta().hasLore()) this.rawLore = Utils.rawLore(drop.getItemMeta().getLore());
		if (drop.hasItemMeta() && drop.getItemMeta().hasEnchants()) this.rawEnchants = Utils.rawEnchants(drop.getEnchantments());
		
	}
	
	public boolean hasPermission(GDPlayer p) {
		if (permission == null) return true;
		return p.hasPermission(permission);
	}

	public boolean tryChance() {
		return 99*Math.random()+1 <= chance ? true : false;
	}

	private int getAmount() {
		return (int) Math.round((maxAmount - minAmount)*Math.random()+minAmount);
	}
	
	public ItemStack getItem() {
		drop.setAmount(getAmount());
		return drop;
	}

	public String getRawLore() {
		return rawLore;
	}

	public String getRawEnchants() {
		return rawEnchants;
	}

	public boolean hasBroadcast() {
		return hasBroadcast;
	}
	
	public double getChance() {
		return chance;
	}

	public String getDisplayName() {
		return displayName;
	}
	

}
