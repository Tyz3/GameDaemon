package ru.Baalberith.GameDaemon.CargoDelivery;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.spigotmc.event.entity.EntityMountEvent;

import ru.Baalberith.GameDaemon.ConfigsDaemon;
import ru.Baalberith.GameDaemon.GD;
import ru.Baalberith.GameDaemon.GDPlayer;
import ru.Baalberith.GameDaemon.Extra.ActionObject;
import ru.Baalberith.GameDaemon.Extra.CooldownSystem;
import ru.Baalberith.GameDaemon.Utils.ItemDaemon;
import ru.Baalberith.GameDaemon.Utils.LocationManager;

public class CargoItems implements Listener {
	
	private Task task;
	
	public List<CargoItem> cargoItems;
	
	private ConfigurationSection m;
	private ConfigurationSection c;
	
	public CargoItems() {
		task = new Task();
		
		Bukkit.getPluginManager().registerEvents(this, GD.inst);
	}
	
	public void reload() {
		cargoItems = new ArrayList<CargoItem>();
		
		m = ConfigsDaemon.messagesConfig.getConfigurationSection("cargo-items");
		c = ConfigsDaemon.cargoItemsConfig.get();
		
		loadCargoItems();
		task.reload();
	}
	
	private void loadCargoItems() {
		Set<String> keys = c.getKeys(false);
		for (String k : keys) {
			String type = c.getString(k + ".type");
			if(type == null) continue;
			
			ItemStack item = ItemDaemon.fromString(type);
			if(item == null) continue;
			
			Set<String> keysPoints = c.getConfigurationSection(k+".dropPoints").getKeys(false);
			List<DropPoint> dropPoints = new ArrayList<DropPoint>();
			for (String kp : keysPoints) {
				Location dropLocation = LocationManager.deserializeLocation(c.getString(k+".dropPoints."+kp+".location"));
				if (dropLocation == null) continue;
				int dropRadius = c.getInt(k+".dropPoints."+kp+".dropRadius", 2);
				double salePrice = c.getDouble(k+".dropPoints."+kp+".salePrice", 1.0);
				if (salePrice == 0) continue;
				dropPoints.add(new DropPoint(dropLocation, dropRadius, salePrice));
			}
			if (dropPoints.isEmpty()) continue;
			
			List<PotionEffect> effects = new ArrayList<PotionEffect>();
			String potions = c.getString(k+".effects");
			if (potions != null) effects = ItemDaemon.deSerializePotionEffect(potions);
			
			cargoItems.add(new CargoItem(item, effects, dropPoints));
		}
		Bukkit.getLogger().info("[TRPGCargo] Loaded "+cargoItems.size()+" cargo packs.");
	}
	
	@EventHandler
	public void onItemClick(PlayerInteractEvent e) {
		if(e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
		ItemStack item = e.getItem();
		if(item == null) return;
		Material t = item.getType();
		if(t == Material.AIR) return;
		CargoItem ca = getCargoItem(new ItemStack(t, item.getAmount(), item.getDurability()));
		if (ca == null) return;
		
		GDPlayer p = GD.getGDPlayer(e.getPlayer());
		if (!CooldownSystem.isExpired(ActionObject.Cargo, p)) return;
		
		CargoEngine.inst.activate(p, ca);
	}
	
	@EventHandler
	public void onTeleport(PlayerTeleportEvent e) {
		
		if (e.getPlayer().isOp()) return;
		String world = e.getPlayer().getWorld().getName();
		if (world.equalsIgnoreCase("world") || world.equalsIgnoreCase("dungeonworld")) return;
		GDPlayer p = GD.getGDPlayer(e.getPlayer());
		if (hasCargoItem(p)) {
			e.setCancelled(true);
			p.sendMessage(m.getString("tp-impossible").replace("&", "\u00a7"));
		}
		
	}

	@EventHandler
	public void onCommand(PlayerCommandPreprocessEvent e) {
		GDPlayer p = GD.getGDPlayer(e.getPlayer());
		if (p == null) return;
		if (p.isOp()) return;
		String world = p.getWorld().getName();
		if (world.equalsIgnoreCase("world") || world.equalsIgnoreCase("dungeonworld")) return;
		if (hasCargoItem(p)) {
			e.setCancelled(true);
			p.sendMessage(m.getString("cmd-impossible").replace("&", "\u00a7"));
		}
	}
	
	@EventHandler
	public void onMount(EntityMountEvent e) {
		if (!(e.getEntity() instanceof Player)) return;
		GDPlayer p = GD.getGDPlayer((Player) e.getEntity());
		
		if (p.isOp()) return;
		String world = p.getWorld().getName();
		if (world.equalsIgnoreCase("world") || world.equalsIgnoreCase("dungeonworld")) return;
		if (hasCargoItem(p)) {
			e.setCancelled(true);
			p.sendMessage(m.getString("mount-impossible").replace("&", "\u00a7"));
		}
	}
	
	private boolean hasCargoItem(GDPlayer p) {
		if (p == null) return false;
		for (CargoItem ca : cargoItems) {
			ItemStack item = ca.getItem();
			if (p.hasItem(item) != -1) return true;
		}
		return false;
	}
	
	private CargoItem getCargoItem(ItemStack item) {
		for (CargoItem cargoItem : cargoItems) {
			if (cargoItem.getItem().getType() == item.getType() && cargoItem.getItem().getDurability() == item.getDurability())
				return cargoItem;
		}
		return null;
	}
}
