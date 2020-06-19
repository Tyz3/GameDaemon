package ru.Baalberith.GameDaemon.Menu.Elements.Actions;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import ru.Baalberith.GameDaemon.Menu.Container;
import ru.Baalberith.GameDaemon.Menu.MenuEngine;


public class ActionNextWay extends Action {
	/*
	 * Отвечает за выполнения действия кнопки-перехода
	 * на другие страницы по определённым типам кликов.
	 */
	private String nextWayLC;
	private String nextWayLSC;
	private String previousWay;
	
	public ActionNextWay(String nextWayLC, String nextWayLSC, String previousWay) {
		this.nextWayLC = nextWayLC;
		this.nextWayLSC = nextWayLSC;
		this.previousWay = previousWay;
		actionType = ActionType.NEXT_WAY;
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
		// Переход на инвентарь вперёд #1.
		if (ClickType.LEFT.equals(clickType)) {
			Container c = MenuEngine.inst.containers.getContainer(p.getName(), nextWayLC);
			if (c == null) return;
			p.openInventory(c.getContainer());
			MenuEngine.inst.menuHolders.put(p.getName(), nextWayLC);
		}
		// Переход на инвентарь вперёд #2.
		if (ClickType.SHIFT_LEFT.equals(clickType)) {
			Container c = MenuEngine.inst.containers.getContainer(p.getName(), nextWayLSC);
			if (c == null) return;
			p.openInventory(c.getContainer());
			MenuEngine.inst.menuHolders.put(p.getName(), nextWayLSC);
		}
	}

	public String getNextWayLC() {
		return nextWayLC;
	}

	public String getNextWayLSC() {
		return nextWayLSC;
	}

	public String getPreviousWay() {
		return previousWay;
	}
}
