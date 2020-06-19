package ru.Baalberith.GameDaemon.DropBags;

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
import ru.Baalberith.GameDaemon.DropBags.Bags.FullDropBag;
import ru.Baalberith.GameDaemon.DropBags.Bags.OnceDropBag;
import ru.Baalberith.GameDaemon.DropBags.Bags.OnceDropCommandBag;
import ru.Baalberith.GameDaemon.DropBags.Bags.TwiceDropBag;
import ru.Baalberith.GameDaemon.DropBags.DropBag.BagType;
import ru.Baalberith.GameDaemon.Extra.ActionObject;
import ru.Baalberith.GameDaemon.Extra.CooldownSystem;
import ru.Baalberith.GameDaemon.Utils.ItemDaemon;
import ru.Baalberith.GameDaemon.Utils.ThreadDaemon;

public class DropBags implements Listener {

	private ConfigurationSection c;
	private ConfigurationSection m;
	private List<DropBag> dropBags;
	
	public DropBags() {
		dropBags = new ArrayList<DropBag>();
		Bukkit.getPluginManager().registerEvents(this, GD.inst);
	}
	
	public void reload() {
		try {
			dropBags.clear();
			c = ConfigsDaemon.dropBagsConfig.get();
			m = ConfigsDaemon.messagesConfig.getConfigurationSection("dropBags");
			
			Message.load(m);
			
			Set<String> keys = c.getKeys(false);
			for (String k : keys) {
				
				String name = k;
				ItemStack bag = ItemDaemon.fromString(c.getString(k+".bag"));
				if (bag == null) continue;
				ItemStack key = ItemDaemon.fromString(c.getString(k+".key"));
				
				String type = c.getString(k+".type").toLowerCase();
				if (type == null) continue;
				BagType bagType;
				try {
					bagType = BagType.valueOf(type);
				} catch (IllegalArgumentException e) {continue;}
				
				boolean chestMessage = c.getBoolean(k+".chest", false);
				String bagDisplayName = c.getString(k+".displayName", chestMessage?"сундук":"сумка").replace("&", "§");
				String command = c.getString(k+".command");
				
				if (!c.contains(k+".drop")) continue;
				
				List<Drop> drops = new ArrayList<Drop>();
				Set<String> dropKeys = c.getConfigurationSection(k+".drop").getKeys(false);
				for (String d : dropKeys) {
					String item = c.getString(k+".drop."+d+".item");
					if (item == null || item.equals("")) continue;
					List<String> lore = c.getStringList(k+".drop."+d+".lore");
					if (lore != null && !lore.isEmpty()) item = item.replace("{lore}", ItemDaemon.listToString(lore, ItemDaemon.SEPARATOR));
					List<String> enchs = c.getStringList(k+".drop."+d+".enchantments");
					if (enchs != null && !enchs.isEmpty()) item = item.replace("{enchantments}", ItemDaemon.listToString(enchs, ItemDaemon.SEPARATOR));
					boolean hasBroadcast = c.getBoolean(k+".drop."+d+".hasBroadcast", false);
					double chance = c.getDouble(k+".drop."+d+".chance", 100.0);
					String[] amount = c.getString(k+".drop."+d+".amount").split("-");
					int minAmount = Integer.parseInt(amount[0]);
					int maxAmount = Integer.parseInt(amount.length == 1 ? amount[0] : amount[1]);
					String dropDisplayName = c.getString(k+".drop."+d+".displayName", "предмет").replace("&", "§");
					ItemStack iStack = ItemDaemon.deSerializeItem(item);
					if (iStack == null) continue;
					String permission = c.getString(k+".drop."+d+".permission");
					drops.add(new Drop(iStack, chance, minAmount, maxAmount, hasBroadcast, dropDisplayName, permission));
				}
				GD.log("[DropBags] Loaded "+drops.size()+" drop items.");
				
				DropBag dropBag = null;
				switch (bagType) {
				case full:
					dropBag = new FullDropBag(name, bag, key, drops, chestMessage, bagDisplayName, bagType);
					break;
				case once:
					dropBag = new OnceDropBag(name, bag, key, drops, chestMessage, bagDisplayName, bagType);
					break;
				case twice:
					dropBag = new TwiceDropBag(name, bag, key, drops, chestMessage, bagDisplayName, bagType);
					break;
				case onceCmd:
					dropBag = new OnceDropCommandBag(name, bag, key, drops, chestMessage, bagDisplayName, bagType, command);
					break;
				default:
					continue;
				}
				
				dropBags.add(dropBag);
			}
			GD.log("[DropBags] Loaded "+dropBags.size()+" drop bags.");
		} catch (Exception e) {e.printStackTrace();}
	}
	
	@EventHandler
	public void onRightClick(PlayerInteractEvent e) {
		if (e.getItem() == null) return;
		Action a = e.getAction();
		if (a == Action.LEFT_CLICK_BLOCK || a == Action.LEFT_CLICK_AIR || a == Action.PHYSICAL) return;
		
		ItemStack item = e.getItem();
		Material t = item.getType();
		if (t == Material.AIR) return;
		
		ItemStack bag = new ItemStack(t, 1, item.getDurability());
		DropBag db = getDropBag(bag);
		if (db == null) return;
		GDPlayer p = GD.getGDPlayer(e.getPlayer());
		if (!CooldownSystem.isExpired(ActionObject.DropBag, p)) return;
		
		ThreadDaemon.async(() -> {
			if (p.p.isSneaking())
				db.giveShiftDrop(p, item.getAmount());
			else db.giveDrop(p);
			CooldownSystem.add(ActionObject.DropBag, p, 3);
		});
	}
	
	private DropBag getDropBag(ItemStack bag) {
		for (DropBag db : dropBags) {
			if (db.getBagItem().getType() == bag.getType() && db.getBagItem().getDurability() == bag.getDurability())
				return db;
		}
		return null;
	}
	
}
