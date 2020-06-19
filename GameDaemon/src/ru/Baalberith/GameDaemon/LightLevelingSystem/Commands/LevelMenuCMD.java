package ru.Baalberith.GameDaemon.LightLevelingSystem.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import ru.Baalberith.GameDaemon.GD;
import ru.Baalberith.GameDaemon.GDPlayer;
import ru.Baalberith.GameDaemon.LightLevelingSystem.LiLSEngine;

public class LevelMenuCMD implements CommandExecutor {
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		GDPlayer p = GD.getGDPlayer(sender);
		
		LiLSEngine.inst.openLevelMenu(p);
		
		return true;
	}

}
