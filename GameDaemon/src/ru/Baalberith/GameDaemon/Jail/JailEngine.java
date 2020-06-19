package ru.Baalberith.GameDaemon.Jail;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitTask;

import ru.Baalberith.GameDaemon.ConfigsDaemon;
import ru.Baalberith.GameDaemon.GD;
import ru.Baalberith.GameDaemon.GDPlayer;
import ru.Baalberith.GameDaemon.Utils.ItemDaemon;
import ru.Baalberith.GameDaemon.Utils.ThreadDaemon;
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

public class JailEngine implements Listener {
	
	public RestoreInventory ri = new RestoreInventory();
	public Map<String, Long> jailbackDelay = new HashMap<String, Long>();
	public Map<String, String> activeEditInvs = new HashMap<String, String>();
	
	private ConfigurationSection c;
	private ConfigurationSection m;
	private ConfigurationSection i;
	
	private long delay;
	private String invTitle;
	private BukkitTask task;
	
	public JailEngine() {
		Bukkit.getPluginManager().registerEvents(this, GD.inst);
	}
	
	public void reload() {
		c = ConfigsDaemon.mainConfig.getConfigurationSection("violations.jail");
		m = ConfigsDaemon.messagesConfig.getConfigurationSection("violations.jail");
		if (!ConfigsDaemon.jailConfig.get().contains("inventories")) {
			ConfigsDaemon.jailConfig.get().createSection("inventories");
			saveData();
		}
		i = ConfigsDaemon.jailConfig.get().getConfigurationSection("inventories");
		
		delay = 1000 * c.getLong("jailback-time");
		invTitle = c.getString("jailback-title").replace("&", "\u00a7");
		
		if (task != null) Bukkit.getScheduler().cancelTask(task.getTaskId());
		task = ThreadDaemon.asyncTimer(() -> cleanDelay(), 0, 20*10);
	}
	
	public void imprison(ImprisonedPlayer ip, String cmd) {
		
		// Получение игрока в PermissionsEx
		PermissionUser user = PermissionsEx.getUser(ip.getName());
		if (user == null) return;
		
		GDPlayer p = ip.getPlayer();
		
		p.p.setGameMode(GameMode.SURVIVAL);
		
		// Сохраняем инвентарь игрока
		ri.save(p);
		
		// Меняем права доступа у игрока
		user.removeGroup("default");
		user.addGroup(ip.getPermissionGroup());
		
		// Если игрок онлайн, то телепортировать его на рудник
		if (p.isOnline())
			p.teleportSync(ip.getLocation());
		
		// Выдаём задание игроку через команду CustomNPCs
		cmd(ip.getName(), ip.getQuestId(), cmd);
		
		ImprisonedPlayers.inst.imprisonedPlayers.add(ip);
		
		// Сохраняем данные игрока в конфиг заключённых
		setData("players."+ip.getName()+".penalty", ip.getPenalty());
		setData("players."+ip.getName()+".questId", ip.getQuestId());
		setData("players."+ip.getName()+".reason", ip.getReason());
		setData("players."+ip.getName()+".punisher", ip.getPunisher());
		setData("players."+ip.getName()+".location", ip.getStringLocation());
		setData("players."+ip.getName()+".imprisonedDate", ip.getImprisonedDate());
		saveData();
		
	}
	
	
	private void cmd(String player, short questId, String cmd) {
		GD.dispatchCommand(cmd.replace("[player]", player).replace("[questId]", ""+questId));
	}
	
	
	public void exempt(String name) {
		
		ImprisonedPlayer ip = ImprisonedPlayers.inst.getImprisonedPlayer(name);
		if (ip == null) return;
		
		// Получение игрока в PermissionsEx
		PermissionUser user = PermissionsEx.getUser(name);
		if (user == null) return;
		
		// Даём доступ к команде /jailback на какое-то время
		jailbackDelay.put(name, new Date().getTime());
		
		// Возвращаем группу default игроку
		user.removeGroup(ip.getPermissionGroup());
		user.addGroup("default");
		
		// Удаляем задание рудника у игрока
		GD.dispatchCommand(c.getString("exempt-cmd", "noppes quest stop [player] [questId]").replace("[player]", ip.getName()).replace("[questId]", ""+ip.getQuestId()));
		
		// Удаляем игрока из списка заключённых
		ImprisonedPlayers.inst.imprisonedPlayers.remove(ip);
		
		// Удаляем данные игрока из конфига заключённых
		removeData("players."+name);
		saveData();
	}
	
	private void setData(String path, Object object) {
		ConfigsDaemon.jailConfig.get().set(path, object);
	}
	
	private void removeData(String path) {
		ConfigsDaemon.jailConfig.get().set(path, null);
	}
	
	private void saveData() {
		ConfigsDaemon.jailConfig.save();
	}
	
	// Взаимодействие с инвентарём игрока
	public class RestoreInventory {
		
		public void remove(GDPlayer p) {
			removeData("inventories."+p.getName());
			saveData();
		}
		
		public Inventory get(GDPlayer p) {
			Inventory restoreInv = Bukkit.createInventory((InventoryHolder) p.getBukkitPlayer(), 9*5, invTitle);
			List<String> items = i.getStringList(p.getName());
			
			for (int i = 0; i < items.size(); i++) {
				restoreInv.setItem(i, ItemDaemon.deSerializeItem(items.get(i)));
			}
			
			return restoreInv;
		}
		
		public void update(GDPlayer p, Inventory inv) {
			ThreadDaemon.sync(() -> {
				ArrayList<String> itemsLog = new ArrayList<String>();
				ItemStack[] items = inv.getContents();
				
				for (int i = 0; i < items.length; i++) {
					String element = ItemDaemon.serializeItem(items[i], false);
					if (element == null) continue;
					
					itemsLog.add(element);
				}
				
				setData("inventories."+p.getName(), itemsLog);
				saveData();
			});
		}
		
		public void save(GDPlayer p) {
			ThreadDaemon.async(() -> {
				PlayerInventory inv = p.p.getInventory();
				ArrayList<String> itemsLog = new ArrayList<String>();
				ItemStack[] items = inv.getContents();
				ItemStack[] itemsArmor = inv.getArmorContents();

				p.p.getInventory().clear();
				
				for (int i = 0; i < itemsArmor.length; i++) {
					if (itemsArmor[i] == null || itemsArmor[i].getType().equals(Material.AIR)) continue;
					
					itemsLog.add(ItemDaemon.serializeItem(itemsArmor[i], false));
				}
				
				for (int i = 0; i < items.length; i++) {
					if (items[i] == null) continue;
					
					itemsLog.add(ItemDaemon.serializeItem(items[i], false));
				}
				
				p.p.getInventory().setArmorContents(new ItemStack[] {new ItemStack(Material.AIR), new ItemStack(Material.AIR), new ItemStack(Material.AIR), new ItemStack(Material.AIR)});
				
				setData("inventories."+p.getName(), itemsLog);
				saveData();
			});
		}
		
	}
	
	private void cleanDelay() {
		Set<String> keys = jailbackDelay.keySet();
		for (String player : keys) {
			if (new Date().getTime() < delay+jailbackDelay.get(player)) continue;
			
			GDPlayer p = GD.getGDPlayer(player);
			jailbackDelay.remove(player);
			ri.remove(p);
			
			p.sendMessage(m.getString("jailback-expired").replace("&", "\u00a7"));
			if (p.p.getOpenInventory() == null) continue;
			if (p.p.getOpenInventory().getTitle().equalsIgnoreCase(invTitle))
				p.p.closeInventory();
		}
	}
	
	@EventHandler
	public void join(PlayerJoinEvent e) {
		ImprisonedPlayer ip = ImprisonedPlayers.inst.getImprisonedPlayer(e.getPlayer().getName());
		if (ip != null) {
			e.getPlayer().teleport(ip.getLocation());
		}
	}
	
	@EventHandler
    public void quit(PlayerQuitEvent e) {
    	if (jailbackDelay.containsKey(e.getPlayer().getName())) {
    		String player = e.getPlayer().getName();
    		if (new Date().getTime() >= delay+jailbackDelay.get(player)) {
    			jailbackDelay.remove(player);
    			ri.remove(GD.getGDPlayer(e.getPlayer()));
    		}
    	}
    }

	@EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
    	Inventory inv = e.getInventory();
    	
    	if (inv.getName().equalsIgnoreCase(invTitle)) {
    		if (jailbackDelay.containsKey(e.getPlayer().getName())) {
    			ImprisonedPlayers.inst.engine.ri.update(GD.getGDPlayer((Player) e.getPlayer()), inv);
    		}
    		if (e.getPlayer().hasPermission("gsm.jail.back.other")) {
    			if (activeEditInvs.containsKey(e.getPlayer().getName())) {
    				GDPlayer p = GD.getGDPlayer(activeEditInvs.get(e.getPlayer().getName()));
        			ImprisonedPlayers.inst.engine.ri.update(p, inv);
        			activeEditInvs.remove(e.getPlayer().getName());
    			}
    		}
    		
    	}
    			
    }
}
