package ru.Baalberith.GameDaemon.DropBags.Bags;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.bukkit.inventory.ItemStack;

import ru.Baalberith.GameDaemon.GDPlayer;
import ru.Baalberith.GameDaemon.DropBags.Drop;
import ru.Baalberith.GameDaemon.DropBags.DropBag;
import ru.Baalberith.GameDaemon.DropBags.Message;

public class TwiceDropBag extends DropBag {

	private double totalShare = 0;
	public TwiceDropBag(String name, ItemStack bag, ItemStack key, List<Drop> drops, boolean chestMessage,
			String displayName, BagType bagType) {
		super(name, bag, key, drops, chestMessage, displayName, bagType);
		Collections.sort(drops, Comparator.comparing(Drop::getChance));
		drops.forEach(d -> totalShare += d.getChance());
	}

	@Override
	public void giveDrop(GDPlayer p) {
		if (!takeItems(p, bag)) return;
		
		List<Drop> dropToGive = tryDrop(p);
		if (dropToGive.isEmpty()) {
			Message.emptyBag.send(p);
			return;
		}
		if (hasDrop(dropToGive, p));
		dropToGive.stream().forEach(i -> giveItem(i, p));
		
		p.p.updateInventory();
		p.addOpenedBags(getDisplayName(), 1);
	}

	@Override
	public void giveShiftDrop(GDPlayer p, int amount) {
		if (!takeItems(p, bag, amount)) return;
		
		for (int j = 0; j < amount; j++) {
			List<Drop> dropToGive = tryDrop(p);
			if (dropToGive.isEmpty()) {
				Message.emptyBag.send(p);
				continue;
			}
			if (hasDrop(dropToGive, p));
			dropToGive.stream().forEach(i -> giveItem(i, p));
		}
		
		p.p.updateInventory();
		p.addOpenedBags(getDisplayName(), amount);
	}
	
	private List<Drop> tryDrop(GDPlayer p) {
		double totalShare = this.totalShare;
		List<Drop> permDrop = new ArrayList<Drop>();
		for (Drop d : drops) {
			if (d.hasPermission(p)) permDrop.add(d);
			else totalShare -= d.getChance();
		}
		
		List<Drop> items = new ArrayList<Drop>();
		double fix = 0;
		double random = 1 + Math.random() * (totalShare - 1);
		for (Drop d : permDrop) {
			random -= d.getChance();
			if (random <= 0) {
				items.add(d);
				fix = d.getChance();
				break;
			}
		}

		// Механизм рандомизации второго другого предмета из
		// той же коллекции, основан на сортировке drops от БОЛЬШЕГО к меньшему.
		random = 1 + Math.random() * (totalShare - 1) - fix;
		for (Drop d : permDrop) {
			random -= d.getChance();
			if (random <= 0) {
				if (d.getChance() != fix) {
					items.add(d);
					break;
				} else random += fix;
			}
		}
		
		return items;
	}

}
