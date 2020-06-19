package ru.Baalberith.GameDaemon.PeaceNewbies;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;

import ru.Baalberith.GameDaemon.ConfigsDaemon;
import ru.Baalberith.GameDaemon.GD;
import ru.Baalberith.GameDaemon.GDPlayer;
import ru.Baalberith.GameDaemon.PeaceNewbies.Commands.NewbieCMD;

public class Newbies {
	
	private ConfigurationSection c;
	private ConfigurationSection m;
	private Task task;
	private NewbieHandler newbieHandler;
	private NewbieCMD newbie;
	
	public static Newbies inst;
	public static long EXPIRE_TIME;
	public static String MSG_EXPIRED;
	public static String MSG_NOT_NEWBIE;
	public static String MSG_HELP;
	public static String MSG_BROADCAST;
	public static String MSG_POTION_BLOCK;
	public static String MSG_DAMAGE_BLOCK;
	public static String MSG_JOIN;
	
	
	public List<Newbie> newbies;
	public List<Newbie> waitingToAdd;
	public List<Newbie> waitingToRemove;
	
	
	public Newbies() {
		inst = this;
		newbies = new ArrayList<Newbie>();
		waitingToAdd = new ArrayList<Newbie>();
		waitingToRemove = new ArrayList<Newbie>();
		task = new Task();
		newbieHandler = new NewbieHandler();
		newbie = new NewbieCMD();
		
        
		Bukkit.getPluginManager().registerEvents(newbieHandler, GD.inst);
		GD.inst.getCommand("newbie").setExecutor(newbie);
	}
	
	public void reload() {
		try {
			c = ConfigsDaemon.mainConfig.getConfigurationSection("newbies");
			m = ConfigsDaemon.messagesConfig.getConfigurationSection("newbies");
			
			Task.TASK_TIMER = c.getLong("checkEvery", 60)*20;
			EXPIRE_TIME = c.getLong("expireTime", 60)*1000;
			MSG_EXPIRED = m.getString("expired").replace("&", "\u00a7");
			
			MSG_NOT_NEWBIE = m.getString("notNewbie").replace("&", "\u00a7");
			MSG_HELP = m.getString("help").replace("&", "\u00a7");
			MSG_BROADCAST = m.getString("broadcast").replace("&", "\u00a7");
			MSG_POTION_BLOCK = m.getString("potionBlock").replace("&", "\u00a7");
			MSG_DAMAGE_BLOCK = m.getString("damageBlock").replace("&", "\u00a7");
			MSG_JOIN = m.getString("join").replace("&", "\u00a7");
			
			
			task.stop();
			
			newbies.clear();
			waitingToAdd.clear();
			waitingToRemove.clear();
			
			for (GDPlayer p : GD.online) {
				try {
					boolean peaceMode = p.hasPeaceMode();
					if (!peaceMode) continue;
					Newbie newbie = new Newbie(p.getName());
					newbies.add(newbie);
				} catch (ClassCastException e) {
					e.printStackTrace();
				}
			}
			
			GD.log("[PeaceNewbies] Loaded "+newbies.size()+" newbies.");
			
			task.reload();
		} catch (Exception e) {e.printStackTrace();}
	}
	
	public void removeNewbie(String pName) {
		for (Newbie newbie : newbies) {
			if (!newbie.getName().equalsIgnoreCase(pName)) continue;
			waitingToRemove.add(newbie);
			
			newbie.getGDPlayer().p.setDisplayName(newbie.getName());
			newbie.getGDPlayer().setPeaceMode(false);
		}
	}
	
	public Newbie addNewbie(String pName) {
		Newbie newbie = new Newbie(pName);
		waitingToAdd.add(newbie);
		newbie.getGDPlayer().p.setDisplayName("✪"+newbie.getName());
        return newbie;
	}
	
	public Newbie getNewbie(String name) {
		for (Newbie newbie : newbies) {
			if (!newbie.getName().equalsIgnoreCase(name)) continue;
			GDPlayer p = newbie.getGDPlayer();
			if (p == null) continue;
			if (!newbie.isExpired(p)) return newbie;
		}
		return null;
	}
	
	public boolean isNewbie(String name) {
		for (Newbie newbie : newbies) {
			if (newbie.getName().equalsIgnoreCase(name)) return true;
		}
		return false;
	}
	
	
	
}
