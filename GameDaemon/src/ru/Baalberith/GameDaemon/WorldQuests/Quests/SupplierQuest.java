package ru.Baalberith.GameDaemon.WorldQuests.Quests;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import ru.Baalberith.GameDaemon.GD;
import ru.Baalberith.GameDaemon.GDPlayer;
import ru.Baalberith.GameDaemon.Utils.ItemDaemon;
import ru.Baalberith.GameDaemon.Utils.LocationManager;
import ru.Baalberith.GameDaemon.Utils.MathOperation;
import ru.Baalberith.GameDaemon.Utils.ThreadDaemon;
import ru.Baalberith.GameDaemon.WorldQuests.Message;
import ru.Baalberith.GameDaemon.WorldQuests.WorldQuest;

public class SupplierQuest extends WorldQuest {
	
	private int maxStatus;
	private List<ItemStack> cargoItems = new ArrayList<ItemStack>();
	
	// variable data
	public int nowStatus;
	
	@Override
	public void reload(ConfigurationSection c) {
		clear();
		cargoItems.clear();
		
		// Общие настройки
		this.id = c.getName();
		this.type = WorldQuestType.supplier;
		this.name = c.getString("name", Message.supplier_name.get()); 
		this.hologramLocation = LocationManager.deserializeLocation(c.getString("hologram"));
		this.duration = c.getInt("duration", 420);
		
		for (String src : c.getStringList("rewards")) {
			ItemStack reward = ItemDaemon.deSerializeItem(src);
			this.rewards.add(reward);
		}
		
		for (String src : c.getStringList("description")) {
			this.description.add(src.replace("&", "§"));
		}

		// Настройки этого квеста
		this.maxStatus = c.getInt("maxStatus", 10);
		
		for (String src : c.getStringList("cargoItems")) {
			ItemStack cargoItem = ItemDaemon.deSerializeItem(src);
			this.cargoItems.add(cargoItem);
		}
		
		Set<String> areas = c.getConfigurationSection("areas").getKeys(false);
		for (String area : areas) {
			Location center = LocationManager.deserializeLocation(c.getString("areas."+area+".center"));
			if (center == null) continue;
			int diameter = c.getInt("areas."+area+".diameter", -1);
			int height = c.getInt("areas."+area+".height", -1);
			if (diameter == -1 || height == -1) continue;
			
			addArea(center, diameter, diameter, height);
		}
		
	}
	
	@Override
	public void updateHologram() {
		if (hologram == null) return;
		if (hologramLocation == null) {
			deleteHologram();
			return;
		}
		
		hologram.clearLines();
		hologram.addLine(Message.supplier_hologram_title.replace("{name}", name).get());
		hologram.addLine(Message.supplier_hologram_status.replace("{now}", nowStatus).replace("{max}", maxStatus).get());
		hologram.addLine(MathOperation.makeTimeToString(Message.supplier_hologram_duration.get(), expiresMillis-System.currentTimeMillis()));
		description.forEach(desc -> hologram.addLine(desc));
		
		ThreadDaemon.sync(() -> hologram.update());
	}
	
	public boolean checkItemInHand(ItemStack hand) {
		for (ItemStack i : cargoItems) {
			if (i.getType() == hand.getType() && i.getDurability() == hand.getDurability()) return true;
		}
		return false;
	}
	
	public void increaseStatus(Player player, ItemStack hand) {
		GDPlayer p = GD.getGDPlayer(player);
		if (p.takeItem(hand)) {
			giveRewards(p);
			nowStatus++;
			Message.supplier_dropped.send(p);
		}
	}

	@Override
	public void restoreDefaultState() {
		super.restoreDefaultState();
		nowStatus = 0;
	}

	@Override
	public void giveRewardsViaMail() { }
	
	public List<ItemStack> getCargoItems() {
		return cargoItems;
	}
	
	public void addCargoItem(ItemStack item) {
		cargoItems.add(item);
	}
	
	public void removeCargoItem(int id) {
		if (cargoItems.size() <= id) return;
		cargoItems.remove(id);
	}
}
