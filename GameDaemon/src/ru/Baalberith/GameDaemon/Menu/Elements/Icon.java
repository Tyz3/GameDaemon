package ru.Baalberith.GameDaemon.Menu.Elements;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import ru.Baalberith.GameDaemon.Menu.MenuEngine;
import ru.Baalberith.GameDaemon.Menu.Elements.Actions.Action;
import ru.Baalberith.GameDaemon.Menu.Elements.Actions.ActionNextWay;
import ru.Baalberith.GameDaemon.Menu.Elements.Actions.Action.ActionType;

public class Icon extends Element {
	private Action action;
	
	public Icon(int position, ItemStack i, String previousWay) {
		init(position, i);
		action = new ActionNextWay(null, null, previousWay);
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
		meta.setLore(getLore());
		meta.setDisplayName(getDisplayName());
		i.setItemMeta(meta);
		return i;
	}
}
