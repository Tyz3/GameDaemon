package ru.Baalberith.GameDaemon.Warps.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import ru.Baalberith.GameDaemon.GD;
import ru.Baalberith.GameDaemon.GDPlayer;
import ru.Baalberith.GameDaemon.Utils.ThreadDaemon;
import ru.Baalberith.GameDaemon.Warps.WarpEngine;

public class DelwarpCMD implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		GDPlayer p = GD.getGDPlayer(sender);
		if (args.length == 1) {
			ThreadDaemon.async(() -> WarpEngine.inst.deleteWarp(p, args[0]));
			return true;
		}
		ThreadDaemon.async(() -> WarpEngine.inst.help(sender));
		return true;
	}
}
