package ru.Baalberith.GameDaemon.Commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import ru.Baalberith.GameDaemon.ConfigsDaemon;
import ru.Baalberith.GameDaemon.GD;
import ru.Baalberith.GameDaemon.GDPlayer;
import ru.Baalberith.GameDaemon.Utils.ItemDaemon;
import ru.Baalberith.GameDaemon.Utils.MathOperation;

public class Introduction implements CommandExecutor {
	
	private List<ItemStack> itemsForRemove;
	private static String message;
	private static String tpChanged;
	private static String cmd;
	private static String record;
	private static String msgStartIntro;
	private static Long recordIntroTime;
	
	private ConfigurationSection c;
	
	public Introduction() {
		itemsForRemove = new ArrayList<ItemStack>();
		GD.inst.getCommand("intro").setExecutor(this);
		
	}
	
	public void reload() {
		try {
			c = ConfigsDaemon.mainConfig.getConfigurationSection("intro");
			
			message = c.getString("end-intro-message").replace("&", "\u00a7");
			tpChanged = c.getString("tp-changed").replace("&", "\u00a7");
			cmd = c.getString("cmd");
			record = c.getString("record").replace("&", "\u00a7");
			msgStartIntro = c.getString("startIntro", "&aВы приступили к обучению.").replace("&", "\u00a7");
			
			itemsForRemove.clear();
			List<String> keys = c.getStringList("items-for-remove");
			for (String k : keys) {
				ItemStack ri = ItemDaemon.fromString(k);
				if (ri == null) continue;
				itemsForRemove.add(ri);
			}
			
			recordIntroTime = GD.settings.getLong("intro.recordTime", -1);
			if (recordIntroTime == -1) {
				GD.settings.set("intro.recordTime", -1);
				recordIntroTime = (long) -1;
			}

		} catch (Exception e) {e.printStackTrace();}
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		if (!sender.getName().equalsIgnoreCase("CONSOLE")) return false;
		if (args.length != 2) return false;
		
		if (args[0].equalsIgnoreCase("clear")) {
			GDPlayer p = GD.getGDPlayer(args[1]);
			if (p == null) return true;
			p.clearItems(itemsForRemove);
			
		}
		
		if (args[0].equalsIgnoreCase("start")) {
			GDPlayer p = GD.getGDPlayer(args[1]);
			if (p == null) return true;
			long startTime = p.getPlayTime();
			p.setStartIntroTime(startTime);
			p.sendMessage(msgStartIntro);
			GD.dispatchCommand("pex user "+args[1]+" group add newbies");
			GD.dispatchCommand("pex user "+args[1]+" group remove start");
		}
		
		if (args[0].equalsIgnoreCase("end")) {
			GDPlayer p = GD.getGDPlayer(args[1]);
			if (p == null) return true;
			long introTime = p.getPlayTime() - p.getStartIntroTime();
			String format = MathOperation.makeTimeToString(message, introTime);
			if (introTime < recordIntroTime) {
				GD.broadcast(format.replace("[player]", args[1]).replace("{record}", record));
				GD.settings.set("intro.recordTime", introTime);
			} else GD.broadcast(format.replace("[player]", args[1]).replace("{record}", ""));
			
			GD.dispatchCommand(Introduction.cmd.replace("[player]", args[1]));
			GD.dispatchCommand("pex user "+args[1]+" group add default");
			p.sendMessage(tpChanged);
		}
		
		
		
		return true;
	}
	
	
	
}
