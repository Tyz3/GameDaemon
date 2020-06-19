package ru.Baalberith.GameDaemon.Commands;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;

import ru.Baalberith.GameDaemon.ConfigsDaemon;
import ru.Baalberith.GameDaemon.GD;
import ru.Baalberith.GameDaemon.GDPlayer;
import ru.Baalberith.GameDaemon.Storage;
import ru.Baalberith.GameDaemon.Extra.ActionObject;
import ru.Baalberith.GameDaemon.Extra.CooldownSystem;
import ru.Baalberith.GameDaemon.Utils.MathOperation;
import ru.Baalberith.GameDaemon.Utils.ThreadDaemon;

public class CommandAI implements CommandExecutor {
	private ConfigurationSection m;

	private List<String> msgsFoundAccountInfo = new ArrayList<String>();
	private List<String> msgsAccountInfo = new ArrayList<String>();
	
	public CommandAI() {
		GD.inst.getCommand("account-info").setExecutor(this);
	}
	
	public void reload() {
		try {
			m = ConfigsDaemon.messagesConfig.get();
			
			msgsAccountInfo.clear();
			msgsFoundAccountInfo.clear();
			
			for (String string : m.getStringList("command.account-info")) {
				msgsAccountInfo.add(string.replace("&", "§"));
			}
			
			for (String string : msgsAccountInfo) {
				msgsFoundAccountInfo.add(string.replace("&", "§"));
			}
		} catch (Exception e) {e.printStackTrace();}
	}
	
	SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		ThreadDaemon.async(() -> {
			
			if (!CooldownSystem.isExpired(ActionObject.AccountInfo, sender)) return;
			if (args.length == 0) {
				
				GDPlayer p = GD.getGDPlayer(sender);
				String time = MathOperation.makeTimeToString("{H} ч. {M} мин.", p.getPlayTime());
				String[] strs = {"{player}", sender.getName(), "{dateFirstJoin}", ""+sdf.format(p.getDateFirstJoin()),
						"{dateJoin1}", sdf.format(p.getJoinDate(0)), "{dateJoin2}", sdf.format(p.getJoinDate(1)),
						"{dateJoin3}", sdf.format(p.getJoinDate(2)), "{dateQuit1}", sdf.format(p.getQuitDate(0)),
						"{dateQuit2}", sdf.format(p.getQuitDate(1)), "{dateQuit3}", sdf.format(p.getQuitDate(2)),
						"{ipJoin1}", p.getJoinIp(0), "{ipJoin2}", p.getJoinIp(1),
						"{ipJoin3}", p.getJoinIp(2), "{ipQuit1}", p.getQuitIp(0), "{ipQuit2}", p.getQuitIp(1),
						"{ipQuit3}", p.getQuitIp(2), "{joinsAmount}", ""+p.getJoinsAmount(), "{muteAmount}", ""+p.getMuteAmount(""),
						"{imprisonedAmount}", ""+p.getImprisonedAmount(), "{imprisoned}", p.isImprisoned()?"да":"нет",
						"{time}", time, "{mute}", p.checkMute()?"да":"нет", "&", "§"};
				
				p.sendMessages(m.getStringList("command.account-info"), strs);
				CooldownSystem.add(ActionObject.AccountInfo, sender, 3);
				return;
			}
			
			if (args.length == 1) {
				
				if (!sender.hasPermission("gsm.account-info.other")) {
					sender.sendMessage(ConfigsDaemon.noPermission);
					return;
				}
				
				if (!Storage.fileExists(ConfigsDaemon.STATS_FOLDER, args[0])) {
					sender.sendMessage(ConfigsDaemon.fileNotExists);
					return;
				}

				GDPlayer tar = GD.getGDPlayer(args[0]);
				
				String status = tar.isOnline() ? "online":"offline";
				String time = MathOperation.makeTimeToString("{H} ч. {M} мин.", tar.getTimePlayingSpent());
				
				GDPlayer p = GD.getGDPlayer(sender);
				String[] strs = {"{player}", args[0], "{dateFirstJoin}", ""+sdf.format(tar.getDateFirstJoin()),
						"{dateJoin1}", sdf.format(tar.getJoinDate(0)), "{dateJoin2}", sdf.format(tar.getJoinDate(1)),
						"{dateJoin3}", sdf.format(tar.getJoinDate(2)), "{dateQuit1}", sdf.format(tar.getQuitDate(0)),
						"{dateQuit2}", sdf.format(tar.getQuitDate(1)), "{dateQuit3}", sdf.format(tar.getQuitDate(2)),
						"{status}", status, "{ipJoin1}", tar.getJoinIp(0), "{ipJoin2}", tar.getJoinIp(1),
						"{ipJoin3}", tar.getJoinIp(2), "{ipQuit1}", tar.getQuitIp(0), "{ipQuit2}", tar.getQuitIp(1),
						"{ipQuit3}", tar.getQuitIp(2), "{joinsAmount}", ""+tar.getJoinsAmount(), "{muteAmount}", ""+tar.getMuteAmount(""),
						"{imprisonedAmount}", ""+tar.getImprisonedAmount(), "{imprisoned}", tar.isImprisoned()?"да":"нет",
						"{time}", time, "{mute}", tar.isMuted()?"да":"нет", "&", "§"};
				
				p.sendMessages(m.getStringList("command.found-account-info"), strs);
				CooldownSystem.add(ActionObject.AccountInfo, sender, 3);
				return;
			}
		});
		
		return true;
	}
}
