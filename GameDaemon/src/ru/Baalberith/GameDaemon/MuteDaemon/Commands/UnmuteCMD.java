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

public class UnmuteCMD implements CommandExecutor, TabCompleter {

	public UnmuteCMD() {
		GD.inst.getCommand("unmute").setExecutor(this);
		GD.inst.getCommand("unmute").setTabCompleter(this);
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

		GDPlayer p = GD.getGDPlayer(sender);
		
		if (args.length == 0) {
			ThreadDaemon.async(() -> MuteEngine.inst.help(p));
			return true;
		}
		
		if (args.length == 1) {
			ThreadDaemon.async(() -> MuteEngine.inst.unmutePlayer(p, args[0], ""));
			return true;
		} else {
			ThreadDaemon.async(() -> MuteEngine.inst.unmutePlayer(
					p, args[0], 
					String.join(" ", args).replace(args[0]+" ", "")
				));
			
		}
		
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		
		if (args.length == 1) {
			return CompleteHelper.filter(args, getMuttablePlayers());
		}
		
		return null;
	}
	
	// Игроки с правами оператора или gsm.mutes.immune не подвергаются мутам
	private List<String> getMuttablePlayers() {
		List<String> list = new ArrayList<String>();
		GD.online.stream().forEach(p -> {
			if (!(p.isOp() || p.hasPermission("gsm.mutes.immune") || !p.isMuted()))
				list.add(p.getName());
		});
		return list;
	}

}
