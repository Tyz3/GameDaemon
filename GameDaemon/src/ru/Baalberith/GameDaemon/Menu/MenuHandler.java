package ru.Baalberith.GameDaemon.Menu;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import ru.Baalberith.GameDaemon.GD;
import ru.Baalberith.GameDaemon.Menu.Elements.Element;
import ru.Baalberith.GameDaemon.Utils.ThreadDaemon;

public class MenuHandler implements Listener {
	
	public MenuHandler() {
		Bukkit.getPluginManager().registerEvents(this, GD.inst);
	}
	
	@EventHandler
    public void onMenuClick(InventoryClickEvent e) {
    	if(!(e.getWhoClicked() instanceof Player)) return;
		if (e.getClickedInventory() == null) return;
		Player p = (Player) e.getWhoClicked();
    	if (!MenuEngine.inst.menuHolders.containsKey(p.getName())) return;
    	e.setCancelled(true);
    	
    	if (e.getClickedInventory() == p.getOpenInventory().getBottomInventory()) return;
    	if (!(e.getClick() == ClickType.LEFT || e.getClick() == ClickType.SHIFT_LEFT || e.getClick() == ClickType.RIGHT)) {
    		// Если игрок в гм и он OP, то этот игрок может копировать предмет из меню.
    		if (p.isOp() && e.getClick() == ClickType.MIDDLE) e.setCancelled(false);
    		return;
    	}
    	String pName = p.getName();
    	
    	// Получает контейнер текущего расположения.
    	String containerName = MenuEngine.inst.menuHolders.get(pName);
    	Container c = MenuEngine.inst.containers.getContainer(pName, containerName);
    	if (c == null) return;

    	byte clickedSlot = (byte) e.getRawSlot();
    	
    	// Получаем элемент контейнера по его номеру и выполняем его action.
    	Element element = MenuEngine.inst.getElementByPosition(c, clickedSlot);
    	if (element == null) return;
    	// Отправляем в синхронный поток выполнение действий.
    	ThreadDaemon.sync(() -> element.action(p, e.getClick()));
	}
	
	@EventHandler
    public void onMenuClose(InventoryCloseEvent e) {
		// Мод MineMenu при высоком пинге посылал активацию этого события после того
		// как уже было открыто другое меню.
		if (!Containers.menuTitles.contains(e.getInventory().getTitle())) return; // Фикс от мода MineMenu 21.12.2019
		String pName = e.getPlayer().getName();
		if (!MenuEngine.inst.menuHolders.containsKey(pName)) return;
		
    	if (MenuEngine.inst.blockP.contains(pName)) return;
    	
		MenuEngine.inst.menuHolders.remove(pName);
    	Player p = (Player) e.getPlayer();
		ThreadDaemon.sync(() -> p.updateInventory());
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onJoin(PlayerJoinEvent e) {
		ThreadDaemon.syncLater(() -> _onJoin(e.getPlayer()), 20);
	}
	
	// Нужен для того, чтобы рестартить индивидуальные контейнеры при /gsm reload.
	public void _onJoin(Player p) {
		List<String> listOfIndContainers = MenuEngine.inst.containers.listOfIndContainer;
		if (listOfIndContainers.isEmpty()) return;
		List<Container> list = new ArrayList<Container>();
		for (String iContName : listOfIndContainers) {
			Container c = MenuEngine.inst.getIndividualContainer(p, iContName);
			if (c == null) continue;
			list.add(c);
 		}
		MenuEngine.inst.playersIndContainers.put(p.getName(), list);
	}
	
	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		List<String> iContainers = MenuEngine.inst.containers.listOfIndContainer;
		if (iContainers.isEmpty()) return;
		MenuEngine.inst.playersIndContainers.remove(e.getPlayer().getName());
	}
	
}
