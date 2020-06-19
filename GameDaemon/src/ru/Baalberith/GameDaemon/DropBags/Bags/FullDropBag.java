package ru.Baalberith.GameDaemon.DropBags.Bags;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.inventory.ItemStack;

import ru.Baalberith.GameDaemon.GDPlayer;
import ru.Baalberith.GameDaemon.DropBags.Drop;
import ru.Baalberith.GameDaemon.DropBags.DropBag;

public class FullDropBag extends DropBag {
	
	
	public FullDropBag(String name, ItemStack bag, ItemStack key, List<Drop> drops, boolean chestMessage,
			String displayName, BagType bagType) {
		super(name, bag, key, drops, chestMessage, displayName, bagType);
	}

	public void giveDrop(GDPlayer p) {
		if (!takeItems(p, bag)) return;
		
		List<Drop> dropToGive = tryDrop(p, 1);
		if (hasDrop(dropToGive, p));
		dropToGive.stream().forEach(i -> giveItem(i, p));
		
		p.p.updateInventory();
		p.addOpenedBags(getDisplayName(), 1);
	}

	public void giveShiftDrop(GDPlayer p, int amount) {
		if (!takeItems(p, bag, amount)) return;

		List<Drop> dropToGive = tryDrop(p, amount);
		if (hasDrop(dropToGive, p));
		dropToGive.stream().forEach(i -> giveItem(i, p));
		
		p.p.updateInventory();
		p.addOpenedBags(getDisplayName(), amount);
	}
	
	private List<Drop> tryDrop(GDPlayer p, int amount) {
		List<Drop> items = new ArrayList<Drop>();
		for (int i = 0; i < amount; i++) {
			for (Drop drop : drops) {
				if (!drop.tryChance()) continue;
				items.add(drop);
			}
		}
		return items;
	}

}
