package ru.Baalberith.GameDaemon.CNPSsFactions;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import ru.Baalberith.GameDaemon.ConfigsDaemon;
import ru.Baalberith.GameDaemon.GD;
import ru.Baalberith.GameDaemon.GDPlayer;
import ru.Baalberith.GameDaemon.Extra.ActionObject;
import ru.Baalberith.GameDaemon.Extra.CooldownSystem;
import ru.Baalberith.GameDaemon.Utils.ItemDaemon;

public class FactionItems implements Listener {
	
	private List<FactionItem> factionItems;
	private ConfigurationSection c;
	private FactionEngine engine;
	
	public FactionItems() {
		factionItems = new ArrayList<FactionItem>();
		
		Bukkit.getPluginManager().registerEvents(this, GD.inst);
	}
	
	public void reload() {
		try {
			if (!factionItems.isEmpty()) factionItems.clear();
			
			engine = new FactionEngine();
			c = ConfigsDaemon.mainConfig.getConfigurationSection("faction-items");
			
			if (c == null) return;
			
			Set<String> keys = c.getKeys(false);
			for (String k : keys) {
				ItemStack item = ItemDaemon.fromString(k);
				if (item == null) continue;
				boolean increaseMode = c.getBoolean(k+".increase-mode", true);
				int points = c.getInt(k+".points", 0);
				int factionId = c.getInt(k+".faction-id", 0);
				
				factionItems.add(new FactionItem(item, increaseMode, points, factionId));
			}
			GD.log("[TRPGFaction] Loaded "+factionItems.size()+" boost faction items.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@EventHandler
	public void rightClick(PlayerInteractEvent e) {
		if (e.getItem() == null) return;
		if (e.getAction() == Action.LEFT_CLICK_BLOCK || e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.PHYSICAL) return;

		ItemStack item = e.getItem();
		Material t = item.getType();
		if (t == Material.AIR) return;

		FactionItem f = getFactionItem(new ItemStack(t, item.getAmount(), item.getDurability()));
		if (f == null) return;
		
		GDPlayer p = GD.getGDPlayer(e.getPlayer());
		if (!CooldownSystem.isExpired(ActionObject.FactionItem, p)) return;
		
		CooldownSystem.add(ActionObject.FactionItem, p, 3);
		if (p.p.isSneaking()) {
			engine.activateShift(p, f, p.p.getItemInHand().getAmount());
//			GD.log("CNPCsFactions.FactionItems.rightClick("+p.getName()+", shift = "+p.p.getItemInHand().getAmount()+"), ["+time2+" ms]");
		} else {
			engine.activate(p, f);
//			GD.log("CNPCsFactions.FactionItems.rightClick("+p.getName()+"), ["+time2+" ms]");
		}

	}
	
	public FactionItem getFactionItem(ItemStack item) {
		for (FactionItem fi : factionItems)
			if(fi.getItem().getType() == item.getType() && fi.getItem().getDurability() == item.getDurability())
				return fi;
		return null;
	}
}
