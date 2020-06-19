package ru.Baalberith.GameDaemon.StuffSynchronizer;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import ru.Baalberith.GameDaemon.ConfigsDaemon;
import ru.Baalberith.GameDaemon.Utils.ThreadDaemon;

public class SynchronizeHandler implements Listener {
	
	
	@EventHandler
	public void onJoinServer(PlayerJoinEvent e) {
		if (!ConfigsDaemon.stuffSynchronize) return;
		Player p = e.getPlayer();
		ThreadDaemon.asyncLater(() -> {
			if (SynchronizeEngine.inst.processChange(p.getInventory().getContents())
					| SynchronizeEngine.inst.processChange(p.getInventory().getArmorContents())) {
				p.sendMessage("Некоторые предметы были изменены в вашем инвентаре с целью балансировки.");
				p.sendMessage("Извиняемся за возможные потерянные ресурсы =).");
			}
		}, 2);
	}
	
	@EventHandler
	public void onOpenInventory(InventoryOpenEvent e) {
		if (!ConfigsDaemon.stuffSynchronize) return;
		Player p = (Player) e.getPlayer();
		ThreadDaemon.asyncLater(() -> {
			if (SynchronizeEngine.inst.processChange(e.getInventory().getContents())) {
				p.sendMessage("Некоторые предметы были изменены в вашем сундуке с целью балансировки.");
				p.sendMessage("Извиняемся за возможные потерянные ресурсы =).");
			}
		}, 2);
	}
	
	
	
}
