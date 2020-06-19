package ru.Baalberith.GameDaemon.DropBags;

import java.util.List;

import org.bukkit.inventory.ItemStack;

import ru.Baalberith.GameDaemon.GD;
import ru.Baalberith.GameDaemon.GDPlayer;
import ru.Baalberith.GameDaemon.Utils.MathOperation;

public abstract class DropBag {
	
	public enum BagType {
		full, twice, once, onceCmd;
	}
	
	protected List<Drop> drops;
	protected ItemStack bag;
	protected ItemStack key; // Ключ для открытия DropBag
	
	private String name;
	private boolean chestMessage; // Выбор предмета с дропом: сундук или сумка
	private String displayName;
	private BagType bagType;
	
	public DropBag(String name, ItemStack bag, ItemStack key, List<Drop> drops, boolean chestMessage, String displayName, BagType bagType) {
		this.name = name;
		this.bag = bag;
		this.key = key;
		this.drops = drops;
		this.chestMessage = chestMessage;
		this.displayName = displayName;
		this.bagType = bagType;
	}

	public abstract void giveDrop(GDPlayer p);
	public abstract void giveShiftDrop(GDPlayer p, int amount);
	
	public ItemStack getBagItem() {
		return bag;
	}
	
	public boolean takeItems(GDPlayer p, ItemStack bag) {
		if (!(this.bag.getType() == bag.getType() && this.bag.getDurability() == bag.getDurability())) return false;
		if (key != null && !p.takeItem(key)) {
			Message.hasKey.send(p);
			return false;
		}
		return p.takeItem(this.bag);
	}
	
	public boolean takeItems(GDPlayer p, ItemStack bag, int amount) {
		if (!(this.bag.getType() == bag.getType() && this.bag.getDurability() == bag.getDurability())) return false;
		if (key != null && p.hasItem(key, amount) == -1) {
			Message.hasKey.send(p);
			return false;
		} else p.takeItem(key, amount);
		return p.takeItem(this.bag, amount);
	}
	
	public void giveItem(Drop d, GDPlayer p) {
		ItemStack i = d.getItem();
		if (d.hasBroadcast()) sendBroadcast(p.getName(), d, i);
		p.giveItem(i);
	}
	
	public boolean hasDrop(List<Drop> d, GDPlayer p) {
		if (d.isEmpty()) {
			Message.emptyBag.send(p);
			return false;
		} else Message.personal.send(p);
		
		return true;
	}
	
	public String getName() {
		return name;
	}
	
	public BagType getBagType() {
		return bagType;
	}
	
	private void sendBroadcast(String player, Drop drop, ItemStack item) {
		GD.sendMessageForAll(
			(chestMessage ? Message.broadcastRareChest.get() : Message.broadcastRareBag.get())
			.replace("{player}", player)
			.replace("{chance}", String.valueOf(MathOperation.roundAvoid(drop.getChance(), 2)))
			.replace("{amount}", String.valueOf(item.getAmount()))
			.replace("{id}", String.valueOf(item.getTypeId()))
			.replace("{data}", String.valueOf(item.getDurability()))
			.replace("{lore}", drop.getRawLore() == null ? "" : drop.getRawLore())
			.replace("{enchantments}", drop.getRawEnchants() == null ? "" : drop.getRawEnchants())
			.replace("{displayName}", drop.getDisplayName())
			.replace("{DISPLAYNAME}", displayName)
			.replace("{ID}", String.valueOf(bag.getType().getId()))
			.replace("{DATA}", String.valueOf(bag.getDurability()))
			);
	}
	
	public String getDisplayName() {
		return this.displayName;
	}
}
