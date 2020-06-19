package ru.Baalberith.GameDaemon.Summoning;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;

import ru.Baalberith.GameDaemon.ConfigsDaemon;
import ru.Baalberith.GameDaemon.GD;
import ru.Baalberith.GameDaemon.GDPlayer;
import ru.Baalberith.GameDaemon.Extra.ActionObject;
import ru.Baalberith.GameDaemon.Extra.CooldownSystem;
import ru.Baalberith.GameDaemon.Utils.CountingPattern;
import ru.Baalberith.GameDaemon.Utils.ItemDaemon;
import ru.Baalberith.GameDaemon.Utils.LocationManager;

public class SummonItems implements Listener {
	public static SummonItems inst;
	
	// У всех свитков кулдаун (задержка перез повторным использованием) - 10 секунд
	private int cooldown;
	
	// Все свитки работают только еcли игрок находится на одном из этих блоков
	private int summonRadius;
	
	// Выводить сообщение об оставшемся времени
	private CountingPattern countingPattern;
	
	private List<SummonItem> summonItems = new ArrayList<>();
	
	private ConfigurationSection m;

	protected SummonEngine engine;
	
	public SummonItems() {
		inst = this;
		engine = new SummonEngine();
		
		Bukkit.getPluginManager().registerEvents(this, GD.inst);
	}
	
	public void reload() {
		try {
		engine.stop();
			summonItems.clear();
			
			m = ConfigsDaemon.messagesConfig.getConfigurationSection("summon-items");
			FileConfiguration c = ConfigsDaemon.mainConfig.get();
			cooldown = c.getInt("summon-items.cooldown", 10);
			
			try {
				countingPattern = new CountingPattern(c.getString("summon-items.messageIn", ""));
			} catch(Exception e) {
				countingPattern = new CountingPattern("");
			}
			
			summonRadius = c.getInt("summon-items.summon-radius");
			
			File summonsFile = new File(GD.inst.getDataFolder() + File.separator + "summon_items.yml");
			if(!summonsFile.exists())
			{
				GD.inst.saveResource("summon_items.yml", false);
			}
			FileConfiguration src = YamlConfiguration.loadConfiguration(summonsFile);
			loadSummons(src);
			engine.reload();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void loadSummons(FileConfiguration src) {
		Set<String> keys = src.getKeys(false);
		if(keys != null) for (String k : keys)
		{
			
			String type = src.getString(k + ".type");
			if(type == null) continue;
			
			int globalCooldown = src.getInt(k+".globalCooldown", 0);
			
			ItemStack item = ItemDaemon.fromString(type);
			if(item == null) continue;
			
			int warmup = src.getInt(k + ".warmup");
			String permission = src.getString(k + ".permission");
			Location loc = LocationManager.deserializeLocation(src.getString(k + ".location"));
			if(loc == null) continue;
			List<String> summons = src.getStringList(k + ".summons");
			boolean hasGlobalMessage = src.getBoolean(k+".hasGlobalMessage", false);
			SummonItem s;
			if (hasGlobalMessage) {
				String startGlobalMessage = src.getString(k+".globalMessage.start", "").replace("&", "\u00a7");
				String endGlobalMessage = src.getString(k+".globalMessage.end", "").replace("&", "\u00a7");
				s = new SummonItem(item, globalCooldown, warmup, permission, loc, summons, startGlobalMessage, endGlobalMessage, hasGlobalMessage);
			} else s = new SummonItem(item, warmup, permission, loc, summons);
			
			summonItems.add(s);
		}
		GD.log("[TRPGSummons] Loaded "+summonItems.size()+" summon items.");
	}
	
	
	@EventHandler
	public void onItemClick(PlayerInteractEvent e) {
		if(e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
		ItemStack item = e.getItem();
		if(item == null) return;
		Material t = item.getType();
		if(t == Material.AIR) return;
		
		SummonItem s = getSummonItem(new ItemStack(t, item.getAmount(), item.getDurability()));
		if(s == null) return;
		
		GDPlayer p = GD.getGDPlayer(e.getPlayer());
		
		if(!s.hasPermission(p)) {
			// нет прав
			p.sendMessage(m.getString("not-perms").replace("&", "\u00a7"));
			return;
		}
		
		if (!CooldownSystem.isExpired(ActionObject.Summon, p)) return;
		engine.activate(p, s);
		CooldownSystem.add(ActionObject.Summon, p, 60);
	}
	
	@EventHandler
	public void onMove(PlayerMoveEvent e) {
		GDPlayer p = GD.getGDPlayer(e.getPlayer());
		Task t = engine.getTask(p);
		if(t == null) return;
		if(e.getFrom().distance(e.getTo()) > 0) {
			//p.teleport(e.getFrom());
			t.cancel();
			p.sendMessage(m.getString("summon-cancelled").replace("&", "\u00a7"));
			// Отмена вызова из-за телепортации
		}
	}
	
	@EventHandler
	public void onTeleport(PlayerTeleportEvent e) {
		GDPlayer p = GD.getGDPlayer(e.getPlayer());
		Task t = engine.getTask(p);
		if(t == null) return;
		t.cancel();
		// Отмена вызова из-за телепортации
		p.sendMessage(m.getString("summon-cancelled").replace("&", "\u00a7"));
//			if(e.getFrom().distance(e.getTo()) > 1) {
//				e.setCancelled(true);
//			}
	}
	
	@EventHandler
	public void onToggleSneak(PlayerToggleSneakEvent e) {
		if(!e.isSneaking()) return;
		GDPlayer p = GD.getGDPlayer(e.getPlayer());
		Task t = engine.getTask(p);
		if(t == null) return;
		t.cancel();
		p.sendMessage(m.getString("summon-cancelled").replace("&", "\u00a7"));
		// Отмена вызова из-за использования shift
	}
	
	
	
	
	public SummonItem getSummonItem(ItemStack item) {
		for (SummonItem s : summonItems)
			if(s.getItem().getType() == item.getType() && s.getItem().getDurability() == item.getDurability())
				return s;
		return null;
	}
	
	public int getCooldown() {
		return cooldown;
	}
	
	public boolean isSummonArea(Location pLoc, Location sLoc) {
		int x = pLoc.getBlockX(), y = pLoc.getBlockY(), z = pLoc.getBlockZ();
		int X = sLoc.getBlockX(), Y = sLoc.getBlockY(), Z = sLoc.getBlockZ();
		if ((x >= X-summonRadius && x <= X+summonRadius) && (y >= Y-summonRadius && y <= Y+summonRadius) && (z >= Z-summonRadius && z <= Z+summonRadius))
			return true;
		return false;
	}
	
	public boolean isMessageIn(int i) {
		return countingPattern.contains(i);
	}
}
