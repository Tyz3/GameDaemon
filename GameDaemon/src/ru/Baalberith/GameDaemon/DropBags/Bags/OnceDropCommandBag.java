package ru.Baalberith.GameDaemon.DropBags.Bags;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.inventory.ItemStack;

import ru.Baalberith.GameDaemon.GD;
import ru.Baalberith.GameDaemon.GDPlayer;
import ru.Baalberith.GameDaemon.DropBags.Drop;
import ru.Baalberith.GameDaemon.DropBags.DropBag;
import ru.Baalberith.GameDaemon.DropBags.Message;

public class OnceDropCommandBag extends DropBag {
	
	private double totalShare = 0;
	private String command;
	public OnceDropCommandBag(String name, ItemStack bag, ItemStack key, List<Drop> drops, boolean chestMessage,
			String displayName, BagType bagType, String command) {
		super(name, bag, key, drops, chestMessage, displayName, bagType);
		this.command = command;
		drops.forEach(d -> totalShare += d.getChance());
	}
	
	@Override
	public void giveDrop(GDPlayer p) {
		if (!takeItems(p, bag)) return;
		
		Drop dropToGive = tryDrop(p);
		if (dropToGive == null) {
			Message.emptyBag.send(p);
			return;
		}
		Message.personal.send(p);
		giveItem(dropToGive, p);
		GD.dispatchCommand(command.replace("{player}", p.getName()));
		
		p.p.updateInventory();
		p.addOpenedBags(getDisplayName(), 1);
	}
	
	@Override
	public void giveShiftDrop(GDPlayer p, int amount) {
		if (!takeItems(p, bag, amount)) return;
		
		for (int i = 0; i < amount; i++) {
			Drop dropToGive = tryDrop(p);
			if (dropToGive == null) {
				Message.emptyBag.send(p);
				continue;
			}
			Message.personal.send(p);
			giveItem(dropToGive, p);
			GD.dispatchCommand(command.replace("{player}", p.getName()));
		}
		
		p.p.updateInventory();
		p.addOpenedBags(getDisplayName(), amount);
	}
	
	private Drop tryDrop(GDPlayer p) {
		double totalShare = this.totalShare;
		List<Drop> permDrop = new ArrayList<Drop>();
		for (Drop d : drops) {
			if (d.hasPermission(p)) permDrop.add(d);
			else totalShare -= d.getChance();
		}
		
		double random = 1 + Math.random() * (totalShare - 1);
		for (Drop d : permDrop) {
			random -= d.getChance();
			if (random <= 0) return d;
		}
		if (permDrop.isEmpty()) return null;
		return permDrop.get(0);
	}
}
