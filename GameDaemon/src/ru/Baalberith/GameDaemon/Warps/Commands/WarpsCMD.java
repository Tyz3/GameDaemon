package ru.Baalberith.GameDaemon.Warps.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import ru.Baalberith.GameDaemon.GD;
import ru.Baalberith.GameDaemon.GDPlayer;
import ru.Baalberith.GameDaemon.Utils.ThreadDaemon;
import ru.Baalberith.GameDaemon.Warps.WarpEngine;

public class WarpsCMD implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (GD.isConsoleSender(sender)) return false;
		GDPlayer p = GD.getGDPlayer(sender);
		if (args.length != 0) {
			ThreadDaemon.async(() -> {
				try {
					WarpEngine.inst.warps(p, Integer.parseInt(args[0]));
				} catch (NumberFormatException e) {
					sender.sendMessage("Укажи страницу, а не букву, дебил.");
				}
			});
		} else ThreadDaemon.async(() -> WarpEngine.inst.warps(p, 1));
		return true;
	}
}
