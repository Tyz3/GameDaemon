package ru.Baalberith.GameDaemon.Menu.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import ru.Baalberith.GameDaemon.Menu.Container;
import ru.Baalberith.GameDaemon.Menu.MenuEngine;
import ru.Baalberith.GameDaemon.Utils.ThreadDaemon;

public class MenuCMD implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		if (!(sender instanceof Player)) return false;
		
		Player p = (Player) sender;
		String pName = sender.getName();
		
		Container c = MenuEngine.inst.containers.getContainer(pName, MenuEngine.MAIN_MENU_NAME);
		if (c == null) {
			sender.sendMessage("§4Меню не сформировано.");
			return false;
		}
		MenuEngine.inst.menuHolders.put(pName, MenuEngine.MAIN_MENU_NAME);
		ThreadDaemon.sync(() -> p.openInventory(c.getContainer()));
		
		return true;
	}
	
}
