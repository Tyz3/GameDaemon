package ru.Baalberith.GameDaemon.CargoDelivery;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import ru.Baalberith.GameDaemon.ConfigsDaemon;
import ru.Baalberith.GameDaemon.GD;
import ru.Baalberith.GameDaemon.GDPlayer;
import ru.Baalberith.GameDaemon.Extra.ActionObject;
import ru.Baalberith.GameDaemon.Extra.CooldownSystem;
import ru.Baalberith.GameDaemon.Utils.MathOperation;

public class CargoEngine {
	
	public static CargoEngine inst;
	public CargoItems cargoItems;

	private ConfigurationSection m;
	private ConfigurationSection c;
	
	public CargoEngine() {
		inst = this;
		cargoItems = new CargoItems();
	}
	
	public void reload() {
		try {
			cargoItems.reload();
			
			c = ConfigsDaemon.mainConfig.getConfigurationSection("cargo-items");
			m = ConfigsDaemon.messagesConfig.getConfigurationSection("cargo-items");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void activate(GDPlayer p, CargoItem ca) {
		DropPoint dp = ca.getPointByLocation(p.getLocation());
		if (dp == null) {
			Location nearstLoc = ca.getNearestPoint(p.getLocation());
			if (nearstLoc == null) return;
			double dist = MathOperation.distance3D(p.getLocation(), nearstLoc);
			p.sendMessage(m.getString("no-drop-location")
					.replace("[X]", ""+nearstLoc.getBlockX())
					.replace("[Y]", ""+nearstLoc.getBlockY())
					.replace("[Z]", ""+nearstLoc.getBlockZ())
					.replace("[distance]", ""+MathOperation.roundAvoid(dist, 2))
					.replace("&", "\u00a7"));
			return;
		}
		
		ItemStack item = ca.getItem();
		double salePrice = dp.getSalePrice();

		if (p.takeItem(item)) {
			p.addCargoDeliveredAmount(ca.getItem().getType()+"_"+ca.getItem().getDurability(), 1);
			p.sendMessage(m.getString("success-drop").replace("[amount]", ""+salePrice).replace("&", "\u00a7"));
			String cmd = c.getString("cmd-give-money").replace("[amount]", ""+salePrice).replace("[player]", p.getName());
			GD.dispatchCommand(cmd);
			CooldownSystem.add(ActionObject.Cargo, p, 5*60);
		}
	}
}
