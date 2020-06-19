package ru.Baalberith.GameDaemon.Extra;

import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class BukkitConfig {

	private FileConfiguration config = new YamlConfiguration();
	private File configFile;
	private JavaPlugin instance;
	private File folder;
	private String name;
	
	public BukkitConfig(JavaPlugin instance, File folder, String name) {
		this.instance = instance;
		this.folder = folder;
		this.name = name;
	}
	
	public static BukkitConfig create(JavaPlugin instance, File folder, String name) {
		return new BukkitConfig(instance, folder, name);
	}
	
	public void reload() {
		configFile = new File(folder, name);
		if (!configFile.exists() || configFile.length() == 0) {
			configFile.getParentFile().mkdirs();
			instance.saveResource(name, false);
        }
		config = new YamlConfiguration();
		try {
			config.load(configFile);
		} catch (IOException | InvalidConfigurationException e) {
			e.printStackTrace();
		}
	}
	
	public void save() {
		try {
			config.save(configFile);
		} catch (IOException e) { e.printStackTrace(); }
	}
	
	public FileConfiguration get() {
        return config;
    }
	
	public ConfigurationSection getConfigurationSection(String arg0) {
		return config.getConfigurationSection(arg0);
	}
}
