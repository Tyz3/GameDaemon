package ru.Baalberith.GameDaemon.Menu.Builder.Commands;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import ru.Baalberith.GameDaemon.ConfigsDaemon;
import ru.Baalberith.GameDaemon.Menu.Message;
import ru.Baalberith.GameDaemon.Menu.Builder.Builder;

public class MenubuilderCMD implements CommandExecutor {
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		if (!sender.hasPermission("gsm.admin")) return false;
		
		// mb add container <container> <title> <isIndividual>
		// mb add element <container> <element> <type> <position> <previousWay> *держим предмет в руке*
		// mb edit container <container> <property> add/set/remove/clear <arg>
		// mb edit element <container> <element> <property> add/set/remove/clear <arg>
		// mb clear container <container>
		// mb clear element <container> <element>

		if (args.length < 1) {
			Message.help_info.send(sender);
			return true;
		}
		if (args[0].equalsIgnoreCase("help")) {
			Message.help_info.send(sender);
			return true;
		}
		
		if (args[0].equalsIgnoreCase("show") || args[0].equalsIgnoreCase("s")) {
			if (args.length == 1) {
				List<String> message = Builder.getBuilder().getHierarchy(null, null);
				if (message == null) {
					Message.nothingShow.send(sender);
					return true;
				}
				for (String s : message) sender.sendMessage(s);
				return true;
			}
			if (args.length == 2) {
				List<String> message = Builder.getBuilder().getHierarchy(args[1], null);
				if (message == null) {
					Message.nothingShow.send(sender);
					return true;
				}
				for (String s : message) sender.sendMessage(s);
				return true;
			}
			if (args.length == 3) {
				List<String> message = Builder.getBuilder().getHierarchy(args[1], args[2]);
				if (message == null) {
					Message.nothingShow.send(sender);
					return true;
				}
				for (String s : message) sender.sendMessage(s);
				return true;
			}
			sender.sendMessage(ConfigsDaemon.notEnoughArgs);
			return true;
		}
		
		if (args.length < 3) {
			sender.sendMessage(ConfigsDaemon.notEnoughArgs);
			return true;
		}
		
		if (args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("a")) {
			if (args[1].equalsIgnoreCase("container") || args[1].equalsIgnoreCase("c")) {
				if (args.length != 5) {
					sender.sendMessage(ConfigsDaemon.notEnoughArgs);
					return true;
				}
				sender.sendMessage(Builder.getBuilder().add.addContainer(args[2], args[3], Boolean.parseBoolean(args[4])));
			}
			if (args[1].equalsIgnoreCase("element") || args[1].equalsIgnoreCase("e")) {
				if (args.length < 7)
					sender.sendMessage(ConfigsDaemon.notEnoughArgs);
				else
					sender.sendMessage(Builder.getBuilder().add.addElement(args[2], args[3], args[4], Integer.parseInt(args[5]), args[6], ((Player) sender).getItemInHand().clone(), args));
				
				return true;
			}
		}
		
		if (args[0].equalsIgnoreCase("edit") || args[0].equalsIgnoreCase("e")) {
			if (args[1].equalsIgnoreCase("container") || args[1].equalsIgnoreCase("c")) {
				if (args.length < 6) {
					sender.sendMessage(ConfigsDaemon.notEnoughArgs);
					return true;
				}
				String s = "";
				for (int i = 5; i < args.length; i++) {
					if (i != args.length-1) s += args[i]+" ";
					else s += args[i];
				}
				
				sender.sendMessage(Builder.getBuilder().edit.editContainerProperty(args[2], args[3], args[4], s));
				return true;
			}
			if (args[1].equalsIgnoreCase("element") || args[1].equalsIgnoreCase("e")) {
				if (args.length < 7) {
					sender.sendMessage(ConfigsDaemon.notEnoughArgs);
					return true;
				}
				String s = "";
				for (int i = 6; i < args.length; i++) {
					if (i != args.length-1) s += args[i]+" ";
					else s += args[i];
				}
				sender.sendMessage(Builder.getBuilder().edit.editElementProperty(args[2], args[3], args[4], args[5], s));
				return true;
			}
		}
		
		if (args[0].equalsIgnoreCase("clear") || args[0].equalsIgnoreCase("c")) {
			if (args[1].equalsIgnoreCase("container") || args[1].equalsIgnoreCase("c")) {
				if (args.length != 3) {
					sender.sendMessage(ConfigsDaemon.notEnoughArgs);
					return true;
				}
				sender.sendMessage(Builder.getBuilder().clear.removeContainer(args[2]));
				return true;
				
			}
			if (args[1].equalsIgnoreCase("element") || args[1].equalsIgnoreCase("e")) {
				if (args.length != 4) {
					sender.sendMessage(ConfigsDaemon.notEnoughArgs);
					return true;
				}
				sender.sendMessage(Builder.getBuilder().clear.removeElement(args[2], args[3]));
				return true;
				
			}
		}

		
		
		return true;
	}

}
