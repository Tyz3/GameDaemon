package ru.Baalberith.GameDaemon.Jail.Commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;

import ru.Baalberith.GameDaemon.ConfigsDaemon;
import ru.Baalberith.GameDaemon.GD;
import ru.Baalberith.GameDaemon.GDPlayer;
import ru.Baalberith.GameDaemon.Jail.ImprisonedPlayer;
import ru.Baalberith.GameDaemon.Jail.ImprisonedPlayers;

public class UnjailCMD implements CommandExecutor {

	private ConfigurationSection m;
	
	
	public UnjailCMD() {
	}
	
	
	public void reload() {
		m = ConfigsDaemon.messagesConfig.getConfigurationSection("violations.jail");
	}
	
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

		// Проверка прав для команды
		if (!sender.hasPermission("gsm.unjail")) {
			sender.sendMessage(m.getString("message.other.no-permission", "не указано в конфиге").replace("&", "\u00a7"));
			return true;
		}
		
		// /unjail {player}
		
		// Проверка целостности команды
		if (args.length == 0 || Bukkit.getPlayer(args[0]) == null) {
			sender.sendMessage(m.getString("info.unjail", "не указано в конфиге").replace("&", "\u00a7"));
			return true;
		}
		GDPlayer p = GD.getGDPlayer(args[0]);
		
		// Существует ли игрок на сервере
		if (p == null || !p.p.hasPlayedBefore()) {
			sender.sendMessage(m.getString("player-not-exist", "не указано в конфиге").replace("&", "\u00a7").replace("[player]", args[0]));
			return true;
		}
		
		// Заключён ли игрок
		if (!isImprisoned(args[0])) {
			sender.sendMessage(m.getString("player-dont-imprisoned", "не указано в конфиге").replace("&", "\u00a7").replace("[player]", args[0]));
			return true;
		}
		
		// Освобождаем игрока
		ImprisonedPlayers.inst.engine.exempt(args[0]);
		sender.sendMessage(m.getString("player-exempted", "не указано в конфиге").replace("[player]", args[0]).replace("&", "\u00a7"));
		if (p.isOnline()) {
			p.sendMessage(m.getString("exempt", "не указано в конфиге").replace("[player]", sender.getName()).replace("&", "\u00a7"));
			p.sendMessage(m.getString("cmd-info").replace("&", "\u00a7"));
			p.setImprisoned(false);
		} else {
			p.setImprisoned(false);
			p.saveData();
		}
		
		return true;
	}
	
	
	private boolean isImprisoned(String name) {
		for (ImprisonedPlayer ip : ImprisonedPlayers.inst.imprisonedPlayers) {
			if (ip.getName().equalsIgnoreCase(name)) return true;
		}
		return false;
	}
}
