package ru.Baalberith.GameDaemon.MuteDaemon.Commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import ru.Baalberith.GameDaemon.GD;
import ru.Baalberith.GameDaemon.GDPlayer;
import ru.Baalberith.GameDaemon.MuteDaemon.MuteEngine;
import ru.Baalberith.GameDaemon.Utils.CompleteHelper;
import ru.Baalberith.GameDaemon.Utils.ThreadDaemon;

public class MuteCMD implements CommandExecutor, TabCompleter {

	public MuteCMD() {
		GD.inst.getCommand("mute").setExecutor(this);
		GD.inst.getCommand("mute").setTabCompleter(this);
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		GDPlayer p = GD.getGDPlayer(sender);
		
		if (args.length == 0) {
			if (!GD.isConsoleSender(sender))
				ThreadDaemon.async(() -> MuteEngine.inst.checkMute(p, p.getName()));
			else ThreadDaemon.async(() -> MuteEngine.inst.help(GD.consoleSender));
			return true;
		}
		
		if (args.length == 1) {
			if (args[0].equalsIgnoreCase("help")) {
				ThreadDaemon.async(() -> MuteEngine.inst.helpFull(p));
				return true;
			} else if (args[0].equalsIgnoreCase("history") || args[0].equalsIgnoreCase("hist")) {
				if (!GD.isConsoleSender(sender))
					ThreadDaemon.async(() -> MuteEngine.inst.muteHistory(p, p.getName()));
				return true;
			} else {
				ThreadDaemon.async(() -> MuteEngine.inst.checkMute(p, args[0]));
				return true;
			}
		}
		
		if (args.length == 2) {
			if (args[0].equalsIgnoreCase("history") || args[0].equalsIgnoreCase("hist")) {
				ThreadDaemon.async(() -> MuteEngine.inst.muteHistory(p, args[1]));
				return true;
			} else {
				String[] reasons = args[1].split("\\+");
				ThreadDaemon.async(() -> MuteEngine.inst.mutePlayer(p, args[0], reasons));
				return true;
			}
		}
		
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {

		if (!sender.hasPermission("gsm.mutes.helper")) return null;
		if (args.length == 1) {
			return CompleteHelper.filter(args, getMuttablePlayers(), "help", "history");
		} else if (args.length == 2) {
			if (args[0].equalsIgnoreCase("history")) {
				return CompleteHelper.filter(args, getMuttablePlayers());
			} else {
				return CompleteHelper.filter(args, MuteEngine.allowedReasons);
			}
		}
		
		return null;
	}
	
	// Игроки с правами оператора или gsm.mutes.immune не подвергаются мутам
	private List<String> getMuttablePlayers() {
		List<String> list = new ArrayList<String>();
		GD.online.stream().forEach(p -> {
			if (!(p.isOp() || p.hasPermission("gsm.mutes.immune")))
				list.add(p.getName());
		});
		return list;
	}

}
