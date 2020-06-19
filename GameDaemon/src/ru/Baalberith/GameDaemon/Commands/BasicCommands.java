package ru.Baalberith.GameDaemon.Commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import ru.Baalberith.GameDaemon.ConfigsDaemon;
import ru.Baalberith.GameDaemon.GD;
import ru.Baalberith.GameDaemon.GDSender;
import ru.Baalberith.GameDaemon.Extra.ActionObject;
import ru.Baalberith.GameDaemon.Extra.CooldownSystem;
import ru.Baalberith.GameDaemon.Extra.WarmUpSystem;
import ru.Baalberith.GameDaemon.Utils.CountingPattern;
import ru.Baalberith.GameDaemon.Utils.MathOperation;
import ru.Baalberith.GameDaemon.Utils.ThreadDaemon;

public class BasicCommands implements CommandExecutor {
	
	
	public BasicCommands() {
		GD.inst.getCommand("gamedaemon").setExecutor(this);
		GD.inst.getCommand("roll").setExecutor(this);
		GD.inst.getCommand("food").setExecutor(this);
		GD.inst.getCommand("test").setExecutor(this);
//		GD.inst.getCommand("").setExecutor(this);
//		GD.inst.getCommand("").setExecutor(this);
	}
	
	public void reload() {
		
	}
	
	CountingPattern counting = new CountingPattern("600,300,120,60,10,5-1");
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		if (cmd.getName().equalsIgnoreCase("gamedaemon")) {
			
			if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
				GD.inst.reload();
				sender.sendMessage("§aPlugin has been reload.");
			}
			
			if (args.length >= 1 && args[0].equalsIgnoreCase("reboot")) {
				ThreadDaemon.async(() -> {
					int time = 600;
					if (args.length == 2) try{time = Integer.parseInt(args[1]);} catch(Exception e) {}
					for (; time != 0; time--) {
						
						if (counting.contains(time))
							GD.broadcast(MathOperation.makeTimeToString("§4[Broadcast] §2Рестарт сервера через {M} мин. {S} сек.", time*1000));
						
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					ThreadDaemon.sync(() -> {
						GD.kickAll();
						Bukkit.getServer().shutdown();
					});
				});
			}
			return true;
		}

		if (cmd.getName().equalsIgnoreCase("roll")) {
			

			GDSender s = GD.getGDSender(sender);
			WarmUpSystem.startWarmUp(ActionObject.Roll, s, 5, () -> {
				ConfigurationSection mess = ConfigsDaemon.messagesConfig.get();
				String msg = mess.getString("command.roll").replace("&", "\u00a7");
				int random = (int) (1 + Math.random()*100);
				if (args.length > 0)
					s.sendMessageToNearest(String.format(msg, sender.getName(), random), 16);
				else GD.broadcast(String.format(msg, sender.getName(), random));
			}, true);
			return true;
		}
		
		if (cmd.getName().equalsIgnoreCase("food")) {
			if (!sender.hasPermission("gsm.food")) {
				sender.sendMessage(ConfigsDaemon.noPermission);
				return true;
			}
			
			if (CooldownSystem.isExpired(ActionObject.Food, sender)) {
				GD.getGDPlayer(sender).addPotionEffect(PotionEffectType.SATURATION, 20*120, 0, true);
				CooldownSystem.add(ActionObject.Food, sender, 3*60);
			}
			return true;
		}
		
		if (cmd.getName().equalsIgnoreCase("test")) {
			if (!sender.hasPermission("gsm.test")) {
				sender.sendMessage(ConfigsDaemon.noPermission);
				return true;
			}
			
			Player p = (Player) sender;
			
			ThreadDaemon.async(() -> {
				for (org.bukkit.Sound sound : org.bukkit.Sound.values()) {
					p.playSound(p.getLocation(), sound, 5, 5);
					p.sendMessage("> "+sound.name());
					try {
						Thread.sleep(1200);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
				}
			});
//			HolographicDisplaysAPI.createIndividualHologram(GD.inst, p.getLocation(), p, "{player}");
//			HolographicDisplaysAPI.createIndividualFloatingItem(GD.inst, p.getEyeLocation(), p, new ItemStack(Material.BEDROCK));
		}
		
		
		return true;
	}

}
