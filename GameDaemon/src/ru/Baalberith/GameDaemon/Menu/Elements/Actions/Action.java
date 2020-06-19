package ru.Baalberith.GameDaemon.Menu.Elements.Actions;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

public abstract class Action {
	
	public abstract void action(Player p, ClickType clickType);
	public ActionType actionType;
	
	public enum ActionType {
		CMDS, NEXT_WAY, NOTHING;
	}
	
	public ActionType getActionType() {
		return actionType;
	}
	
}
