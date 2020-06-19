package ru.Baalberith.GameDaemon.RPGSpawn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import ru.Baalberith.GameDaemon.ConfigsDaemon;
import ru.Baalberith.GameDaemon.GD;
import ru.Baalberith.GameDaemon.GDPlayer;
import ru.Baalberith.GameDaemon.Extra.ActionObject;
import ru.Baalberith.GameDaemon.Extra.WarmUpSystem;
import ru.Baalberith.GameDaemon.RPGSpawn.Commands.RpgCMD;
import ru.Baalberith.GameDaemon.Utils.LocationManager;

public class SpawnEngine implements TabCompleter {
	
	public static SpawnEngine inst;
	private FileConfiguration c;
	private ConfigurationSection m;
	
	private RpgCMD rpgCmd = new RpgCMD();
	private List<String> rpgSpawns = new ArrayList<String>();
	public Cmd Cmd = new Cmd();
	private int warmup;
	
	public SpawnEngine() {
		inst = this;
		GD.inst.getCommand("rpg").setExecutor(rpgCmd);
		GD.inst.getCommand("rpg").setTabCompleter(inst);
	}
	
	public void reload() {
		try {
			rpgSpawns.clear();
			c = ConfigsDaemon.rpgSpawnsConfig.get();
			m = ConfigsDaemon.messagesConfig.getConfigurationSection("rpg-spawn");
			warmup = ConfigsDaemon.mainConfig.get().getInt("rpg-spawn.warmup", 6);
			rpgSpawns.addAll(c.getKeys(false));
		} catch (Exception e) {e.printStackTrace();}
	}
	
	public boolean exec(CommandSender sender) {
		GDPlayer p = GD.getGDPlayer(sender);
		String name = p.getRpgSpawn(); // (String) JSONStorage.get(GSM.STATS_FOLDER, sender.getName(), "general.rpgSpawn");
		String title = (name == null || !rpgSpawns.contains(name)) ? ConfigsDaemon.defaultRpgSpawn : name;
		
		Location l = LocationManager.deserializeLocation(c.getString(title+".location"));
		if (l == null) {
			p.sendMessage(m.getString("point-deleted").replace("[title]", title).replace("&", "\u00a7"));
			return true;
		}
		
		WarmUpSystem.startWarmUp(ActionObject.RpgSpawn, p, warmup, () -> tp(l, p, title), false);
		return true;
	}
	
	public void tp(Location l, GDPlayer p, String title) {
		p.teleportSync(l);
		p.sendMessage(m.getString("exec").replace("[title]", title).replace("&", "\u00a7"));
	}
	
	public class Cmd {
		public boolean add(CommandSender sender, String title) {
			if (sender.getName().equalsIgnoreCase("console")) return false;
			if (rpgSpawns.contains(title)) {
				sender.sendMessage(m.getString("already-exists").replace("[title]", title).replace("&", "\u00a7"));
				return true;
			}
			GDPlayer p = GD.getGDPlayer(sender);
			
			Location loc = p.getLocation();
			ConfigsDaemon.rpgSpawnsConfig.get().set(title+".location", loc.getWorld().getName()+" "+loc.getBlockX()+" "+loc.getBlockY()+" "+loc.getBlockZ()+" "+loc.getYaw()+" "+loc.getPitch());
			ConfigsDaemon.rpgSpawnsConfig.save();
			rpgSpawns.add(title);
			sender.sendMessage(m.getString("add").replace("[title]", title).replace("[world]", ""+loc.getWorld().getName()).replace("[X]", ""+loc.getBlockX()).replace("[Y]", ""+loc.getBlockY()).replace("[Z]", ""+loc.getBlockZ()).replace("&", "\u00a7"));
			return true;
		}
		
		public boolean remove(CommandSender sender, String title) {
			if (!rpgSpawns.contains(title)) {
				sender.sendMessage(m.getString("not-exists").replace("[title]", title).replace("&", "\u00a7"));
				return true;
			}
			ConfigsDaemon.rpgSpawnsConfig.get().set(title, null);
			ConfigsDaemon.rpgSpawnsConfig.save();
			rpgSpawns.remove(title);
			sender.sendMessage(m.getString("remove").replace("[title]", title).replace("&", "\u00a7"));
			return true;
		}
		
		public boolean change(CommandSender sender, String player, String newTitle) {
			if (!rpgSpawns.contains(newTitle)) {
				sender.sendMessage(m.getString("not-exists").replace("[title]", newTitle).replace("&", "\u00a7"));
				return true;
			}
			GDPlayer p = GD.getGDPlayer(player);
			if (p == null) {
				sender.sendMessage("Игрок не найден в базе данных.");
				return true;
			}
			p.setRpgSpawn(newTitle);
			Location l = LocationManager.deserializeLocation(c.getString(newTitle+".location"));
			sender.sendMessage(m.getString("change").replace("[title]", newTitle).replace("[player]", player).replace("[world]", l.getWorld().getName()).replace("[X]", ""+l.getBlockX()).replace("[Y]", ""+l.getBlockY()).replace("[Z]", ""+l.getBlockZ()).replace("&", "\u00a7"));
			return true;
		}
		
		public boolean info(CommandSender sender) {
			for (String str : m.getStringList("info")) 
				sender.sendMessage(str.replace("&", "\u00a7"));
			return true;
		}
		
		public boolean list(CommandSender sender) {
			for (String k : c.getKeys(false)) {
				Location l = LocationManager.deserializeLocation(c.getString(k+".location"));
				if (l == null) return false;
				sender.sendMessage(m.getString("list").replace("[title]", k).replace("[world]", l.getWorld().getName()).replace("[X]", ""+l.getBlockX()).replace("[Y]", ""+l.getBlockY()).replace("[Z]", ""+l.getBlockZ()).replace("&", "\u00a7"));
			}
			return true;
		}
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender.isOp()) {
			if (args.length == 1) {
				return Arrays.asList("info","list","add","remove","change");
			}
			if (args.length == 2 && args[0].equalsIgnoreCase("remove")) {
				return rpgSpawns;
			}
			if (args.length > 1 && args[0].equalsIgnoreCase("add")) {
				return new ArrayList<String>();
			}
			if (args.length == 3 && args[0].equalsIgnoreCase("change")) {
				return rpgSpawns;
			}
		}
		return null;
	}
}
