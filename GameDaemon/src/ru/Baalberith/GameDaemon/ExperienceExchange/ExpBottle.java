package ru.Baalberith.GameDaemon.ExperienceExchange;

import org.bukkit.inventory.ItemStack;

public class ExpBottle {
	
	ItemStack in;
	ItemStack out;
	int exp = 0;
	
	public ExpBottle(ItemStack in, ItemStack out, int exp) {
		this.in = in;
		this.out = out;
		this.exp = exp;
	}
	
	public ExpBottle(ItemStack in) {
		this(in, null, 0);
	}
	
	public boolean equals(ExpBottle b) {
		if (b.in == null) return false;
		return b.in.getType() == in.getType() && b.in.getDurability() == in.getDurability();
	}
}
