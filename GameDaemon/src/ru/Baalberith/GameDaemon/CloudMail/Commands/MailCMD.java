package ru.Baalberith.GameDaemon.CloudMail.Commands;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import ru.Baalberith.GameDaemon.ConfigsDaemon;
import ru.Baalberith.GameDaemon.GD;
import ru.Baalberith.GameDaemon.GDPlayer;
import ru.Baalberith.GameDaemon.GDSender;
import ru.Baalberith.GameDaemon.CloudMail.MailEngine;
import ru.Baalberith.GameDaemon.Extra.Installation.InstallationTemplate;
import ru.Baalberith.GameDaemon.Utils.CompleteHelper;
import ru.Baalberith.GameDaemon.Utils.ThreadDaemon;

public class MailCMD extends InstallationTemplate implements TabCompleter {

	public MailCMD() {
		super("mail");
		GD.inst.getCommand("mail").setTabCompleter(this);
	}

	@Override
	public void reload() {
		
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!sender.hasPermission("gsm.mail")) {
			sender.sendMessage(ConfigsDaemon.noPermission);
			return true;
		}
		
		/*
		 * mail <player> - посмотреть письма другого игрока.
		 * ? mail page <page> - открыть страницу <page> mailbox'а.
		 * 
		 * mail marked <player> <letter_id> - пометить/снять метку прочтения.
		 * mail remove <player> <letter_id> - удалить письмо.
		 * mail send <player> <message> - отправить игроку уведомление.
		 * mail item <player> <message> - отправить игроку уведомление с предметом из ваших рук.
		 */
		
		GDSender s = GD.getGDSender(sender);
		
		if (args.length == 0) {
			ThreadDaemon.async(() -> MailEngine.showMailbox(s, s.getName()));
			return true;
		}
		
		if (args.length == 1) {
			
			if (args[0].equalsIgnoreCase("help")) {
				MailEngine.help(s);
				return true;
			} else if (args[0].equalsIgnoreCase("list")) {
				// TODO - Нужно?
				return true;
			} else {
				ThreadDaemon.async(() -> MailEngine.showMailbox(s, args[0]));
				return true;
			}
		}
		
		if (args.length == 3) {
			if (args[0].equalsIgnoreCase("remove")) {
				ThreadDaemon.async(() -> MailEngine.removeSelectedLetter(s, args[1], args[2]));
				return true;
			} else if (args[0].equalsIgnoreCase("marked")) {
				ThreadDaemon.async(() -> MailEngine.toggleMarkedMessage(s, args[1], args[2]));
				return true;
			} else if (args[0].equalsIgnoreCase("open")) {
				if (GD.isConsoleSender(sender)) return false;
				GDPlayer p = GD.getGDPlayer(sender);
				ThreadDaemon.async(() -> MailEngine.openSelectedLetter(p, args[1], args[2]));
				return true;
			} else if (args[0].equalsIgnoreCase("send")) {
				if (!sender.hasPermission("gsm.mail.moder")) return false;
				MailEngine.sendMessageLetter(s, args[1], args[2]);
				return true;
			} else if (args[0].equalsIgnoreCase("item")) {
				if (GD.isConsoleSender(sender)) return false;
				
				MailEngine.sendParcelLetter((GDPlayer) s, args[1], args[2]);
			}
		}
		
		if (args.length > 3) {
			if (!sender.hasPermission("gsm.mail.moder")) return false;
			
			if (args[0].equalsIgnoreCase("send")) {
				String message = "";
				for (int i = 2; i < args.length; i++) message = message.concat(args[i]);
				MailEngine.sendMessageLetter(s, args[1], message);
				return true;
			} else if (args[0].equalsIgnoreCase("item")) {
				if (GD.isConsoleSender(sender)) return false;
				
				String message = "";
				for (int i = 2; i < args.length; i++) message = message.concat(args[i]);
				MailEngine.sendParcelLetter((GDPlayer) s, args[1], message);
				return true;
			}
		}
		
		
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		
		if (args.length == 1) {
			
			if (sender.hasPermission("gsm.mail.admin")) {
				return CompleteHelper.filter(args, MailEngine.mailboxes.keySet(), "help", "send", "item");
			} else if (sender.hasPermission("gsm.mail.moder")) {
				return CompleteHelper.filter(args, "help", "send", "item");
			} else return CompleteHelper.filter(args, "help");
			
		}

		if (args.length == 2) {
			
			if (sender.hasPermission("gsm.mail.moder")) {
				if (args[0].equalsIgnoreCase("send")) {
					return CompleteHelper.filter(args, MailEngine.mailboxes.keySet());
				} else if (args[0].equalsIgnoreCase("item")) {
					return CompleteHelper.filter(args, MailEngine.mailboxes.keySet());
				}
			}
			
		}
		
		return null;
	}

}
