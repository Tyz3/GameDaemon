package ru.Baalberith.GameDaemon.PeaceNewbies.Commands;

import java.util.Date;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import ru.Baalberith.GameDaemon.PeaceNewbies.Newbie;
import ru.Baalberith.GameDaemon.PeaceNewbies.Newbies;
import ru.Baalberith.GameDaemon.Utils.MathOperation;

public class NewbieCMD implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		if (!Newbies.inst.isNewbie(sender.getName())) {
			sender.sendMessage(Newbies.MSG_NOT_NEWBIE);
			return true;
		}
		
		Newbie newbie = Newbies.inst.getNewbie(sender.getName());
		if (newbie == null) return false;
		if (args.length == 0) {
			sender.sendMessage(getHelpMsg(newbie));
			return true;
		}
		
		if (args.length == 1) {
			if (sender.hasPermission("gsm.admin") && !args[0].equalsIgnoreCase(sender.getName())) {
				sender.sendMessage("§7Статус новичка игрока "+args[0]+": "+Newbies.inst.isNewbie(args[0]) != null?"Yes":"No");
			}
			if (!args[0].equalsIgnoreCase(sender.getName())) {
				sender.sendMessage(getHelpMsg(newbie));
				return true;
			} else {
				Newbies.inst.removeNewbie(newbie.getName());
				newbie.sendMessage(Newbies.MSG_EXPIRED);
				broadcast(newbie.getName());
				return true;
			}
		}
		return true;
	}
	
	private String getHelpMsg(Newbie newbie) {
		long time = new Date(newbie.getResidual()).getTime();
		String msg = MathOperation.makeTimeToString(Newbies.MSG_HELP, time)
				.replace("{player}", newbie.getName());
		return msg;
	}

	private void broadcast(String player) {
		Bukkit.broadcastMessage(Newbies.MSG_BROADCAST.replace("{player}", player));
	}

}
