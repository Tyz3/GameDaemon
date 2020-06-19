package ru.Baalberith.GameDaemon.PVPRating.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;

import ru.Baalberith.GameDaemon.ConfigsDaemon;
import ru.Baalberith.GameDaemon.GD;
import ru.Baalberith.GameDaemon.GDPlayer;
import ru.Baalberith.GameDaemon.PVPRating.RatingEngine;
import ru.Baalberith.GameDaemon.PVPRating.Rewards;
import ru.Baalberith.GameDaemon.PVPRating.TopEngine;
import ru.Baalberith.GameDaemon.Utils.ThreadDaemon;

public class PvpCMD implements CommandExecutor {
	
	private ConfigurationSection m;
	
	public void reload() {
		m = ConfigsDaemon.messagesConfig.getConfigurationSection("pvp");
	}
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		
		if (!sender.hasPermission("gsm.pvp")) return false;
		
		if (args.length == 0) {
			if (GD.isConsoleSender(sender)) return false;
			GDPlayer p = GD.getGDPlayer(sender);
			p.sendMessage(
					RatingEngine.funcPrefix+
					m.getString("rating").replace("[symbol]", TopEngine.SYMBOL)
					.replace("[current]", ""+p.getPvpRating())
					.replace("&", "\u00a7"));
			return true;
		}
		
		if (args.length == 1) {
			if (args[0].equalsIgnoreCase("lastkills") || args[0].equalsIgnoreCase("lk")) {
				if (GD.isConsoleSender(sender)) return false;
				ThreadDaemon.async(() -> Rewards.engine.seeLastKills(sender));
				return true;
			}
			
			if (args[0].equalsIgnoreCase("info")) {
				for (String str : m.getStringList("info"))
					sender.sendMessage(str.replace("[funcPrefix]", RatingEngine.funcPrefix).replace("&", "\u00a7"));
				if (sender.hasPermission("gsm.pvp.admin")) 
					for (String str : m.getStringList("info-admin"))
						sender.sendMessage(str.replace("&", "\u00a7"));
				return true;
			}
			
			if (args[0].equalsIgnoreCase("top")) {
				ThreadDaemon.async(() -> Rewards.engine.sendTop(sender));
				return true;
			}
			

			if (args[0].equalsIgnoreCase("reward")) {
				if (GD.isConsoleSender(sender)) return false;
				ThreadDaemon.sync(() -> Rewards.engine.giveReward(sender));
				return true;
			}
			
			if (args[0].equalsIgnoreCase("score")) {
				if (GD.isConsoleSender(sender)) return false;
				if (args.length != 2) {
					sender.sendMessage(ConfigsDaemon.notEnoughArgs);
					return true;
				}
				if (args[1].equalsIgnoreCase("on")) {
					// TODO
				}
				if (args[1].equalsIgnoreCase("off")) {
					// TODO
				}
			}
			
			if (!sender.hasPermission("gsm.pvp.admin")) return false;
			if (args[0].equalsIgnoreCase("zeroize")) {
				ThreadDaemon.async(() -> Rewards.engine.zeroize());
				return true;
			}
			
			if (args[0].equalsIgnoreCase("getrewards")) {
				if (GD.isConsoleSender(sender)) return false;
				GDPlayer p = GD.getGDPlayer(sender);
				ThreadDaemon.async(() -> Rewards.engine.getAllRewards(p));
				return true;
			}
		}
		return true;
	}

}
