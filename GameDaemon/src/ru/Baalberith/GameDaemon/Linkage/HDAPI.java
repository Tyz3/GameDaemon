package ru.Baalberith.GameDaemon.Linkage;

import org.bukkit.Bukkit;

import com.gmail.filoghost.holograms.api.HolographicDisplaysAPI;

public class HDAPI {
	
	public static HolographicDisplaysAPI inst;
	
	public static boolean check() {
		return Bukkit.getPluginManager().isPluginEnabled("HolographicDisplays");
	}
}
