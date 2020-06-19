package ru.Baalberith.GameDaemon.Sharpening;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;

import ru.Baalberith.GameDaemon.ConfigsDaemon;
import ru.Baalberith.GameDaemon.GD;
import ru.Baalberith.GameDaemon.Sharpening.Commands.SharpCMD;
import ru.Baalberith.GameDaemon.Utils.ThreadDaemon;

public class SharpEngine implements Listener {
	
	public static SharpEngine inst;
	private ConfigurationSection c;
	private ConfigurationSection m;
	private BukkitTask task;
	

	static final ItemStack DEFAULT_SLOT = new ItemStack(Material.STAINED_GLASS_PANE, (byte)1, (short)0);
	static final ItemStack PROGRESS_SLOT = new ItemStack(Material.STAINED_GLASS_PANE, (byte)1, (short)15);
	
	static final int MIN_DUR_LOST = 176;
	static final int MAX_DUR_LOST = 225;
	
	
	static List<Material> materials = new ArrayList<Material>();
	static String separatorBetweenValues;
	static double minChanceToBroadcast;
	static Pattern regex;
	static String loreToAdd;

	static String msgRequiresLvl;
	static String sharpTitle;
	static String msgBroadcast;
	static String msgSuccess;
	static String msgFailure;
	
	private HashMap<Integer, Double> levelsByChance = new HashMap<Integer, Double>();
	private HashMap<String, Task> active = new HashMap<String, Task>();
	
	
	public SharpEngine() {
		inst = this;
		
		GD.inst.getCommand("sharpening").setExecutor(new SharpCMD());
		Bukkit.getPluginManager().registerEvents(this, GD.inst);
	}
	
	public void reload() {
		try {
			c = ConfigsDaemon.mainConfig.getConfigurationSection("sharpening");
			m = ConfigsDaemon.messagesConfig.getConfigurationSection("sharpening");
			
			ThreadDaemon.cancelTask(task);
			
			active.clear();
			materials.clear();
			levelsByChance.clear();
			
			
			msgSuccess = m.getString("success").replace("&", "\u00a7");
			msgFailure = m.getString("failure").replace("&", "\u00a7");
			msgBroadcast = m.getString("broadcast").replace("&", "\u00a7");
			sharpTitle = m.getString("title", "&cРуническое усиление").replace("&", "\u00a7");
			msgRequiresLvl = m.getString("requiresLvl").replace("&", "\u00a7");
			
			regex = Pattern.compile(c.getString("regex").replace("&", "\u00a7"));
			minChanceToBroadcast = c.getDouble("minChanceToBroadcast");
			loreToAdd = c.getString("add").replace("&", "\u00a7");
			separatorBetweenValues = c.getString("separatorBetween");
			
			List<String> mat = c.getStringList("materials");
			for (String s : mat) {
				Material m = Material.matchMaterial(s);
				if (m == null) continue;
				materials.add(m);
			}
			
			Set<String> keys = c.getConfigurationSection("levelsByChance").getKeys(false);
			for (String k : keys) {
				Integer lvl = null;
				try {
					lvl = Integer.parseInt(k);
				} catch (NumberFormatException e) {}
				if (lvl == null) continue;
				double chance = c.getDouble("levelsByChance."+k, 0.0);
				levelsByChance.put(lvl, chance);
			}
			
			// Запуск потока, отвечающего за обновление меню заточки у каждого игрока.
			task = ThreadDaemon.asyncTimer(() -> update(), 10, 10);
		} catch (Exception e) {e.printStackTrace();}
	}
	
	private void update() {
		for (Entry<String, Task> e : active.entrySet()) {
			e.getValue().run();
		}
	}
	
	public int getLevelsPay(double chance) {
		int levels = 0;
		for (Entry<Integer, Double> e : levelsByChance.entrySet()) {
			if (chance <= e.getValue()) levels = e.getKey();
//			else break; // TODO
		}
		return levels;
	}
	
	public double getChanceFromString(String str) {
		double d = Double.parseDouble(str.split(separatorBetweenValues)[1].trim().replace("%", ""));
		return d;
	}
	
	/**
	 * @return Возвращает строку в лоре, подходящей под регулярку regex.
	 */
	public static String getEnhancedString(ItemStack item) {
		if (!item.hasItemMeta()) return null;
		ItemMeta meta = item.getItemMeta();
		if (!meta.hasLore()) return null;
		List<String> lore = meta.getLore();
		for (int i = 0; i < lore.size(); i++) {
			if (regex.matcher(lore.get(i)).matches())
				return lore.get(i);
		}
		return null;
	}
	
	public static boolean checkEnhancedString(String src) {
		return regex.matcher(src).matches();
	}
	
	
	@EventHandler
	public void onOpenSharpeningMenu(InventoryClickEvent e) {
    	if(!(e.getWhoClicked() instanceof Player)) return;
		if (e.getClickedInventory() == null) return;
		if (!e.getClickedInventory().getName().equals(sharpTitle)) return;
		
		ClickType cType = e.getClick();
		if (!(cType.equals(ClickType.NUMBER_KEY) || cType.equals(ClickType.LEFT) || cType.equals(ClickType.RIGHT)
				|| cType.equals(ClickType.SHIFT_LEFT) || cType.equals(ClickType.SHIFT_RIGHT))) {
			e.setCancelled(true);
			return;
		}
		
		int clickedSlot = e.getRawSlot();
		Inventory inv = e.getClickedInventory();
		
		if (clickedSlot == 4 && inv.getItem(clickedSlot).getType() == DEFAULT_SLOT.getType()) {
			e.setCancelled(true);
			return;
		}
		
		if ((clickedSlot > 0 && clickedSlot < 4) || (clickedSlot > 4 && clickedSlot < 8)) e.setCancelled(true);
		
	}
	
	@EventHandler
	public void onCloseSharpeningMenu(InventoryCloseEvent e) {
		if (!e.getInventory().getName().equals(sharpTitle)) return;
		Player p = (Player) e.getPlayer();
		Task t = getStartedTask(p.getName());
		t.saveInventory();
		removeTask(p.getName());
		ThreadDaemon.sync(() -> p.updateInventory());
	}
	
	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		String name = e.getPlayer().getName();
		Task t = getStartedTask(name);
		if (t == null) return;
		removeTask(name);
		t.saveInventory();
	}
	
	public synchronized void addTask(String name, Task t) {
		active.put(name, t);
	}
	
	public synchronized void removeTask(String name) {
		active.remove(name);
	}
	
	public synchronized Task getStartedTask(String name) {
		for (Entry<String, Task> e : active.entrySet()) {
			if (e.getKey().equalsIgnoreCase(name))
				return e.getValue();
		}
		return null;
	}
	
}
