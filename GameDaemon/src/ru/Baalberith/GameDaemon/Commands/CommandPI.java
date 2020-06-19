package ru.Baalberith.GameDaemon.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CommandPI implements CommandExecutor{
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		if (!cmd.getName().equalsIgnoreCase("player-info")) return true;
		
//		Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
//			
//			@Override
//			public void run() {
//				ConfigurationSection mess = ConfigsDaemon.inst.getMessagesConfig();
//			
//				if (!sender.hasPermission("gsm.player-info")) {
//					sender.sendMessage(mess.getString("other.no-permission").replace("&", "\u00a7"));
//					return;
//				}
//				
//				if (args.length == 0) {
//					List<String> msg = mess.getStringList("command.player-info");
//					for (int i = 0; i < msg.size(); i++) {
//						sender.sendMessage(placeHolder.setPlaceHolder(sender.getName(), msg.get(i)));
//					}
//					return;
//				}
//				
//				if (!sender.hasPermission("gsm.player-info.other")) {
//					sender.sendMessage(mess.getString("other.no-permission").replace("&", "\u00a7"));
//					return;
//				}
//				
//				if (args.length == 1) {
//					if (Storage.fileExists(ConfigsDaemon.STATS_FOLDER, args[0])) {
//						List<String> msg = mess.getStringList("command.player-info");
//						for (int i = 0; i < msg.size(); i++) {
//							sender.sendMessage(placeHolder.setPlaceHolder(args[0], msg.get(i)));
//						}
//						return;
//					} else {
//						sender.sendMessage(mess.getString("command.does-not-exists").replace("&", "\u00a7"));
//						return;
//					}
//				}
//			}
//		});
		return false;
	}
}
