package ru.Baalberith.GameDaemon.LightLevelingSystem;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import ru.Baalberith.GameDaemon.GDPlayer;
import ru.Baalberith.GameDaemon.Utils.MathOperation;

public class ExpBooster {
	
	ItemStack item;
	double bonus;
	long durationMillis;
	
	public ExpBooster(Material type, short data, double bonus, long durationMillis) {
		item = new ItemStack(type, 1, data);
		this.bonus = bonus;
		this.durationMillis = durationMillis;
	}
	
	public void giveBoost(GDPlayer p, ItemStack itemInHand) {
		if (!equals(itemInHand)) return;
		if (p.getExpBoostBonus() < bonus) {
			p.setExpBoost(true);
			p.setExpBoostBonus(bonus);
			p.setExpBoostRemainingTime(durationMillis);
			Message.booster_activate
				.replace("{xpBoost}", String.valueOf(bonus*100))
				.replace("{time}", MathOperation.makeTimeToString(Message.booster_time.get(), durationMillis))
				.send(p);
			
		} else Message.booster_imposible.send(p);
		
	}
	
	public boolean equals(ItemStack i) {
		return i.getType() == item.getType() && i.getDurability() == item.getDurability();
	}
}
