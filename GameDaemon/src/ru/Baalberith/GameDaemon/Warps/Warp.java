package ru.Baalberith.GameDaemon.Warps;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import com.gmail.filoghost.holograms.api.Hologram;
import com.gmail.filoghost.holograms.api.HolographicDisplaysAPI;

import ru.Baalberith.GameDaemon.GD;
import ru.Baalberith.GameDaemon.GDPlayer;
import ru.Baalberith.GameDaemon.Extra.ActionObject;
import ru.Baalberith.GameDaemon.Extra.CooldownSystem;
import ru.Baalberith.GameDaemon.Extra.WaitingSystem;
import ru.Baalberith.GameDaemon.Extra.WarmUpSystem;
import ru.Baalberith.GameDaemon.Utils.ThreadDaemon;

public class Warp {
	
	private Location location;
	private String title;
	private String owner;
	private String description;
	private boolean publicWarp;
	private List<String> whitelist;
	private List<String> blacklist;
	private int fee;
	private int bank;
	private int visits;
	private int weekOfYear;
	private boolean hide;
	private Hologram hologram;
	
	public Warp(Location location,
			String title,
			String owner,
			boolean publicWarp,
			List<String> whitelist,
			List<String> blacklist,
			int fee,
			int bank,
			int visits,
			int weekOfYear,
			String description,
			boolean hide) {
		this.location = location;
		this.owner = owner;
		this.publicWarp = publicWarp;
		this.whitelist = whitelist;
		this.blacklist = blacklist;
		this.fee = fee;
		this.bank = bank;
		this.visits = visits;
		this.weekOfYear = weekOfYear;
		this.title = title;
		this.description = description;
		this.hide = hide;
	}
	
	public void removeHologram() {
		if (hologram != null && WarpEngine.hdAPIEnabled) {
			hologram.clearLines();
			hologram.delete();
		}
	}
	
	public void rebuildHologram() {
		removeHologram();
		createHologram();
	}
	
	private void createHologram() {
		if (!WarpEngine.hdAPIEnabled) return;
		if (hide || !publicWarp) return;
		Location loc = location.clone();
		loc.setY(loc.getY()+2.5);
		hologram = HolographicDisplaysAPI.createHologram(GD.inst, loc);
		hologram.addLine(Message.holographic_title.replace("{warp}", title).get());
		hologram.addLine(Message.holographic_visitors.replace("{visitors}", visits).get());
		hologram.addLine(Message.holographic_balance.replace("{balance}", bank).get());
		String[] desc = description.split("!!");
		for (int i = 0; i < desc.length && i < 3; i++) {
			hologram.addLine(desc[i]);
		}
		ThreadDaemon.sync(() -> hologram.update());
	}
	
	public boolean hasFee(GDPlayer p) {
		if (WarpEngine.fee == null) return true;
		return (p.hasItem(WarpEngine.fee, fee) != -1);
	}
	
	public void toWarpWithFee(GDPlayer p, boolean useWarmUp) {
		if (WarpEngine.fee == null) {
			toWarp(p, useWarmUp);
			return;
		}
		WaitingSystem.createRequest(ActionObject.Warp, p, () -> {
			boolean take = p.takeItem(WarpEngine.fee, fee);
			if (take) {
				bank += fee;
				toWarp(p, useWarmUp);
			} else p.sendMessage(Message.warp_fee_notEnough.get().replace("{amount}", ""+fee));
		}, Message.warp_fee_description.get().replace("{amount}", String.valueOf(fee)), true);
	}
	
	public void setLocation(Location loc) {
		this.location = loc;
		rebuildHologram();
	}
	
	public Location getLocation() {
		return location;
	}
	
	public int withdrawAll(GDPlayer p) {
		return withdrawMoney(p, bank);
	}
	
	public int withdrawMoney(GDPlayer p, int amount) {
		int start = amount;
		for (; amount > 0;) {
			if (p.hasSpace(1)) {
				if (amount >= 64) {
					p.giveItem(new ItemStack(WarpEngine.fee.getType(), 64, WarpEngine.fee.getDurability()));
					amount -= 64;
				} else {
					p.giveItem(new ItemStack(WarpEngine.fee.getType(), amount, WarpEngine.fee.getDurability()));
					amount = 0;
				}
			} else break;
		}
		int gave = start - amount;
		bank -= gave;
		rebuildHologram();
		return gave;
	}
	
	public boolean hasFee() {
		return fee == 0 ? false : true;
	}
	
	public int getWhitelistSize() {
		return whitelist.size();
	}
	
	public int getBlacklistSize() {
		return blacklist.size();
	}
	
	public void toWarp(GDPlayer p, boolean useWarmUp) {
		WarmUpSystem.startWarmUp(ActionObject.Warp, p, useWarmUp?WarpEngine.inst.getWarmUpByPerm(p):0, () -> {
			p.teleportSync(location);
			if (!isOwner(p.getName())) increeseVisits();
			CooldownSystem.add(ActionObject.Warp, p, WarpEngine.inst.getCooldownByPerm(p));
		}, false);
	}
	
	public boolean isInWhitelist(String player) {
		return whitelist.contains(player);
	}
	
	public void addToWhitelist(String player) {
		whitelist.add(player);
	}
	
	public void removeFromWhitelist(String player) {
		whitelist.remove(player);
	}
	
	public boolean isInBlacklist(String player) {
		return blacklist.contains(player);
	}
	
	public void addToBlacklist(String player) {
		blacklist.add(player);
	}
	
	public void removeFromBlacklist(String player) {
		blacklist.remove(player);
	}
	
	public boolean isOwner(String player) {
		return owner.equalsIgnoreCase(player);
	}
	
	private void increeseVisits() {
		visits++;
		rebuildHologram();
	}

	public int getVisits() {
		return visits;
	}

	public int getBank() {
		return bank;
	}

	public boolean isPublic() {
		return publicWarp;
	}
	
	public void setPublic(boolean publicWarp) {

		this.publicWarp = publicWarp;
		if (publicWarp) rebuildHologram();
		else removeHologram();
		
	}

	public String getTitle() {
		return title;
	}

	public int getWeekOfYear() {
		return weekOfYear;
	}
	
	public void setFee(int fee) {
		this.fee = fee;
	}
	
	public int getFee() {
		return fee;
	}
	
	public String getOwner() {
		return owner;
	}
	
	public List<String> getWhitelist() {
		return whitelist;
	}
	
	public List<String> getBlacklist() {
		return blacklist;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
		rebuildHologram();
	}

	public boolean isHide() {
		return hide;
	}

	public void setHide(boolean hide) {
		this.hide = hide;
		rebuildHologram();
	}
}
