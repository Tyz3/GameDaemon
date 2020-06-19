package ru.Baalberith.GameDaemon.Warps;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

import ru.Baalberith.GameDaemon.ConfigsDaemon;
import ru.Baalberith.GameDaemon.Utils.LocationManager;

public class Warps {
	
	
	private ConfigurationSection c;
	
	private List<Warp> warps;
	private boolean firstStart = true;
	
	public Warps() {
		warps = new ArrayList<Warp>();
	}
	
	public void reload() {
		if (!firstStart) {
			saveInFile();
		} else firstStart = false;
		
		warps.clear();
		c = ConfigsDaemon.warpsConfig.get();
		
		Set<String> keys = c.getKeys(false);
		for (String k : keys) {
			Location loc = LocationManager.deserializeLocation(c.getString(k+".location"));
			if (loc == null) continue;
			String owner = c.getString(k+".owner");
			if (owner == null) continue;
			boolean publicWarp = c.getBoolean(k+".public", false);
			
			List<String> whitelist = c.getStringList(k+".whitelist");
			if (whitelist == null) whitelist = new ArrayList<String>();
			
			List<String> blacklist = c.getStringList(k+".blacklist");
			if (blacklist == null) blacklist = new ArrayList<String>();
			
			int fee = c.getInt(k+".fee", 0);
			int bank = c.getInt(k+".bank", 0);
			int weekOfYear = c.getInt(k+".weekOfYear", 0);

			Calendar cal = Calendar.getInstance();
			cal.setTime(new Date());
			int week = cal.get(Calendar.WEEK_OF_YEAR);
			int visits = week != weekOfYear ? 0 : c.getInt(k+".visits", week);
			
			boolean hide = c.getBoolean(k+".hide", false);
			
			
			String desc = c.getString(k+".description", "");
			
			Warp w = new Warp(loc, k, owner, publicWarp, whitelist, blacklist, fee, bank, visits, week, desc, hide);
			w.rebuildHologram();
			warps.add(w);
		}
		
		Bukkit.getLogger().info("[TRPGWarps] Loaded "+warps.size()+" warps.");
	}
	
	public void saveInFile() {
		for (String w : ConfigsDaemon.warpsConfig.get().getKeys(false)) {
			ConfigsDaemon.warpsConfig.get().set(w, null);
		}
		for (Warp w : warps) {
			ConfigsDaemon.warpsConfig.get().set(w.getTitle()+".location", LocationManager.serializeLocation(w.getLocation()));
			ConfigsDaemon.warpsConfig.get().set(w.getTitle()+".owner", w.getOwner());
			ConfigsDaemon.warpsConfig.get().set(w.getTitle()+".public", w.isPublic());
			ConfigsDaemon.warpsConfig.get().set(w.getTitle()+".description", w.getDescription());
			ConfigsDaemon.warpsConfig.get().set(w.getTitle()+".whitelist", w.getWhitelist());
			ConfigsDaemon.warpsConfig.get().set(w.getTitle()+".blacklist", w.getBlacklist());
			ConfigsDaemon.warpsConfig.get().set(w.getTitle()+".fee", w.getFee());
			ConfigsDaemon.warpsConfig.get().set(w.getTitle()+".bank", w.getBank());
			ConfigsDaemon.warpsConfig.get().set(w.getTitle()+".visits", w.getVisits());
			ConfigsDaemon.warpsConfig.get().set(w.getTitle()+".weekOfYear", w.getWeekOfYear());
			ConfigsDaemon.warpsConfig.get().set(w.getTitle()+".hide", w.isHide());
			ConfigsDaemon.warpsConfig.save();
		}
	}
	
	public Warp getWarpInstance(String warpName) {
		for (Warp warp : warps) {
			if (warp.getTitle().equalsIgnoreCase(warpName)) return warp;
		}
		return null;
	}
	
	public int getTotalWarpsByPlayer(String player) {
		int amount = 0;
		for (Warp warp : warps) {
			if (warp.isOwner(player)) amount++;
		}
		return amount;
	}
	
	public int getPublicWarpsByPlayer(String player) {
		int amount = 0;
		for (Warp w : warps) {
			if (w.isOwner(player) && w.isPublic()) amount++;
		}
		return amount;
	}
	
	public int getPrivateWarpsByPlayer(String player) {
		int amount = 0;
		for (Warp w : warps) {
			if (w.isOwner(player) && !w.isPublic()) amount++;
		}
		return amount;
	}
	
	public void setHide(Warp warp, boolean hide) {
		warp.setHide(hide);
	}
	
	public void addWarp(Location location, String title, String owner, boolean publicWarp) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		int week = cal.get(Calendar.WEEK_OF_YEAR);
		warps.add(new Warp(location, title, owner, publicWarp, new ArrayList<String>(), new ArrayList<String>(), 0, 0, 0, week, "", false));
	}
	
	public void removeWarp(Warp warp) {
		warp.removeHologram();
		warps.remove(warp);
	}
	
	public void addToWhitelist(Warp w, String player) {
		w.addToWhitelist(player);
	}
	
	public void removeFromWhitelist(Warp w, String player) {
		w.removeFromWhitelist(player);
	}
	
	public void addToBlacklist(Warp w, String player) {
		w.addToBlacklist(player);
	}
	
	public void removeFromBlacklist(Warp w, String player) {
		w.removeFromBlacklist(player);
	}
	
	public void setAccessMode(Warp w, boolean accessMode) {
		w.setPublic(accessMode);
	}
	
	public void setFee(Warp w, int fee) {
		w.setFee(fee);
	}
	
	public List<Warp> getPlayerInvitedAndOwnedWarps(String player) {
		List<Warp> pWarps = new ArrayList<Warp>();
		warps.stream().forEach(w -> {
			if (!w.isHide() && (w.isOwner(player) || w.isInWhitelist(player))) pWarps.add(w);
		});
		return pWarps;
	}
	
	public List<Warp> getPlayerAvailableWarps(String player) {
		List<Warp> pWarps = new ArrayList<Warp>();
		warps.stream().forEach(w -> {
			if (!w.isHide() && (w.isOwner(player) || w.isInWhitelist(player) || w.isPublic())) pWarps.add(w);
		});
		return pWarps;
	}
	
	public List<Warp> getOwnWarps(String player) {
		List<Warp> pWarps = new ArrayList<Warp>();
		warps.stream().forEach(w -> {
			if (w.isOwner(player) && !w.isHide()) pWarps.add(w);
		});
		return pWarps;
	}
	
	public List<Warp> getAllWarps() {
		List<Warp> pWarps = new ArrayList<Warp>();
		warps.stream().forEach(w -> {
			if (!w.isHide()) pWarps.add(w);
		});
		return pWarps;
	}
	
	public List<Warp> getAllWarpsForOp() {
		return warps;
	}
	
	public void moveLocation(Warp w, Location loc) {
		w.setLocation(loc);
	}
}
