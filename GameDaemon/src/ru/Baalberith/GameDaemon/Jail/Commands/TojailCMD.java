package ru.Baalberith.GameDaemon.Jail.Commands;

import java.util.Date;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;

import ru.Baalberith.GameDaemon.ConfigsDaemon;
import ru.Baalberith.GameDaemon.GD;
import ru.Baalberith.GameDaemon.GDPlayer;
import ru.Baalberith.GameDaemon.Jail.ImprisonedPlayer;
import ru.Baalberith.GameDaemon.Jail.ImprisonedPlayers;
import ru.Baalberith.GameDaemon.Utils.LocationManager;

public class TojailCMD implements CommandExecutor {

	private ConfigurationSection m;
	private ConfigurationSection c;
	
	public TojailCMD() {
	}
	
	public void reload() {
		m = ConfigsDaemon.messagesConfig.getConfigurationSection("violations.jail");
		c = ConfigsDaemon.mainConfig.getConfigurationSection("violations.jail");
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		// Проверка прав для команды
		if (!sender.hasPermission("gsm.tojail")) {
			sender.sendMessage(m.getString("message.other.no-permission", "не указано в конфиге").replace("&", "\u00a7"));
			return true;
		}
		
		// /tojail {penalty} {player} {reason}
		
		// Проверка целостности команды
		if (args.length < 3 || c.getString("punish-levels."+args[0]) == null) {
			sender.sendMessage(m.getString("info.tojail", "не указано в конфиге").replace("&", "\u00a7"));
			return true;
		}
		
		GDPlayer p = GD.getGDPlayer(args[1]);
		
		// Существует ли игрок на сервере
		if (p == null || !p.p.hasPlayedBefore()) {
			sender.sendMessage(m.getString("player-not-exist", "не указано в конфиге").replace("&", "\u00a7").replace("[player]", args[1]));
			return true;
		}
		
		// Если игрока недавно освобождали
		if (ImprisonedPlayers.inst.engine.jailbackDelay.containsKey(p.getName())) {
			sender.sendMessage(m.getString("player-recently-exempted", "не указано в конфиге").replace("&", "\u00a7").replace("[player]", args[1]));
			return true;
		}
		
		// Заключён ли игрок
		ImprisonedPlayer ip = ImprisonedPlayers.inst.getImprisonedPlayer(p.getName());
		if (ip != null) {
			sender.sendMessage(m.getString("already-in-jail", "не указано в конфиге").replace("&", "\u00a7").replace("[player]", args[1]));
			sender.sendMessage(getBroadcastMessage(m.getString("check", "не указано в конфиге"), ip));
			return true;
		}
		
		// Создаём заключённого игрока
		String reason = getReasonMessage(args, 2);
		Location location = LocationManager.deserializeLocation(c.getString("punish-levels."+args[0]+".location", "world 0 0 0"));
		short questId = (short) c.getInt("punish-levels."+args[0]+".quest-id", 1);
		
		ip = new ImprisonedPlayer(args[1], Short.parseShort(args[0]), questId, reason, sender.getName(), location, c.getString("permissionsEx-group"), new Date().getTime());
		
		// Сажаем игрока на рудник
		ImprisonedPlayers.inst.engine.imprison(ip, c.getString("quest-cmd"));
		GD.broadcast(getBroadcastMessage(m.getString("broadcast", "не указано в конфиге"), ip));
		if (p.isOnline()) p.sendMessage(m.getString("cmd-info", "не указано в конфиге").replace("&", "\u00a7"));
		
		// Статистика заключений
		p.addImprisonedAmount(1);
		p.setImprisoned(true);
		if (!p.isOnline()) p.saveData();
		
		return true;
	}
	
	private String getBroadcastMessage(String message, ImprisonedPlayer ip) {
		
		return message.replace("[punisher]", ip.getPunisher()).
				replace("[violator]", ip.getName()).
				replace("[reasonLevel]", m.getString("reason-level")).
				replace("[penalty]", ""+ip.getPenalty()).
				replace("[reason]", ip.getReason()).
				replace("&", "\u00a7");
	}
	
	private String getReasonMessage(String[] args, int minIndex) {
		String message = "";

		for (int i = minIndex; i < args.length; i++) {
			message += (i != args.length-1) ? args[i]+" " : args[i];
		}
		return message;
	}
}
