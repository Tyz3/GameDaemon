package ru.Baalberith.GameDaemon.Commands;

import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import ru.Baalberith.GameDaemon.ConfigsDaemon;
import ru.Baalberith.GameDaemon.GD;
import ru.Baalberith.GameDaemon.Extra.ActionObject;
import ru.Baalberith.GameDaemon.Extra.CooldownSystem;

public class CoinsConverter implements CommandExecutor {
	
	public CoinsConverter() {
		GD.inst.getCommand("convert").setExecutor(this);
	}
	
	private void convert64To1(Player p, ConfigurationSection conf, Set<String> keys) {
		
		for (String k : keys) {
			ItemStack from = new ItemStack(Material.getMaterial(k.split(":")[0]), 64, (short) Short.parseShort(k.split(":")[1]));
			
			if (p.getItemInHand().equals(from)) {
				ItemStack to = new ItemStack(Material.getMaterial(conf.getString(k+".to").split(":")[0]), 1, (short) Short.parseShort(conf.getString(k+".to").split(":")[1]));
				p.getItemInHand().setType(to.getType());
				p.getItemInHand().setAmount(to.getAmount());
				p.getItemInHand().setDurability(to.getDurability());
				
				p.sendMessage(conf.getString(k+".message-to").replace("&", "\u00a7"));
				return;
			}
		}
	}
	
	private void convert1To64(Player p, ConfigurationSection conf, Set<String> keys) {
		//
		for (String k : keys) {
			ItemStack from = new ItemStack(Material.getMaterial(conf.getString(k+".to").split(":")[0]), 1, (short) Short.parseShort(conf.getString(k+".to").split(":")[1]));
			
			if (p.getItemInHand().equals(from)) {
				ItemStack to = new ItemStack(Material.getMaterial(k.split(":")[0]), 64, (short) Short.parseShort(k.split(":")[1]));
				p.getItemInHand().setType(to.getType());
				p.getItemInHand().setAmount(to.getAmount());
				p.getItemInHand().setDurability(to.getDurability());
				
				p.sendMessage(conf.getString(k+".message-back").replace("&", "\u00a7"));
				return;
			}
		}
	}
	
	private int isInInventory(Player p, ConfigurationSection conf, Set<String> keys) {
		
		String itemName = p.getItemInHand().getType()+":"+p.getItemInHand().getDurability();
		
		if (p.getItemInHand().getAmount() == 1) {
			for (String string : keys) {
				if (itemName.equalsIgnoreCase(conf.getString(string+".to")))
					return 1;
			}
		}
		
		if (p.getItemInHand().getAmount() == 64) {
			for (String string : keys) {
				if (itemName.equalsIgnoreCase(string))
					return 2;
			}
		}
		
		return 0;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] arg) {
		
		if (!cmd.getName().equalsIgnoreCase("convert")) return true;
		if (!(sender instanceof Player)) return true;
		if (!sender.hasPermission("gsm.convert")) return true;
		Player p = Bukkit.getPlayer(sender.getName());
		if (p.getItemInHand().equals(null)) return true;
		
		ConfigurationSection conf = ConfigsDaemon.mainConfig.getConfigurationSection("converter.items");
		Set<String> keys = conf.getKeys(false);
		
		if (!CooldownSystem.isExpired(ActionObject.Convert, p)) return true;
		if (isInInventory(p, conf, keys) == 0) {
			sender.sendMessage(ConfigsDaemon.mainConfig.get().getString("converter.message").replace("&", "\u00a7"));
			CooldownSystem.add(ActionObject.Convert, p, 3);
			return true;
		} else if (isInInventory(p, conf, keys) == 1) {
			convert1To64(p, conf, keys);
			CooldownSystem.add(ActionObject.Convert, p, 3);
			return true;
		} else {
			convert64To1(p, conf, keys);
			CooldownSystem.add(ActionObject.Convert, p, 3);
			return true;
		}
		
	}
}
