package ru.Baalberith.GameDaemon.Jail.Commands;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;

import ru.Baalberith.GameDaemon.ConfigsDaemon;
import ru.Baalberith.GameDaemon.GD;
import ru.Baalberith.GameDaemon.GDPlayer;
import ru.Baalberith.GameDaemon.Jail.ImprisonedPlayer;
import ru.Baalberith.GameDaemon.Jail.ImprisonedPlayers;

public class JailCMD implements CommandExecutor {

	private ConfigurationSection m;
	private ConfigurationSection c;
	private int delay;

	public JailCMD() {
	}
	
	public void reload() {
		m = ConfigsDaemon.messagesConfig.getConfigurationSection("violations.jail");
		c = ConfigsDaemon.mainConfig.getConfigurationSection("violations.jail");
		delay = 1000 * c.getInt("jailback-time", 120);
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		if ((args.length == 0 || args[0].equalsIgnoreCase("info")) && sender.hasPermission("gsm.jail")) {
			for (String string : m.getStringList("info.info"))
				sender.sendMessage(string.replace("[funcPrefix]", ImprisonedPlayers.funcPrefix).replace("&", "\u00a7"));
			if (sender.hasPermission("gsm.jail.admin"))
				for (String string : m.getStringList("info.info-admin"))
					sender.sendMessage(string.replace("&", "\u00a7"));
			return true;
		}
		
		if (args.length == 0) return false;
		
		if (args[0].equalsIgnoreCase("check")) {
			
			if (!sender.hasPermission("gsm.jail.check")) {
				sender.sendMessage(ConfigsDaemon.messagesConfig.get().getString("other.no-permission").replace("&", "\u00a7"));
				return true;
			}
			
			if (args.length < 2) {
				sender.sendMessage(ImprisonedPlayers.funcPrefix+m.getString("info.check"));
				return true;
			}
			
			ImprisonedPlayer ip = ImprisonedPlayers.inst.getImprisonedPlayer(args[1]);
			if (ip == null) {
				sender.sendMessage(ImprisonedPlayers.funcPrefix+m.getString("player-dont-imprisoned").replace("[player]", args[1]).replace("&", "\u00a7"));
				return true;
			}
			
			String punisher = ip.getPunisher();
			String violator = ip.getName();
			String reason = ip.getReason();
			String reasonLevel = m.getString("reason-level").replace("[penalty]", ""+ip.getPenalty());
			long imprisonedDate = ip.getImprisonedDate();
			
			SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
			sender.sendMessage(ImprisonedPlayers.funcPrefix+m.getString("check").
					replace("[punisher]", punisher).
					replace("[violator]", violator).
					replace("[reason]", reason).
					replace("[reasonLevel]", reasonLevel).
					replace("[imprisonedDate]", sdf.format(imprisonedDate)).replace("&", "\u00a7"));
			
			return true;
		}

		if (args[0].equalsIgnoreCase("back")) {
			
			// Открыть инвентарь другого заключённого
			if (args.length == 2 && sender.hasPermission("gsm.jail.back.other")) {
				GDPlayer p = GD.getGDPlayer(sender);
				GDPlayer tp = GD.getGDPlayer(args[1]);
				
				if (tp == null) return false;
				
				if (ImprisonedPlayers.inst.getImprisonedPlayer(args[1]) == null && !ImprisonedPlayers.inst.engine.jailbackDelay.containsKey(args[1])) {
					sender.sendMessage(ImprisonedPlayers.funcPrefix+m.getString("player-dont-imprisoned").replace("[player]", args[1]).replace("&", "\u00a7"));
					return true;
				}
				
				ImprisonedPlayers.inst.engine.activeEditInvs.put(p.getName(), tp.getName());
				p.p.openInventory(ImprisonedPlayers.inst.engine.ri.get(tp));
				
				return true;
			}
			
			if (args.length == 1 && sender.hasPermission("gsm.jail.back")) {
				String player = sender.getName();
				
				// Заключённый ли игрок сейчас
				if (ImprisonedPlayers.inst.getImprisonedPlayer(player) != null) {
					sender.sendMessage(ImprisonedPlayers.funcPrefix+m.getString("you-imprisoned-yet").replace("&", "\u00a7"));
					return true;
				}
				
				// Есть ли у него время на возврат вещей
				if (!ImprisonedPlayers.inst.engine.jailbackDelay.containsKey(player)) {
					sender.sendMessage(ImprisonedPlayers.funcPrefix+m.getString("you-no-imprisoned").replace("&", "\u00a7"));
					return true;
				}
				
				GDPlayer p = GD.getGDPlayer(sender);
				
				// Проверяем доступ к команде
				if (new Date().getTime() >= delay + ImprisonedPlayers.inst.engine.jailbackDelay.get(player)) {
					sender.sendMessage(ImprisonedPlayers.funcPrefix+m.getString("you-no-imprisoned").replace("&", "\u00a7"));
					ImprisonedPlayers.inst.engine.ri.remove(p);
					return true;
				}
				
				p.p.openInventory(ImprisonedPlayers.inst.engine.ri.get(p));
				Date date = new Date();
				date.setTime(delay + ImprisonedPlayers.inst.engine.jailbackDelay.get(player) - new Date().getTime());
				sender.sendMessage(ImprisonedPlayers.funcPrefix+m.getString("jailback-access", ChatColor.RED+"Интвентарь возврата будет действовать ещё [timer] мин." ).replace("[timer]", parseTime(date)).replace("&", "\u00a7"));
				
				return true;
			} else {
				sender.sendMessage(ConfigsDaemon.messagesConfig.get().getString("other.no-permission").replace("&", "\u00a7"));
			}
		}
		
		return true;
	}
	
	private String parseTime(Date date) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("mm мин ss сек");
		return dateFormat.format(date);
	}
}
