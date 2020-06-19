package ru.Baalberith.GameDaemon.Warps;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import ru.Baalberith.GameDaemon.ConfigsDaemon;
import ru.Baalberith.GameDaemon.GD;
import ru.Baalberith.GameDaemon.GDPlayer;
import ru.Baalberith.GameDaemon.Linkage.HDAPI;
import ru.Baalberith.GameDaemon.Utils.CompleteHelper;
import ru.Baalberith.GameDaemon.Utils.ItemDaemon;
import ru.Baalberith.GameDaemon.Warps.Commands.DelwarpCMD;
import ru.Baalberith.GameDaemon.Warps.Commands.SetwarpCMD;
import ru.Baalberith.GameDaemon.Warps.Commands.WarpCMD;
import ru.Baalberith.GameDaemon.Warps.Commands.WarpsCMD;

public class WarpEngine implements TabCompleter {

	public static WarpEngine inst;
	private ConfigurationSection c;
	
	private Warps warps;
	private SetwarpCMD setwarpCmd;
	private DelwarpCMD delwarpCmd;
	private WarpCMD warpCmd;
	private WarpsCMD warpsCmd;
	
	static ItemStack fee;
	private List<ConfiguredLimit> configuredLimits = new LinkedList<ConfiguredLimit>();
	private List<ConfiguredTimer> configuredTimers = new LinkedList<ConfiguredTimer>();
	private ConfiguredLimit configuredDefaultLimit;
	private ConfiguredTimer configuredDefaultTimer;
	private Pattern pattern;
	private int minLength = 1;
	private int maxLength;
	private List<String> allowedWorlds;
	private int feeSetMin;
	private int feeSetMax;
	private int feeGetMin;
	private int feeGetMax;
	private int warpsOnPage;
	public static boolean hdAPIEnabled = false;
	
	public WarpEngine() {
		inst = this;
		
		warps = new Warps();
		setwarpCmd = new SetwarpCMD();
		delwarpCmd = new DelwarpCMD();
		warpCmd = new WarpCMD();
		warpsCmd = new WarpsCMD();
		allowedWorlds = new ArrayList<String>();
		
		hdAPIEnabled = HDAPI.check();
		
		GD.inst.getCommand("setwarp").setExecutor(setwarpCmd);
		GD.inst.getCommand("delwarp").setExecutor(delwarpCmd);
		GD.inst.getCommand("warp").setExecutor(warpCmd);
		GD.inst.getCommand("warps").setExecutor(warpsCmd);

		GD.inst.getCommand("warp").setTabCompleter(this);
		GD.inst.getCommand("delwarp").setTabCompleter(this);
	}
	
	public void reload() {
		try {
			ConfigurationSection m = ConfigsDaemon.messagesConfig.getConfigurationSection("warps");
			Message.load(m, m.getString("label"));
			c = ConfigsDaemon.mainConfig.getConfigurationSection("warps");
			
			inizializeLimits();
			inizializeTimers();
			
			fee = ItemDaemon.fromString(c.getString("feeItem"));
			
			warps.reload();
		} catch (Exception e) {e.printStackTrace();}
	}
	
	public void saveAllToDisk() {
		warps.saveInFile();
	}
	
	public boolean to(GDPlayer p, String warpName, boolean useWarmup) {
		// Проверка на существование варпа
		Warp w = warps.getWarpInstance(warpName);
		if (w == null) {
			Message.common_notExists.replace("{warp}", warpName).send(p);
			return false;
		}
		
		String pName = p.getName();
		if (!w.isOwner(pName) && !p.hasPermission("gsm.warps.admin")) {
			if (w.isPublic()) {
				if (w.isInBlacklist(pName)) {
					Message.warp_inBlacklist.send(p);
					return false;
				}
				if (w.hasFee()) {
					if (w.isInWhitelist(pName)) {
						w.toWarp(p, useWarmup);
					} else {
						w.toWarpWithFee(p, useWarmup);
					}
				} else w.toWarp(p, useWarmup);
			} else {
				if (!w.isInWhitelist(pName)) {
					Message.warp_notInWhitelist.send(p);
					return false;
				}
				if (w.hasFee()) {
					if (w.isInBlacklist(pName)) {
						w.toWarpWithFee(p, useWarmup);
					} else w.toWarp(p, useWarmup);
				} else w.toWarp(p, useWarmup);
			}
		} else w.toWarp(p, useWarmup);
		
		return true;
	}
	
	public boolean createWarp(GDPlayer p, String warpName) {
		
		if (!p.hasPermission("gsm.warps.setwarp")) {
			p.sendMessage(ConfigsDaemon.noPermission);
			return false;
		}
		
		if (warps.getWarpInstance(warpName) != null) {
			Message.setwarp_alreadyExists.send(p);
			return false;
		}
		
		if (warpName.length() > maxLength || warpName.length() < minLength) {
			Message.setwarp_lengthFail.send(p);
			return false;
		}
		
		if (!pattern.matcher(warpName).matches()) {
			Message.setwarp_nameFail.send(p);
			return false;
		}
		
		if (p.isOp() || p.hasPermission("gsm.warps.admin")) {
			warps.addWarp(p.getLocation(), warpName, p.getName(), false);
			info(p, warpName);
			Message.setwarp_successfully.replace("{warp}", warpName).replace("{public}", Message.common_private.get()).send(p);
			return true;
		}
		
		String worldName = p.getLocation().getWorld().getName();
		if (!allowedWorlds.contains(worldName)) {
			Message.setwarp_disallowedWorld.send(p);
			return false;
		}
		
		int now = warps.getTotalWarpsByPlayer(p.getName());
		if (!canCreateByTotalLimit(p, now)) {
			Message.setwarp_totalLimit.send(p);
			return false;
		}
		int nowPri = warps.getPrivateWarpsByPlayer(p.getName());
		int nowPub = warps.getPublicWarpsByPlayer(p.getName());
		if (!canCreateByPrivateLimit(p, nowPri) && !canCreateByPublicLimit(p, nowPub)) {
			Message.setwarp_privateLimit.send(p);
			return false;
		}
		
		warps.addWarp(p.getLocation(), warpName, p.getName(), false);
		info(p, warpName);
		Message.setwarp_successfully.replace("{warp}", warpName).replace("{public}", Message.common_private.get()).send(p);
		return true;
	}
	
	public boolean deleteWarp(GDPlayer p, String warpName) {
		Warp w = warps.getWarpInstance(warpName);
		if (w == null) {
			Message.common_notExists.replace("{warp}", warpName).send(p);
			return false;
		}
		
		if (p.isOp() || p.hasPermission("gsm.warps.admin")) {
			Message.delwarp_successfully.replace("{warp}", warpName).send(p);
			warps.removeWarp(w);
			return true;
		}
		
		if (!w.isOwner(p.getName())) {
			Message.common_notOwner.replace("{warp}", warpName).send(p);
			return false;
		}
		
		if (w.getBank() > 0) {
			Message.delwarp_bankNotEmpty.replace("{bank}", String.valueOf(w.getBank())).send(p);
			return false;
		}

		
		warps.removeWarp(w);
		Message.delwarp_successfully.replace("{warp}", warpName).send(p);
		return true;
	}
	
	public boolean invite(GDPlayer p, String warpName, String target) {
		Warp w = warps.getWarpInstance(warpName);
		if (w == null) {
			Message.common_notExists.replace("{warp}", warpName).send(p);
			return false;
		}
		
		if (p.isOp() || p.hasPermission("gsm.warps.admin")) {
			Message.warp_whitelist_add.replace("{player}", target).replace("{warp}", warpName).send(p);
			warps.addToWhitelist(w, target);
			return true;
		}
		
		if (!w.isOwner(p.getName())) {
			Message.common_notOwner.replace("{warp}", warpName).send(p);
			return false;
		}
		
		if (w.isInWhitelist(target)) {
			Message.warp_whitelist_alreadyIn.replace("{warp}", warpName).send(p);
			return false;
		}
		
		int now = w.getWhitelistSize();
		if (!canAddByWhitelistLimit(p, now)) {
			Message.warp_whitelistLimit.send(p);
			return false;
		}
		
		warps.addToWhitelist(w, target);
		Message.warp_whitelist_add.replace("{player}", target).replace("{warp}", warpName).send(p);
		return true;
	}
	
	public boolean uninvite(GDPlayer p, String warpName, String target) {
		Warp w = warps.getWarpInstance(warpName);
		if (w == null) {
			Message.common_notExists.replace("{warp}", warpName).send(p);
			return false;
		}
		
		if (p.isOp() || p.hasPermission("gsm.warps.admin")) {
			Message.warp_whitelist_remove.replace("{player}", target).replace("{warp}", warpName).send(p);
			warps.removeFromWhitelist(w, target);
			return true;
		}
		
		if (!w.isOwner(p.getName())) {
			Message.common_notOwner.replace("{player}", target).replace("{warp}", warpName).send(p);
			return false;
		}
		
		if (!w.isInWhitelist(target)) {
			Message.warp_whitelist_removeFail.replace("{player}", target).replace("{warp}", warpName).send(p);
			return false;
		}
		
		warps.removeFromWhitelist(w, target);
		Message.warp_whitelist_remove.replace("{player}", target).replace("{warp}", warpName).send(p);
		return true;
	}
	
	public boolean blacklistAdd(GDPlayer p, String warpName, String target) {
		Warp w = warps.getWarpInstance(warpName);
		if (w == null) {
			Message.common_notExists.replace("{warp}", warpName).send(p);
			return false;
		}
		
		if (p.isOp() || p.hasPermission("gsm.warps.admin")) {
			Message.warp_blacklist_add.send(p);
			warps.addToBlacklist(w, target);
			return true;
		}
		
		if (!w.isOwner(p.getName())) {
			Message.common_notOwner.replace("{warp}", warpName).send(p);
			return false;
		}
		
		if (w.isInBlacklist(target)) {
			Message.warp_blacklist_alreadyIn.replace("{player}", target).replace("{warp}", warpName).send(p);
			return false;
		}
		
		int now = w.getBlacklistSize();
		if (!canAddByBlacklistLimit(p, now)) {
			Message.warp_blacklistLimit.replace("{player}", target).replace("{warp}", warpName).send(p);
			return false;
		}
		
		warps.addToBlacklist(w, target);
		Message.warp_blacklist_add.replace("{player}", target).replace("{warp}", warpName).send(p);
		return true;
	}
	
	public boolean blacklistRemove(GDPlayer p, String warpName, String target) {
		Warp w = warps.getWarpInstance(warpName);
		if (w == null) {
			Message.common_notExists.replace("{warp}", warpName).send(p);
			return false;
		}
		
		if (p.isOp() || p.hasPermission("gsm.warps.admin")) {
			Message.warp_blacklist_remove.replace("{player}", target).replace("{warp}", warpName).send(p);
			warps.removeFromBlacklist(w, target);
			return true;
		}
		
		if (!w.isOwner(p.getName())) {
			Message.common_notOwner.replace("{warp}", warpName).send(p);
			return false;
		}
		
		if (!w.isInBlacklist(target)) {
			Message.warp_blacklist_removeFail.replace("{player}", target).replace("{warp}", warpName).send(p);
			return false;
		}
		
		warps.removeFromBlacklist(w, target);
		Message.warp_blacklist_remove.replace("{player}", target).replace("{warp}", warpName).send(p);
		return true;
	}
	
	public boolean setPublic(GDPlayer p, String warpName, boolean accessMode) {
		Warp w = warps.getWarpInstance(warpName);
		if (w == null) {
			Message.common_notExists.replace("{warp}", warpName).send(p);
			return false;
		}
		
		if (p.isOp() || p.hasPermission("gsm.warps.admin")) {
			Message.warp_accessMode
				.replace("{public}", accessMode ? Message.common_public.get() : Message.common_private.get())
				.replace("{warp}", warpName)
				.send(p);
			warps.setAccessMode(w, accessMode);
			return true;
		}
		
		if (!w.isOwner(p.getName())) {
			Message.common_notOwner.replace("{warp}", warpName).send(p);
			return false;
		}
		
		if (accessMode) {
			int now = warps.getPublicWarpsByPlayer(p.getName());
			if (!canCreateByPublicLimit(p, now)) {
				Message.setwarp_publicLimit.send(p);
				return false;
			}
		} else {
			int now = warps.getPrivateWarpsByPlayer(p.getName());
			if (!canCreateByPrivateLimit(p, now)) {
				Message.setwarp_privateLimit.send(p);
				return false;
			}
		}
		
		warps.setAccessMode(w, accessMode);
		Message.warp_accessMode
			.replace("{public}", accessMode ? Message.common_public.get() : Message.common_private.get())
			.replace("{warp}", warpName)
			.send(p);
		return true;
	}
	
	public boolean setFee(GDPlayer p, String warpName, int fee) {
		if (fee > feeSetMax || fee < feeSetMin) {
			Message.warp_fee_setThreshold.send(p);
			return false;
		}
		
		Warp w = warps.getWarpInstance(warpName);
		if (w == null) {
			Message.common_notExists.replace("{warp}", warpName).send(p);
			return false;
		}
		
		if (p.isOp() || p.hasPermission("gsm.warps.admin")) {
			Message.warp_fee_set.replace("{warp}", warpName).replace("{fee}", ""+fee).send(p);
			w.setFee(fee);
			return true;
		}
		
		if (!w.isOwner(p.getName())) {
			Message.common_notOwner.replace("{warp}", warpName).send(p);
			return false;
		}

		Message.warp_fee_set.replace("{warp}", warpName).replace("{fee}", ""+fee).send(p);
		w.setFee(fee);
		return true;
	}

	public boolean desc(GDPlayer p, String warpName, String desc) {
		Warp w = warps.getWarpInstance(warpName);
		if (w == null) {
			Message.common_notExists.replace("{warp}", warpName).send(p);
			return false;
		}
		
		if (p.isOp() || p.hasPermission("gsm.warps.admin")) {
			Message.warp_descriptionSet.replace("{warp}", warpName).send(p);
			w.setDescription(desc);
			return true;
		}
		
		if (!w.isOwner(p.getName())) {
			Message.common_notOwner.replace("{warp}", warpName).send(p);
			return false;
		}

		Message.warp_descriptionSet.replace("{warp}", warpName).replace("!!", " ").send(p);
		w.setDescription(desc);
		return true;
	}
	
	public boolean withdraw(GDPlayer p, String warpName, int amount) {
		if (amount > feeGetMax || amount < feeGetMin) {
			Message.warp_fee_getThreshold.send(p);
			return false;
		}
		
		Warp w = warps.getWarpInstance(warpName);
		if (w == null) {
			Message.common_notExists.replace("{warp}", warpName).send(p);
			return false;
		}
		
		if (p.isOp() || p.hasPermission("gsm.warps.admin")) {
			Message.warp_fee_get.replace("{warp}", warpName).replace("{amount}", ""+w.withdrawMoney(p, amount)).send(p);
			return true;
		}
		
		if (!w.isOwner(p.getName())) {
			Message.common_notOwner.replace("{warp}", warpName).send(p);
			return false;
		}
		
		Message.warp_fee_get.replace("{warp}", warpName).replace("{amount}", ""+w.withdrawMoney(p, amount)).send(p);
		return true;
	}
	
	public boolean withdrawAll(GDPlayer p, String warpName) {
		Warp w = warps.getWarpInstance(warpName);
		if (w == null) {
			Message.common_notExists.replace("{warp}", warpName).send(p);
			return false;
		}
		
		if (p.isOp() || p.hasPermission("gsm.warps.admin")) {
			Message.warp_fee_get.replace("{warp}", warpName).replace("{amount}", ""+w.withdrawAll(p)).send(p);
			return true;
		}
		
		if (!w.isOwner(p.getName())) {
			Message.common_notOwner.replace("{warp}", warpName).send(p);
			return false;
		}
		
		Message.warp_fee_get.replace("{warp}", warpName).replace("{amount}", ""+w.withdrawAll(p)).send(p);
		return true;
	}
	
	public boolean list(GDPlayer p, int page) {
		List<Warp> warps = p.isOp() || p.hasPermission("gsm.warps.smoder")?this.warps.getAllWarpsForOp():this.warps.getAllWarps();
		try {
			warps.get((page-1)*warpsOnPage);
		} catch (IndexOutOfBoundsException e) {
			Message.warp_list_nothing.send(p);
			return false;
		}
		if (warps.isEmpty()) {
			Message.warp_list_nothing.send(p);
			return false;
		}
		
		Message.warp_list_title.replace("{page}", ""+page).send(p);
		for (int i = 0; i < warpsOnPage; i++) {
			Warp w = null;
			try {
				w = warps.get(i + (page-1)*warpsOnPage);
			} catch (IndexOutOfBoundsException e) {break;}
			if (w == null) break;
			if (w.isHide()) {
				Message.warp_list_hidden.replace("{warp}", w.getTitle())
					.replace("{owner}", w.getOwner())
					.replace("{public}", w.isPublic()?Message.common_public.get():Message.common_private.get())
					.replace("{fee}", ""+w.getFee())
					.replace("{visits}", ""+w.getVisits()).send(p);
			} else if (w.isOwner(p.getName())) {
				Message.warp_list_your.replace("{warp}", w.getTitle())
					.replace("{owner}", w.getOwner())
					.replace("{public}", w.isPublic()?Message.common_public.get():Message.common_private.get())
					.replace("{fee}", ""+w.getFee())
					.replace("{visits}", ""+w.getVisits()).send(p);
			} else if (w.isInWhitelist(p.getName())) {
				Message.warp_list_invited.replace("{warp}", w.getTitle())
					.replace("{owner}", w.getOwner())
					.replace("{public}", w.isPublic()?Message.common_public.get():Message.common_private.get())
					.replace("{fee}", ""+(w.isPublic()?0:w.getFee()))
					.replace("{visits}", ""+w.getVisits()).send(p);
			} else {
				Message.warp_list_all.replace("{warp}", w.getTitle())
					.replace("{owner}", w.getOwner())
					.replace("{public}", w.isPublic()?Message.common_public.get():Message.common_private.get())
					.replace("{fee}", ""+w.getFee())
					.replace("{visits}", ""+w.getVisits()).send(p);
			}
		}
		
		return true;
	}
	
	public boolean warps(GDPlayer p, int page) {
		List<Warp> warps = this.warps.getPlayerInvitedAndOwnedWarps(p.getName());
		try {
			warps.get((page-1)*warpsOnPage);
		} catch (IndexOutOfBoundsException e) {
			Message.warp_list_nothing.send(p);
			return false;
		}
		if (warps.isEmpty()) {
			Message.warp_list_nothing.send(p);
			return false;
		}
		
		Message.warps_title.replace("{page}", ""+page).send(p);
		for (int i = 0; i < warpsOnPage; i++) {
			Warp w = null;
			try {
				w = warps.get(i + (page-1)*warpsOnPage);
			} catch (IndexOutOfBoundsException e) {break;}
			if (w == null) break;

			if (w.isOwner(p.getName())) {
				Message.warps_raw.replace("{warp}", w.getTitle())
				.replace("{visits}", ""+w.getVisits())
				.replace("{public}", w.isPublic()?Message.common_public.get():Message.common_private.get())
				.replace("{fee}", ""+w.getFee()).send(p);
			} else {
				Message.warps_invited.replace("{warp}", w.getTitle())
				.replace("{owner}", w.getOwner())
				.replace("{public}", w.isPublic()?Message.common_public.get():Message.common_private.get())
				.replace("{fee}", ""+(w.isPublic()?0:w.getFee())).send(p);
			}
		}
		
		return true;
	}
	
	public boolean help(CommandSender p) {
		Message.warp_help_title.send(p);
		Message.warp_help_default.send(p);
		if (p.hasPermission("gsm.warps.smoder")) {
			Message.warp_help_admin.send(p);
		}
		return true;
	}
	
	public boolean movehere(GDPlayer p, String warpName) {
		Warp w = warps.getWarpInstance(warpName);
		if (w == null) {
			Message.common_notExists.replace("{warp}", warpName).send(p);
			return false;
		}
		
		if (p.isOp() || p.hasPermission("gsm.warps.admin")) {
			Message.warp_movehere.replace("{warp}", warpName).send(p);
			warps.moveLocation(w, p.getLocation());
			return true;
		}
		
		String worldName = p.getLocation().getWorld().getName();
		if (!allowedWorlds.contains(worldName)) {
			Message.setwarp_disallowedWorld.send(p);
			return false;
		}
		
		warps.moveLocation(w, p.getLocation());
		Message.warp_movehere.replace("{warp}", warpName).send(p);
		return true;
	}
	
	public boolean info(GDPlayer p, String warpName) {
		Warp w = warps.getWarpInstance(warpName);
		if (w == null) {
			Message.common_notExists.replace("{warp}", warpName).send(p);
			return false;
		}
		
		Message.warp_info_title.send(p);
		Message.warp_info_default.replace("{warp}", w.getTitle())
			.replace("{owner}", w.getOwner())
			.replace("{description}", w.getDescription().replace("!!", " "))
			.replace("{visits}", ""+w.getVisits())
			.replace("{public}", w.isPublic()?Message.common_public.get():Message.common_private.get())
			.replace("{fee}", ""+w.getFee()).send(p);
		if (w.isOwner(p.getName()) || p.isOp() || p.hasPermission("gsm.warps.smoder")) {
			Message.warp_info_owner.replace("{whitelist}", listToString(w.getWhitelist()))
				.replace("{blacklist}", listToString(w.getBlacklist()))
				.replace("{bank}", ""+w.getBank())
				.replace("{nowW}", ""+w.getWhitelistSize())
				.replace("{maxW}", ""+getWhitelistLimit(p))
				.replace("{nowB}", ""+w.getBlacklistSize())
				.replace("{maxB}", ""+getBlacklistLimit(p)).send(p);
		}
		if (p.isOp() || p.hasPermission("gsm.warps.smoder")) {
			Message.warp_info_admin.replace("{hide}", w.isHide()?Message.common_hidden.get():Message.common_shown.get()).send(p);
		}
		
		return true;
	}
	
	private String listToString(List<String> whitelist) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < whitelist.size(); i++) {
			sb.append(whitelist.get(i));
			if (i < whitelist.size()-1) {
				sb.append(", ");
			}
		}
		return sb.toString();
	}
	
	public boolean setHide(String warpName, GDPlayer p, boolean hide) {
		Warp w = warps.getWarpInstance(warpName);
		if (w == null) {
			Message.common_notExists.replace("{warp}", warpName).send(p);
			return false;
		}
		
		if (p.isOp() || p.hasPermission("gsm.warps.smoder")) {
			Message.warp_hideMode
				.replace("{hide}", hide?Message.common_hidden.get():Message.common_shown.get())
				.replace("{warp}", warpName).send(p);
			warps.setHide(w, hide);
			return true;
		}
		return false;
	}
	
	
	private void inizializeLimits() {
		configuredLimits.clear();
		allowedWorlds.clear();
		
		pattern = Pattern.compile(c.getString("regex"));
		maxLength = c.getInt("maxLength", 16);
		minLength = c.getInt("minLength", 1);
		allowedWorlds = c.getStringList("allowedWorlds");
		feeSetMin = c.getInt("fee.set.min", 0);
		feeSetMax = c.getInt("fee.set.max", 64);
		feeGetMin = c.getInt("fee.get.min", 1);
		feeGetMax = c.getInt("fee.get.max", 256);
		warpsOnPage = c.getInt("warpsOnPage");
		
		Set<String> keys = c.getConfigurationSection("configuredLimits").getKeys(false);
		for (String k : keys) {
			if (k.equalsIgnoreCase("default")) {
				int whitelist = c.getInt("configuredLimits."+k+".whitelist", 5);
				int blacklist = c.getInt("configuredLimits."+k+".blacklist", 5);
				int totalLimit = c.getInt("configuredLimits."+k+".totalLimit", 0);
				int publicLimit = c.getInt("configuredLimits."+k+".publicLimit", 0);
				int privateLimit = c.getInt("configuredLimits."+k+".privateLimit", 0);
				configuredDefaultLimit = new ConfiguredLimit(k, whitelist, blacklist, totalLimit, publicLimit, privateLimit);
			} else {
				int whitelist = c.getInt("configuredLimits."+k+".whitelist", 5);
				int blacklist = c.getInt("configuredLimits."+k+".blacklist", 5);
				int totalLimit = c.getInt("configuredLimits."+k+".totalLimit", 0);
				int publicLimit = c.getInt("configuredLimits."+k+".publicLimit", 0);
				int privateLimit = c.getInt("configuredLimits."+k+".privateLimit", 0);
				configuredLimits.add(new ConfiguredLimit("gsm.warps.limits."+k, whitelist, blacklist, totalLimit, publicLimit, privateLimit));
			}
		}
		
		Bukkit.getLogger().info("[TRPGWarps] Loaded "+configuredLimits.size()+" configured limits.");
	}
	
	private void inizializeTimers() {
		configuredTimers.clear();
		Set<String> keys = c.getConfigurationSection("configuredTimers").getKeys(false);
		for (String k : keys) {
			if (k.equalsIgnoreCase("default")) {
				int warmUp = c.getInt("configuredTimers."+k+".warmUp", 0);
				int cooldown = c.getInt("configuredTimers."+k+".cooldown", 0);
				configuredDefaultTimer = new ConfiguredTimer("gsm.warps.limits."+k, cooldown, warmUp);
			} else {
				int warmUp = c.getInt("configuredTimers."+k+".warmUp", 0);
				int cooldown = c.getInt("configuredTimers."+k+".cooldown", 0);
				configuredTimers.add(new ConfiguredTimer("gsm.warps.limits."+k, cooldown, warmUp));
			}
		}
		Bukkit.getLogger().info("[TRPGWarps] Loaded "+configuredTimers.size()+" configured timers.");
	}
	
	public int getWarmUpByPerm(GDPlayer p) {
		if (p.hasPermission("gsm.warps.limits.warmup.bypass")) return 0;
		for (ConfiguredTimer cTimer : configuredTimers) {
			if (cTimer.checkPerm(p)) return cTimer.getWarmUp();
		}
		return configuredDefaultTimer.getWarmUp();
	}
	
	public int getCooldownByPerm(GDPlayer p) {
		if (p.hasPermission("gsm.warps.limits.cooldown.bypass")) return 0;
		for (ConfiguredTimer cTimer : configuredTimers) {
			if (cTimer.checkPerm(p)) return cTimer.getCooldown();
		}
		return configuredDefaultTimer.getCooldown();
	}
	
	public boolean canCreateByTotalLimit(GDPlayer p, int now) {
		if (p.hasPermission("gsm.warps.limits.total.bypass")) return true;
		for (ConfiguredLimit cLimit : configuredLimits) {
			if (cLimit.checkPerm(p) && !cLimit.reachedTotalLimit(now))
				return true;
		}
		return !configuredDefaultLimit.reachedTotalLimit(now);
	}
	
	public boolean canCreateByPublicLimit(GDPlayer p, int now) {
		if (p.hasPermission("gsm.warps.limits.public.bypass")) return true;
		for (ConfiguredLimit cLimit : configuredLimits) {
			if (cLimit.checkPerm(p) && !cLimit.reachedPublicLimit(now)) {
				System.out.println("Проверка "+p.getName() + " > "+cLimit.permission);
				return true;
			}
		}
		return !configuredDefaultLimit.reachedPublicLimit(now);
	}
	
	public boolean canCreateByPrivateLimit(GDPlayer p, int now) {
		if (p.hasPermission("gsm.warps.limits.private.bypass")) return true;
		for (ConfiguredLimit cLimit : configuredLimits) {
			if (cLimit.checkPerm(p) && !cLimit.reachedPrivateLimit(now))
				return true;
		}
		return !configuredDefaultLimit.reachedPrivateLimit(now);
	}
	
	public int getWhitelistLimit(GDPlayer p) {
		if (p.hasPermission("gsm.warps.limits.whitelist.bypass")) return -1;
		for (ConfiguredLimit cLimit : configuredLimits) {
			if (cLimit.checkPerm(p))
				return cLimit.getWhitelistLimit();
		}
		return configuredDefaultLimit.getWhitelistLimit();
	}
	
	public int getBlacklistLimit(GDPlayer p) {
		if (p.hasPermission("gsm.warps.limits.blacklist.bypass")) return -1;
		for (ConfiguredLimit cLimit : configuredLimits) {
			if (cLimit.checkPerm(p))
				return cLimit.getBlacklistLimit();
		}
		return configuredDefaultLimit.getBlacklistLimit();
	}
	
	public boolean canAddByWhitelistLimit(GDPlayer p, int now) {
		if (p.hasPermission("gsm.warps.limits.whitelist.bypass")) return true;
		for (ConfiguredLimit cLimit : configuredLimits) {
			if (cLimit.checkPerm(p) && !cLimit.reachedWhitelistLimit(now))
				return true;
		}
		return !configuredDefaultLimit.reachedWhitelistLimit(now);
	}
	
	public boolean canAddByBlacklistLimit(GDPlayer p, int now) {
		if (p.hasPermission("gsm.warps.limits.blacklist.bypass")) return true;
		for (ConfiguredLimit cLimit : configuredLimits) {
			if (cLimit.checkPerm(p) && !cLimit.reachedBlacklistLimit(now))
				return true;
		}
		return !configuredDefaultLimit.reachedBlacklistLimit(now);
	}
	
	// delwarp ...
	// warp ...
	
	String[] names = {"invite", "uninvite", "blacklist",
			"unblacklist", "withdraw", "fee",
			"public", "private", "movehere", "list", "desc", "info"};
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		
		if (args.length == 1) {
			if (cmd.equals(GD.inst.getCommand("warp"))) {
				if (sender.isOp() || sender.hasPermission("gsm.warps.admin")) {
					return filter(args, warps.getAllWarpsForOp(), "list");
				}
				return filter(args, warps.getPlayerAvailableWarps(sender.getName()), "list");
			}
			
			if (cmd.equals(GD.inst.getCommand("delwarp"))) {
				if (sender.isOp() || sender.hasPermission("gsm.warps.admin")) {
					return filter(args, warps.getAllWarpsForOp());
				}
				return filter(args, warps.getOwnWarps(sender.getName()));
			}
		}
		
		if (args.length == 2) {
			if (cmd.equals(GD.inst.getCommand("warp"))) {
				if (args[1].equalsIgnoreCase("info")) {
					return filter(args, warps.getAllWarps());
				}
				if (sender.isOp() || sender.hasPermission("gsm.warps.smoder")) {
					List<String> list = new ArrayList<String>(Arrays.asList(names));
					GD.online.forEach(o -> list.add(o.getName()));
					return CompleteHelper.filter(args,  list);
				}
				return CompleteHelper.filter(args, names);
			}
		}
		
		return null;
	}
	
	private List<String> filter(String[] args, List<Warp> list) {
		String last = args[args.length - 1];
		List<String> r = new ArrayList<String>();
		for(Warp s : list) {
			if(s.getTitle().toLowerCase().startsWith(last.toLowerCase())) r.add(s.getTitle());
		}
		return r;
	}
	
	private List<String> filter(String[] args, List<Warp> list, String... strs) {
		String last = args[args.length - 1];
		List<String> r = new ArrayList<String>();
		for(Warp s : list) {
			if(s.getTitle().toLowerCase().startsWith(last.toLowerCase())) r.add(s.getTitle());
		}
		for (String s : strs) {
			if(s.toLowerCase().startsWith(last.toLowerCase())) r.add(s);
		}
		return r;
	}
}
