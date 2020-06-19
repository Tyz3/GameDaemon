package ru.Baalberith.GameDaemon.Clans.Groups.Party.Commands;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import ru.Baalberith.GameDaemon.GD;
import ru.Baalberith.GameDaemon.GDPlayer;
import ru.Baalberith.GameDaemon.Clans.Groups.Party.PartyEngine;
import ru.Baalberith.GameDaemon.Utils.CompleteHelper;
import ru.Baalberith.GameDaemon.Utils.ThreadDaemon;

public class PartyCMD implements CommandExecutor, TabCompleter {
	
	
	public PartyCMD() {
		GD.inst.getCommand("party").setExecutor(this);
		GD.inst.getCommand("party").setTabCompleter(this);
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		if (args.length == 0) {
			PartyEngine.inst.help(sender);
			return true;
		}
		
		// trp help
		// trp leave
		// trp c/chat
		
		if (args.length == 1) {
			if (args[0].equalsIgnoreCase("help")) {
				ThreadDaemon.async(() -> PartyEngine.inst.help(sender));
				return true;
			} else if (args[0].equalsIgnoreCase("createrandom")) {
				if (GD.isConsoleSender(sender)) return false;
				GDPlayer p = GD.getGDPlayer(sender);
				ThreadDaemon.async(() -> PartyEngine.inst.createRandom(p));
				return true;
			} else if (args[0].equalsIgnoreCase("leave")) {
				if (GD.isConsoleSender(sender)) return false;
				GDPlayer p = GD.getGDPlayer(sender);
				ThreadDaemon.async(() -> PartyEngine.inst.leave(p));
				return true;
			} else if (args[0].equalsIgnoreCase("c") || args[0].equalsIgnoreCase("chat")) {
				if (GD.isConsoleSender(sender)) return false;
				GDPlayer p = GD.getGDPlayer(sender);
				ThreadDaemon.async(() -> PartyEngine.inst.toggleChat(p));
				return true;
			} else if (args[0].equalsIgnoreCase("socialspy")) {
				ThreadDaemon.async(() -> PartyEngine.inst.socialspy(sender));
				return true;
			}
			return false;
		}
		
		if (args.length == 2) {
			if (GD.isConsoleSender(sender)) return false;
			GDPlayer p = GD.getGDPlayer(sender);
			if (args[0].equalsIgnoreCase("create")) {
				ThreadDaemon.async(() -> PartyEngine.inst.create(p, args[1]));
				return true;
			} else if (args[0].equalsIgnoreCase("invite")) {
				ThreadDaemon.async(() -> PartyEngine.inst.invite(p, args[1]));
				return true;
			} else if (args[0].equalsIgnoreCase("kick")) {
				ThreadDaemon.async(() -> PartyEngine.inst.kick(p, args[1]));
				return true;
			} else if (args[0].equalsIgnoreCase("join")) {
				ThreadDaemon.async(() -> PartyEngine.inst.join(p, args[1]));
				return true;
			} else if (args[0].equalsIgnoreCase("scoreboard")) {
				ThreadDaemon.async(() -> PartyEngine.inst.scoreboard(p, args[1]));
				return true;
			}
			return false;
		}
		
		
		return false;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		
		if (args.length == 1) {
			if (sender.hasPermission("gsm.party.smoder")) {
				return CompleteHelper.filter(args, "help", "leave", "chat", "create", "createrandom", "invite", "kick", "join", "scoreboard", "socialspy");
			} else return CompleteHelper.filter(args, "help", "leave", "chat", "create", "createrandom", "invite", "kick", "join", "scoreboard");
		}
		
		if (args.length == 2) {
			if (args[0].equalsIgnoreCase("kick")) {
				GDPlayer p = GD.getGDPlayer(sender);
				if (!p.hasParty()) return null;
				return CompleteHelper.filter(args, p.getParty().getMembers());
			} else if (args[0].equalsIgnoreCase("scoreboard")) {
				return CompleteHelper.filter(args, "show", "hide");
			} else if (args[0].equalsIgnoreCase("create")) {
				return CompleteHelper.filter(args, PartyEngine.randomName());
			}
		}
		
		return null;
	}
}
