package ru.Baalberith.GameDaemon.LightLevelingSystem;

import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Inventory;

import ru.Baalberith.GameDaemon.ConfigsDaemon;
import ru.Baalberith.GameDaemon.GD;
import ru.Baalberith.GameDaemon.GDPlayer;
import ru.Baalberith.GameDaemon.LightLevelingSystem.Trait.TraitType;
import ru.Baalberith.GameDaemon.LightLevelingSystem.Commands.LevelCMD;
import ru.Baalberith.GameDaemon.LightLevelingSystem.Commands.LevelMenuCMD;

public class LiLSEngine {
	
	public static LiLSEngine inst;

	public static int maxLevel;
	public static int rewardAmount;
	public static int rewardEvery;
	
	private ExpBoosters boosters = new ExpBoosters();
	
	public LiLSEngine() {
		inst = this;
		Bukkit.getPluginManager().registerEvents(new LiLSHandler(), GD.inst);
		Bukkit.getPluginManager().registerEvents(boosters, GD.inst);
		
		GD.inst.getCommand("levelmenu").setExecutor(new LevelMenuCMD());
		GD.inst.getCommand("level").setExecutor(new LevelCMD());
		
	}
	
	public void reload() {
		try {
			ConfigurationSection c = ConfigsDaemon.mainConfig.getConfigurationSection("lightLevelingSystem");
			ConfigurationSection m = ConfigsDaemon.messagesConfig.getConfigurationSection("lightLevelingSystem");
			Message.load(m);
			
			LiLSHandler.inMenu.forEach(p -> p.closeInventory());
			LiLSHandler.inMenu.clear();
			
			maxLevel = c.getInt("maxLevel", 100);
			rewardAmount = c.getInt("reward.amount", 3);
			rewardEvery = c.getInt("reward.amount", 5);
			
			Traits.reload();
			boosters.reload();
		} catch (Exception e) {e.printStackTrace();}
	}
	
	
	public void openLevelMenu(GDPlayer p) {
		Inventory levelMenu = Bukkit.createInventory(p.getBukkitPlayer(), 27, Message.levelMenuTitle.get());
		
		
		for (Entry<TraitType, Trait> e : Traits.traits.entrySet()) {
			levelMenu.setItem(e.getValue().position, e.getValue().getIcon(p));
		}
		
		LiLSHandler.inMenu.add(p.getBukkitPlayer());
		p.p.openInventory(levelMenu);
	}
	
	public void processClick(GDPlayer p, int clickedSlot) {
		
		// TODO
		
	}
	
	
}
