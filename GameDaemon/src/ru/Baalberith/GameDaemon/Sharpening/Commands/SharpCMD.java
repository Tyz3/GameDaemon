package ru.Baalberith.GameDaemon.Sharpening.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import ru.Baalberith.GameDaemon.ConfigsDaemon;
import ru.Baalberith.GameDaemon.GD;
import ru.Baalberith.GameDaemon.GDPlayer;
import ru.Baalberith.GameDaemon.Sharpening.SharpEngine;
import ru.Baalberith.GameDaemon.Sharpening.Task;

public class SharpCMD implements CommandExecutor {

	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

		if (!sender.hasPermission("gsm.sharpening")) {
			sender.sendMessage(ConfigsDaemon.noPermission);
			return true;
		}
		
		GDPlayer p = GD.getGDPlayer(sender);
		Task task = new Task(p);
		SharpEngine.inst.addTask(sender.getName(), task);
		task.openInventory();
		return true;
	}
}
