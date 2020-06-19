package ru.Baalberith.GameDaemon.LightLevelingSystem;

import java.util.concurrent.CopyOnWriteArrayList;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;

import ru.Baalberith.GameDaemon.GD;
import ru.Baalberith.GameDaemon.GDPlayer;

public class LiLSHandler implements Listener {
	
	public static CopyOnWriteArrayList<Player> inMenu = new CopyOnWriteArrayList<Player>();
	
	@EventHandler
	public void onExpReceive(PlayerExpChangeEvent e) {
		GDPlayer p = GD.getGDPlayer(e.getPlayer());
		
		p.sendMessage("Было "+p.getTotalExperience() +" Получил "+e.getAmount());
		// TODO
	}
	
//	@EventHandler
//	public void onBoosterUse(PlayerInteractEvent e) {
//		// TODO
//	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onDeath(PlayerDeathEvent e) {
		e.setKeepLevel(true);
		e.setDroppedExp(0);
		GDPlayer p = GD.getGDPlayer(e.getEntity());
		p.takeExperience((int) (p.getTotalExperience()*0.1));
		Player k = e.getEntity().getKiller();
		
		System.out.println(p.getName());
		System.out.println(k);
	}
	
	@EventHandler
	public void onLevelMenuClick(InventoryClickEvent e) {
    	if(!(e.getWhoClicked() instanceof Player)) return;
		if (e.getClickedInventory() == null) return;
		if (!inMenu.contains((Player) e.getWhoClicked())) return;
		
		GDPlayer p = GD.getGDPlayer((Player) e.getWhoClicked());
    	if (e.getClickedInventory() == p.p.getOpenInventory().getBottomInventory()) return;
    	e.setCancelled(true);
    	
    	int clickedSlot = e.getRawSlot();
    	LiLSEngine.inst.processClick(p, clickedSlot);
	}
	
	@EventHandler
	public void onLevelMenuClose(InventoryCloseEvent e) {
		if (!e.getInventory().getTitle().equalsIgnoreCase(Message.levelMenuTitle.get())) return;
		
		Player p = (Player) e.getPlayer();
		inMenu.remove(p);
	}
}
