package ru.Baalberith.GameDaemon.Warps.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import ru.Baalberith.GameDaemon.ConfigsDaemon;
import ru.Baalberith.GameDaemon.GD;
import ru.Baalberith.GameDaemon.GDPlayer;
import ru.Baalberith.GameDaemon.Extra.ActionObject;
import ru.Baalberith.GameDaemon.Extra.CooldownSystem;
import ru.Baalberith.GameDaemon.Utils.ThreadDaemon;
import ru.Baalberith.GameDaemon.Warps.Message;
import ru.Baalberith.GameDaemon.Warps.WarpEngine;

public class WarpCMD implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		// warp <name>  -  TP
		// warp <name> <player>  -  TP player to a warp
		// warp <name> invite <player>  -  Add player to a whitelist
		// warp <name> uninvite <player>  -  Remove player from a whitelist
		// warp <name> blacklist <player>  -  Add to a blacklist
		// warp <name> unblacklist <player>  -  Remove from a blacklist
		// warp <name> withdraw <amount/all>  -  Withdraw a few coins from warp bank
		// warp <name> fee <amount>  -  Set an amount of fee
		// warp <name> public/private  -  Set a public/private join mode
		// warp <name> movehere  -  Move a tp point of the warp to new location
		
		if (args.length == 0) {
			ThreadDaemon.async(() -> WarpEngine.inst.help(sender));
			return true;
		}
		
		if (args.length == 1) {
			if (args[0].equalsIgnoreCase("help")) {
				ThreadDaemon.async(() -> WarpEngine.inst.help(sender));
				return true;
			} else if (args[0].equalsIgnoreCase("list")) {
				if (GD.isConsoleSender(sender)) return false;
				GDPlayer p = GD.getGDPlayer(sender);
				ThreadDaemon.async(() -> WarpEngine.inst.list(p, 1));
				return true;
			} else {
				if (GD.isConsoleSender(sender)) return false;
				if (!sender.hasPermission("gsm.warps.to")) {
					sender.sendMessage(ConfigsDaemon.noPermission);
					return true;
				}
				GDPlayer p = GD.getGDPlayer(sender);
				if (!CooldownSystem.isExpired(ActionObject.Warp, p)) return true;
				ThreadDaemon.async(() -> WarpEngine.inst.to(p, args[0], true));
				return true;
			}
		}
		
		if (args.length == 2) {
			if (args[1].equalsIgnoreCase("movehere")) {
				if (GD.isConsoleSender(sender)) return false;
				GDPlayer p = GD.getGDPlayer(sender);
				ThreadDaemon.async(() -> WarpEngine.inst.movehere(p, args[0]));
				return true;
			} else if (args[1].equalsIgnoreCase("public")) {
				if (GD.isConsoleSender(sender)) return false;
				GDPlayer p = GD.getGDPlayer(sender);
				ThreadDaemon.async(() -> WarpEngine.inst.setPublic(p, args[0], true));
				return true;
			} else if (args[1].equalsIgnoreCase("private")) {
				if (GD.isConsoleSender(sender)) return false;
				GDPlayer p = GD.getGDPlayer(sender);
				ThreadDaemon.async(() -> WarpEngine.inst.setPublic(p, args[0], false));
				return true;
			} else if (args[1].equalsIgnoreCase("info")) {
				if (GD.isConsoleSender(sender)) return false;
				GDPlayer p = GD.getGDPlayer(sender);
				ThreadDaemon.async(() -> WarpEngine.inst.info(p, args[0]));
				return true;
			} else if (args[0].equalsIgnoreCase("list")) {
				if (GD.isConsoleSender(sender)) return false;
				GDPlayer p = GD.getGDPlayer(sender);
				ThreadDaemon.async(() -> {
					try {
						WarpEngine.inst.list(p, Integer.parseInt(args[1]));
					} catch (NumberFormatException e) {
						sender.sendMessage("Укажи страницу, а не букву, дебил.");
					}
				});
				return true;
			} else if (args[1].equalsIgnoreCase("hide")) {
				if (GD.isConsoleSender(sender)) return false;
				GDPlayer p = GD.getGDPlayer(sender);
				ThreadDaemon.async(() -> WarpEngine.inst.setHide(args[0], p, true));
				return true;
			} else if (args[1].equalsIgnoreCase("show")) {
				if (GD.isConsoleSender(sender)) return false;
				GDPlayer p = GD.getGDPlayer(sender);
				ThreadDaemon.async(() -> WarpEngine.inst.setHide(args[0], p, false));
				return true;
			}
			if (sender.hasPermission("gsm.warps.to.others")) {
				if (GD.isConsoleSender(sender)) return false;
				GDPlayer p1 = GD.getGDPlayer(args[1]);
				if (p1 != null) {
					ThreadDaemon.async(() -> WarpEngine.inst.to(p1, args[0], false));
					return true;
				}
			}
		}

		// warp <name> invite <player>  -  Add player to a whitelist
		// warp <name> uninvite <player>  -  Remove player from a whitelist
		// warp <name> blacklist <player>  -  Add to a blacklist
		// warp <name> unblacklist <player>  -  Remove from a blacklist
		// warp <name> withdraw <amount>  -  Withdraw a few coins from warp bank
		// warp <name> fee <amount>/all  -  Set an amount of fee
		// warp <name> desc - Info about
		if (args.length == 3) {
			if (GD.isConsoleSender(sender)) return false;
			GDPlayer p = GD.getGDPlayer(sender);

			if (args[1].equalsIgnoreCase("invite")) {
				ThreadDaemon.async(() -> WarpEngine.inst.invite(p, args[0], args[2]));
				return true;
			} else if (args[1].equalsIgnoreCase("uninvite")) {
				ThreadDaemon.async(() -> WarpEngine.inst.uninvite(p, args[0], args[2]));
				return true;
			} else if (args[1].equalsIgnoreCase("blacklist")) {
				ThreadDaemon.async(() -> WarpEngine.inst.blacklistAdd(p, args[0], args[2]));
				return true;
			} else if (args[1].equalsIgnoreCase("unblacklist")) {
				ThreadDaemon.async(() -> WarpEngine.inst.blacklistRemove(p, args[0], args[2]));
				return true;
			} else if (args[1].equalsIgnoreCase("desc")) {
				ThreadDaemon.async(() -> WarpEngine.inst.desc(p, args[0], args[2]));
				return true;
			} else if (args[1].equalsIgnoreCase("withdraw")) {
				ThreadDaemon.async(() -> {
					try {
						if (args[2].equalsIgnoreCase("all"))
							WarpEngine.inst.withdrawAll(p, args[0]);
						else WarpEngine.inst.withdraw(p, args[0], Integer.parseInt(args[2]));
					} catch (NumberFormatException e) {
						Message.warp_fee_getThreshold.send(sender);
					}
				});
				return true;
			} else if (args[1].equalsIgnoreCase("fee")) {
				ThreadDaemon.async(() -> {
					try {
						WarpEngine.inst.setFee(p, args[0], Integer.parseInt(args[2]));
					} catch (NumberFormatException e) {
						Message.warp_fee_setThreshold.send(sender);
					}
				});
				return true;
			}
		}
		
		if (args.length > 3) {
			if (GD.isConsoleSender(sender)) return false;
			GDPlayer p = GD.getGDPlayer(sender);
			if (args[1].equalsIgnoreCase("desc")) {
				ThreadDaemon.async(() -> {
					StringBuilder sb = new StringBuilder();
					for (int i = 2; i < args.length; i++) {
						sb.append(args[i]);
						if (i < args.length-1) sb.append(" ");
					}
					WarpEngine.inst.desc(p, args[0], sb.toString());
				});
				return true;
			}
		}

		ThreadDaemon.async(() -> WarpEngine.inst.help(sender));
		return true;
	}
}
