package ru.Baalberith.GameDaemon.Linkage;

import org.bukkit.Bukkit;

import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;

public class WEAPI {
	
	public static WorldEdit inst = WorldEdit.getInstance();
	
	public static boolean check() {
		return Bukkit.getPluginManager().isPluginEnabled("WorldEdit");
	}
	
	public static LocalSession getSession(String name) {
		return check() ? inst.getSessionManager().findByName(name) : null;
	}

}
