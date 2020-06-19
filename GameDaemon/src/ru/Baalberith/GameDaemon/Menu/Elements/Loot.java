package ru.Baalberith.GameDaemon.Menu.Elements;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import ru.Baalberith.GameDaemon.Menu.Containers;
import ru.Baalberith.GameDaemon.Menu.MenuEngine;
import ru.Baalberith.GameDaemon.Menu.Elements.Actions.Action;
import ru.Baalberith.GameDaemon.Menu.Elements.Actions.ActionNextWay;
import ru.Baalberith.GameDaemon.Menu.Elements.Actions.Action.ActionType;

public class Loot extends Element {

	private Action action;
	private int dropChance;
	
	public final static String DROP_CHANCE_PATH = "dropChance";
	
	public Loot(int position, ItemStack i, String previousWay, int dropChance) {
		init(position, i);
		action = new ActionNextWay(null, null, previousWay);
		this.dropChance = dropChance;
	}
	
	public void init(int position, ItemStack i) {
		this.position = position;
		item = i;
		List<String> lore = i.getItemMeta().getLore();
		lore.add("");
		lore.add(Containers.dropLoot);
	}
	
	@Override
	public void action(Player p, ClickType clickType) {
		if (action.getActionType() == ActionType.CMDS) {
			action.action(p, clickType);
		} else {
	    	MenuEngine.inst.blockP.add(p.getName());
			action.action(p, clickType);
	    	MenuEngine.inst.blockP.remove(p.getName());
		}
	}

	@Override
	public ItemStack getItem(Player p) {
		ItemStack i = new ItemStack(getMaterial(), getAmount(), (short)getDurability());
		i.addUnsafeEnchantments(getEnchantments());
		ItemMeta meta = i.getItemMeta();
		List<String> lore = new ArrayList<String>();
		for (String l : getLore()) {
			lore.add(l.replace("{dropChance}", String.valueOf(dropChance)).replace("&", "§"));
		}
		meta.setLore(lore);
		
		meta.setDisplayName(getDisplayName());
		i.setItemMeta(meta);
		return i;
	}

	public int getDropChance() {
		return dropChance;
	}

	public void setDropChance(int dropChance) {
		this.dropChance = dropChance;
	}

}
