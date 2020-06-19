package ru.Baalberith.GameDaemon.Linkage;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import me.clusterstorm.servassist.ServerAssistant;

public class SAAPI {
	
	public static ServerAssistant inst;
	
	public static boolean check() {
		return Bukkit.getPluginManager().isPluginEnabled("ServerAssistant");
	}
	
	public static boolean inCombat(Player p) {
		return check() ? me.clusterstorm.servassist.api.AssistAPI.inCombat(p) : false;
	}
}
