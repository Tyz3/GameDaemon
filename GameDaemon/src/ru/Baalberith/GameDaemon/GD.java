package ru.Baalberith.GameDaemon;

import java.io.File;
import java.util.concurrent.CopyOnWriteArrayList;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class GD extends JavaPlugin {
	
	/*
									 ██ ▄█▀ ██▀███   ▒█████   ███▄    █  ▒█████    ██████ 
									 ██▄█▒ ▓██ ▒ ██▒▒██▒  ██▒ ██ ▀█   █ ▒██▒  ██▒▒██    ▒ 
									▓███▄░ ▓██ ░▄█ ▒▒██░  ██▒▓██  ▀█ ██▒▒██░  ██▒░ ▓██▄   
									▓██ █▄ ▒██▀▀█▄  ▒██   ██░▓██▒  ▐▌██▒▒██   ██░  ▒   ██▒
									▒██▒ █▄░██▓ ▒██▒░ ████▓▒░▒██░   ▓██░░ ████▓▒░▒██████▒▒
									▒ ▒▒ ▓▒░ ▒▓ ░▒▓░░ ▒░▒░▒░ ░ ▒░   ▒ ▒ ░ ▒░▒░▒░ ▒ ▒▓▒ ▒ ░
									░ ░▒ ▒░  ░▒ ░ ▒░  ░ ▒ ▒░ ░ ░░   ░ ▒░  ░ ▒ ▒░ ░ ░▒  ░ ░
									░ ░░ ░   ░░   ░ ░ ░ ░ ▒     ░   ░ ░ ░ ░ ░ ▒  ░  ░  ░  
									░  ░      ░         ░ ░           ░     ░ ░        ░  
	*/
	
	public static GD inst;
	
	public static CopyOnWriteArrayList<GDPlayer> online = new CopyOnWriteArrayList<GDPlayer>();
	public static GDSender consoleSender = new GDSender();
	
	public static Storage settings = new Storage(ConfigsDaemon.SETTINGS_FOLDER+".json", ConfigsDaemon.SETTINGS_FOLDER, false);
	
	private static DaemonsRecruiter daemonsRecruiter;
	
	public void onEnable() {
		inst = this;
		daemonsRecruiter = new DaemonsRecruiter();
	}
	
	public void onDisable() {
		daemonsRecruiter.unload();
		online.forEach(o -> o.saveData());
		settings.save();
	}
	
	public void reload() {
		daemonsRecruiter.reload();
		online.forEach(o -> o.saveData());
		settings.save();
	}
	
	public static void sendMessageToNearest(String msg) {
		try {
			online.forEach(p -> p.sendMessage(msg));
		} catch (Exception e) { e.printStackTrace(); }
	}
	
	public static void sendMessageForAll(String msg) {
		try {
			online.forEach(p -> p.sendMessage(msg));
		} catch (Exception e) { e.printStackTrace(); }
	}
	
	public static void broadcast(String msg) {
		Bukkit.broadcastMessage(msg);
	}
	
	public static void broadcast(String[] msg, String... args) {
		for (int i = 0; i < msg.length; i++) {
			broadcast(msg[i], args);
		}
	}
	
	public static void broadcast(String msg, String... args) {
		if (args != null && args.length % 2 == 0 && args.length > 1) {
			for (int i = 0; i < args.length; i+=2) {
				msg = msg.replace(args[i], args[i+1]);
			}
		}
		broadcast(msg);
	}
	
	public static void broadcasts(String... strings) {
		for (int i = 0; i < strings.length; i++) {
			broadcast(strings[i]);
		}
	}
	
	public static GDData getGDData(String name) {
		for (int i = 0; i < online.size(); i++) {
			GDPlayer p = online.get(i);
			if (p.getName().equalsIgnoreCase(name)) return p;
		}
		boolean exists = Bukkit.getOfflinePlayer(name).hasPlayedBefore();
		return exists ? new GDData(name, ConfigsDaemon.STATS_FOLDER) : null;
	}
	
	/**
	 * @param sender игрок или консоль.
	 * @return возвращает класс sender'a для отправки сообщений.
	 */
	public static GDSender getGDSender(CommandSender sender) {
		if (isConsoleSender(sender)) return consoleSender;
		for (int i = 0; i < online.size(); i++) {
			GDPlayer p = online.get(i);
			if (p.getName().equalsIgnoreCase(sender.getName())) return p;
		}
		return null;
	}
	
	/**
	 * @param name имя отправителя сообщений, может быть ник игрока или имя консоли.
	 * @return возвращает класс sender'a для отправки сообщений.
	 */
	public static GDSender getGDSender(String name) {
		if (isConsoleSender(name)) return consoleSender;
		for (int i = 0; i < online.size(); i++) {
			GDPlayer p = online.get(i);
			if (p.getName().equalsIgnoreCase(name)) return p;
		}
		return null;
	}
	
	/**
	 * @param player игрок bukkit сервера
	 * @return возвращает надстройку обычного игрока с расширенным функционалом.
	 */
	public static GDPlayer getGDPlayer(Player player) {
		if (player == null) return null;
		for (int i = 0; i < online.size(); i++) {
			GDPlayer p = online.get(i);
			if (p.getName().equalsIgnoreCase(player.getName())) return p;
		}
		return null;
	}
	
	/**
	 * @param sender отправитель сообщений, может быть как консоль, так и игрок.
	 * @return возвращает надстройку обычного игрока с расширенным функционалом.
	 */
	public static GDPlayer getGDPlayer(CommandSender sender) {
		for (int i = 0; i < online.size(); i++) {
			GDPlayer p = online.get(i);
			if (p.getName().equalsIgnoreCase(sender.getName())) return p;
		}
		return null;
	}
	
	/**
	 * @param name имя игрока, если не найдено, то вернёт null.
	 * @return возвращает надстройку обычного игрока с расширенным функционалом.
	 */
	public static GDPlayer getGDPlayer(String name) {
		Player player = Bukkit.getPlayer(name);
		if (player == null) {
			GDData d = getGDData(name);
			return d == null ? null : new GDPlayer(name, ConfigsDaemon.STATS_FOLDER);
		}
		for (int i = 0; i < online.size(); i++) {
			GDPlayer p = online.get(i);
			if (p.getName().equals(name)) return p;
		}
		return null;
	}
	
	public static void kickAll() {
		for (int i = 0; i < online.size(); i++) {
			online.get(i).kick(GD.inst.getDescription().getDescription());
		}
//		online.forEach(o -> o.kick(GD.inst.getDescription().getDescription()));
	}
	
	public static void removeGDPlayer(GDPlayer player) {
		online.remove(player);
	}
	
	public static GDPlayer addGDPlayer(Player player) {
		GDPlayer p = new GDPlayer(player, ConfigsDaemon.STATS_FOLDER);
		online.add(p);
		return p;
	}
	
	public static void dispatchCommand(String cmd) {
		if (cmd == null) return;
		Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), cmd);
	}
	
	public static void log(String log) {
		Bukkit.getLogger().info(log);
	}
	
	public static String[] getPlayerFiles() {
		File f = new File(".".concat(File.separator).concat(ConfigsDaemon.STATS_FOLDER));
		return f.list();
	}
	
	public static boolean isConsoleSender(CommandSender sender) {
		return sender.getName().equalsIgnoreCase("CONSOLE") ? true : false;
	}
	
	public static boolean isConsoleSender(String name) {
		return name.equalsIgnoreCase("CONSOLE") ? true : false;
	}
	
	public static void sendServerMessage(String message) {
		GD.inst.getServer().getConsoleSender().sendMessage(message.replace("&", "\u00a7"));
	}
}
