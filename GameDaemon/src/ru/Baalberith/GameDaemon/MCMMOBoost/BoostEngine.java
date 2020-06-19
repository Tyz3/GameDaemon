package ru.Baalberith.GameDaemon.MCMMOBoost;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import com.gmail.nossr50.api.ExperienceAPI;

import ru.Baalberith.GameDaemon.ConfigsDaemon;
import ru.Baalberith.GameDaemon.GD;
import ru.Baalberith.GameDaemon.GDPlayer;
import ru.Baalberith.GameDaemon.MCMMOBoost.Commands.MmoboostCMD;
import ru.Baalberith.GameDaemon.Utils.CompleteHelper;
import ru.Baalberith.GameDaemon.Utils.ItemDaemon;

public class BoostEngine implements TabCompleter {
	
	private static final String[] SKILLS = {"mining", "woodcutting", "herbalism", "fishing", "excavation", "archery", "swords", "axes", "taming", "acrobatics"};
	private ConfigurationSection m;
	private ConfigurationSection c;
	private MmoboostCMD mmoBoostCmd;
	private Map<ItemStack, Integer> boostItems;
	private static final int LVL_BY_XP = 20;
	
	public static BoostEngine inst;
	
	public BoostEngine() {
		inst = this;
		boostItems = new HashMap<ItemStack, Integer>();
		mmoBoostCmd = new MmoboostCMD(SKILLS);
		GD.inst.getCommand("mmoboost").setExecutor(mmoBoostCmd);
		GD.inst.getCommand("mmoboost").setTabCompleter(this);
	}
	
	public void reload() {
		try {
			boostItems.clear();
			m = ConfigsDaemon.messagesConfig.getConfigurationSection("mcmmo-boost");
			c = ConfigsDaemon.mainConfig.getConfigurationSection("mcmmo-boost");
			
			 
			List<String> keys = c.getStringList("items");
			for (String k : keys) {
				String[] args = k.split(";");
				if (args.length != 2) continue;
				ItemStack ri = ItemDaemon.fromString(args[0]);
				if (ri == null) continue;
				int boostXp = Integer.parseInt(args[1]);
				boostItems.put(ri, boostXp);
			}
			GD.log("[McmmoBoost] Loaded "+boostItems.size()+" mcmmo boost items.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void process(CommandSender sender, String skillName) {
		GDPlayer p = GD.getGDPlayer(sender);
		for (Entry<ItemStack, Integer> e : boostItems.entrySet()) {
			int cap = ExperienceAPI.getLevelCap(skillName);
			try {
				int nowXp = ExperienceAPI.getXP(p.getBukkitPlayer(), skillName);
				int boostXp = e.getValue();
				if ((nowXp+boostXp)/LVL_BY_XP <= cap && p.takeItem(e.getKey())) {
					ExperienceAPI.addRawXP(p.getBukkitPlayer(), skillName, boostXp);
					p.addReceivedMcmmoExp(boostXp);
					p.sendMessage(String.format(m.getString("success"), e.getValue(), skillName), "&", "\u00a7");
					return;
				}
			} catch (Exception ex) {}
		}
		p.sendMessage(m.getString("error").replace("&", "\u00a7"));
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length == 1)
			return CompleteHelper.filter(args, SKILLS);
		return null;
	}
	
}
