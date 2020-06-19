package ru.Baalberith.GameDaemon.Menu.Elements.Actions;

import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import ru.Baalberith.GameDaemon.Menu.Container;
import ru.Baalberith.GameDaemon.Menu.MenuEngine;

public class ActionCmds extends Action {
	/*
	 * Определяет набор команд, выполняемых при определённом
	 * клике на кнопку, а также возврат на страницу в previousWay.
	 */
	private List<String> commands;
	private String previousWay;
	
	
	public ActionCmds(List<String> commands, String previousWay) {
		this.commands = commands;
		this.previousWay = previousWay;
		actionType = ActionType.CMDS;
	}
	
	public List<String> getCommands() {
		return commands;
	}
	
	public String getPreviousWay() {
		return previousWay;
	}
	
	@Override
	public void action(Player p, ClickType clickType) {
		// Переход на инвентарь назад.
		if (ClickType.RIGHT.equals(clickType)) {
			if (previousWay.equalsIgnoreCase(MenuEngine.NO_ACTION)) return;
			Container c = MenuEngine.inst.containers.getContainer(p.getName(), previousWay);
			if (c == null) return;
			p.openInventory(c.getContainer());
			MenuEngine.inst.menuHolders.put(p.getName(), previousWay);
		}
		// Выполнение команд для игрока.
		if (ClickType.LEFT.equals(clickType) || ClickType.SHIFT_LEFT.equals(clickType)) {
	    	MenuEngine.inst.blockP.remove(p.getName()); // fix 11.12.2019
			p.closeInventory();
			for (String cmd : commands) p.performCommand(cmd);
		}
	}
	
	
}
