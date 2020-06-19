package ru.Baalberith.GameDaemon.MCMMOBoost.Commands;

import java.util.Arrays;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import ru.Baalberith.GameDaemon.MCMMOBoost.BoostEngine;
import ru.Baalberith.GameDaemon.Utils.ThreadDaemon;

public class MmoboostCMD implements CommandExecutor {
	
	private List<String> skills;
	
	public MmoboostCMD(String[] skills) {
		this.skills = Arrays.asList(skills);
	}
	
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length != 1) return false;
		
		if (!skills.contains(args[0])) return false;
		
		ThreadDaemon.async(() -> BoostEngine.inst.process(sender, args[0]));
		
		return true;
		
	}
	
}
