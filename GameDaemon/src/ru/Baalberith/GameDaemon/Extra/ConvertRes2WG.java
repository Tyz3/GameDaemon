package ru.Baalberith.GameDaemon.Extra;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import ru.Baalberith.GameDaemon.GD;

public class ConvertRes2WG {

	private File confResFile;
	private FileConfiguration confRes;

	private File confWGFile;
	private FileConfiguration confWG;
	
	public ConvertRes2WG() {
		loadResConfig();
		loadWGConfig();
		ConfigurationSection c = confRes.getConfigurationSection("Residences");
		ConfigurationSection wg = confWG;
		Set<String> keys = c.getKeys(false);
		for (String k : keys) {
			String player = c.getString(k+".Permissions.OwnerLastKnownName");
			List<String> members = new ArrayList<String>();
			if (c.contains(k+".Permissions.PlayerFlags")) {
				Set<String> keys2 = c.getConfigurationSection(k+".Permissions.PlayerFlags").getKeys(false);
				for (String member : keys2) {
					if (Bukkit.getPlayer(member) == null) continue;
					String name = Bukkit.getPlayer(member).getName();
					if (!name.equalsIgnoreCase(player)) members.add(name);
				}
			}
			int minX = c.getInt(k+".Areas.main.X1");
			int minY = c.getInt(k+".Areas.main.Y1");
			int minZ = c.getInt(k+".Areas.main.Z1");
			int maxX = c.getInt(k+".Areas.main.X2");
			int maxY = c.getInt(k+".Areas.main.Y2");
			int maxZ = c.getInt(k+".Areas.main.Z2");
			
			wg.set("regions."+k+".flags.pvp", "deny");
			wg.set("regions."+k+".flags.mob-damage", "deny");
			wg.set("regions."+k+".flags.creeper-explosion", "deny");
			wg.set("regions."+k+".flags.tnt", "deny");
			wg.set("regions."+k+".owners.players", new ArrayList<String>(Arrays.asList(new String[] {player})));
			wg.set("regions."+k+".members.players", members);
			wg.set("regions."+k+".type", "cuboid");
			wg.set("regions."+k+".priority", 0);
			wg.set("regions."+k+".min.x", (double) minX);
			wg.set("regions."+k+".min.y", (double) minY);
			wg.set("regions."+k+".min.z", (double) minZ);
			wg.set("regions."+k+".max.x", (double) maxX);
			wg.set("regions."+k+".max.y", (double) maxY);
			wg.set("regions."+k+".max.z", (double) maxZ);
			
		}
		saveWGConfig();
	}
	
	private void saveWGConfig() {
		try {
			confWG.save(confWGFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void loadWGConfig() {
		confWGFile = new File(GD.inst.getDataFolder(), "regions.yml");
		if (!confWGFile.exists()) {
			confWGFile.getParentFile().mkdirs();
			try {
				confWGFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
//			GD.inst.saveResource("regions.yml", false);
        }
		confWG = new YamlConfiguration();
		try {
			confWG.load(confWGFile);
		} catch (IOException | InvalidConfigurationException e) {
			e.printStackTrace();
		}
	}
	
	private void loadResConfig() {
		confResFile = new File(GD.inst.getDataFolder(), "res_world.yml");
		if (!confResFile.exists()) {
			confResFile.getParentFile().mkdirs();
			try {
				confResFile.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
		confRes = new YamlConfiguration();
		try {
			confRes.load(confResFile);
		} catch (IOException | InvalidConfigurationException e) {
			e.printStackTrace();
		}
	}
}
