package ru.Baalberith.GameDaemon.RPGSpawn.Commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import ru.Baalberith.GameDaemon.ConfigsDaemon;
import ru.Baalberith.GameDaemon.GD;
import ru.Baalberith.GameDaemon.RPGSpawn.SpawnEngine;

public class RpgCMD implements CommandExecutor {
	
	private List<String> rpgSpawns = new ArrayList<String>();
	
	public RpgCMD() {
		rpgSpawns.addAll(ConfigsDaemon.rpgSpawnsConfig.get().getKeys(false));
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		if (!sender.hasPermission("gsm.rpg-spawn.use")) return false;
		
		if (args.length == 0) {
			if (GD.isConsoleSender(sender)) return false;
			return SpawnEngine.inst.exec(sender);
		}
		
		if (args.length > 0) {
			switch (args[0]) {
				case "info":
					if (!sender.hasPermission("gsm.rpg-spawn.helper")) return false;
					return SpawnEngine.inst.Cmd.info(sender);
				case "list":
					if (!sender.hasPermission("gsm.rpg-spawn.helper")) return false;
					return SpawnEngine.inst.Cmd.list(sender);
				case "add":
					if (!sender.hasPermission("gsm.rpg-spawn.admin")) return false;
					if (args.length < 2) return false;
					String str1 = "";
					for (int i = 1; i < args.length; i++) str1 += args[i]+" ";
					return SpawnEngine.inst.Cmd.add(sender, str1.substring(0, str1.length()-1));
				case "change":
					if (!sender.hasPermission("gsm.rpg-spawn.helper")) return false;
					if (args.length < 3) return false;
					String str2 = "";
					for (int i = 2; i < args.length; i++) str2 += args[i]+" ";
					return SpawnEngine.inst.Cmd.change(sender, args[1], str2.substring(0, str2.length()-1));
				case "remove":
					if (!sender.hasPermission("gsm.rpg-spawn.admin")) return false;
					if (args.length < 2) return false;
					return SpawnEngine.inst.Cmd.remove(sender, args[1]);
				default:
					return false;
			}
		}
		
		return false;
	}
	

}
