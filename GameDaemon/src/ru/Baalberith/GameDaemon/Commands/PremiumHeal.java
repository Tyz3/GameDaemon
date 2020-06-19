package ru.Baalberith.GameDaemon.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Player;

import ru.Baalberith.GameDaemon.ConfigsDaemon;
import ru.Baalberith.GameDaemon.GD;
import ru.Baalberith.GameDaemon.Extra.ActionObject;
import ru.Baalberith.GameDaemon.Extra.CooldownSystem;

public class PremiumHeal implements CommandExecutor {

	public PremiumHeal() {
		GD.inst.getCommand("heal").setExecutor(this);
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		if (!cmd.getName().equalsIgnoreCase("heal")) return true;
		if (sender.hasPermission("gsm.premiumheal.50")) {
			if (!CooldownSystem.isExpired(ActionObject.PremiumHeal, sender)) return true;
			Damageable p = (Damageable) sender;
			double mh = p.getMaxHealth();
			double h = p.getHealth();
			((Player) sender).setHealth(h + mh/2 < mh ? h + mh/2 : mh);
			return true;
		}
		
		if (sender.hasPermission("gsm.premiumheal.25")) {
			if (!CooldownSystem.isExpired(ActionObject.PremiumHeal, sender)) return true;
			Damageable p = (Damageable) sender;
			double mh = p.getMaxHealth();
			double h = p.getHealth();
			((Player) sender).setHealth(h + mh/4 < mh ? h + mh/4 : mh);
			CooldownSystem.add(ActionObject.PremiumHeal, sender, 1800);
			return true;
		}
		
		sender.sendMessage(ConfigsDaemon.noPermission);
		return true;
	}

}
