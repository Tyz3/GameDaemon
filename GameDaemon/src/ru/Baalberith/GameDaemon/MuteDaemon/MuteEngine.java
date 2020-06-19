package ru.Baalberith.GameDaemon.MuteDaemon;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.scheduler.BukkitTask;

import ru.Baalberith.GameDaemon.ConfigsDaemon;
import ru.Baalberith.GameDaemon.GD;
import ru.Baalberith.GameDaemon.GDPlayer;
import ru.Baalberith.GameDaemon.GDSender;
import ru.Baalberith.GameDaemon.MuteDaemon.Commands.MuteCMD;
import ru.Baalberith.GameDaemon.MuteDaemon.Commands.UnmuteCMD;
import ru.Baalberith.GameDaemon.Utils.MathOperation;
import ru.Baalberith.GameDaemon.Utils.ThreadDaemon;

public class MuteEngine {
	
	public static MuteEngine inst;
	
	static String timeFormat;
	private static String comboFormat;
	private static HashMap<String, Integer> timers = new HashMap<String, Integer>();
	
	public static String[] allowedReasons;
	static List<String> blockedCmds = new ArrayList<String>();
	private HashMap<String, String> descReasons = new HashMap<String, String>();
	
	private BukkitTask task;
	
	public MuteEngine() {
		inst = this;
		new MuteCMD();
		new UnmuteCMD();
		new MuteHandler();
	}
	
	public void reload() {
		try {
			blockedCmds.clear();
			ConfigurationSection c = ConfigsDaemon.mainConfig.getConfigurationSection("violations.mutes");
			ConfigurationSection m = ConfigsDaemon.messagesConfig.getConfigurationSection("violations.mutes");
			Message.load(m, m.getString("label"));
			
			descReasons.clear();
			Set<String> reas = m.getConfigurationSection("description").getKeys(false);
			reas.forEach(k -> descReasons.put(k, m.getString("description."+k).replace("&", "§")));
			
			timers.clear();
			timeFormat = c.getString("timeFormat");
			comboFormat = c.getString("comboFormat").replace("&", "§");
			
			Set<String> keys = c.getConfigurationSection("timers").getKeys(false);
			List<String> tempList = new ArrayList<String>();
			for (String k : keys) {
				timers.put(k, (Integer) c.getInt("timers."+k)*1000);
				tempList.add(k);
			}
			timers = timers.entrySet().stream()
					.sorted(Map.Entry.<String, Integer>comparingByValue(Comparator.naturalOrder()))
					.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
			
			allowedReasons = new String[tempList.size()];
			for (int i = 0; i < allowedReasons.length; i++) {
				allowedReasons[i] = tempList.get(i);
			}
			blockedCmds.addAll(c.getStringList("blockedCmds"));
			
			if (task == null) ThreadDaemon.cancelTask(task);
			ThreadDaemon.asyncTimer(() -> muteChecker(), 20, 20*10);
		} catch (Exception e) {e.printStackTrace();}
	}
	
	private void muteChecker() {
		for (GDPlayer p : GD.online) {
			if (p.isMuted()) {
				if (p.checkMute()) continue;
				p.removeMute();
				Message.muteCleared.send(p);
				MuteHandler.removePlayerFromMute(p.getName());
			}
		}
	}
	
	public void mutePlayer(GDPlayer p, String target, String... args) {
		if (!p.hasPermission("gsm.mutes.helper")) {
			p.sendMessage(ConfigsDaemon.noPermission);
			return;
		}
		
		GDPlayer tp = GD.getGDPlayer(target);
		if (tp == null) {
			p.sendMessage(ConfigsDaemon.bukkitPlayerNotExists.replace("{player}", target));
			return;
		} else if (!tp.isOnline()) {
			
		} else if (tp.isOp() || tp.hasPermission("gsm.mutes.immune")) {
			p.sendMessage(ConfigsDaemon.bukkitPlayerNotExists.replace("{player}", target));
			return;
		}
		
		long time = 0;
		for (int i = 0; i < args.length; i++) {
			if (!timers.containsKey(args[i])) {
				Message.incorrectArgs.send(p);
				return;
			}
			if (!tp.hasMuteReason(args[i]))
				time += timers.get(args[i])*(tp.getMuteAmount(args[i]) + 1);
		}
		
		String[] reasons = convertArgsToReasons(tp, args);
		if (tp.addMute(p.getName(), time, args)) {
			MuteHandler.addPlayerToMute(target);
			GD.broadcast(Message.broadcast.gets(), new String[]{"{violator}", target,
					"{whoPunished}", p.getName(),
					"{reason}", String.join(" §8+§r ", reasons),
					"{timeout}", MathOperation.makeTimeToString(timeFormat, time)});
		} else Message.alreadyMuted.send(p);
		if (!tp.isOnline()) {
			tp.saveData();
			return;
		}
	}
	
	private String[] convertArgsToReasons(GDPlayer tp, String[] args) {
		String[] reasons = new String[args.length];
		for (int i = 0; i < args.length; i++) {
			if (!tp.hasMuteReason(args[i]))
				reasons[i] = descReasons.get(args[i]) + " " + comboFormat
					.replace("{combo}", String.valueOf(1+tp.getMuteAmount(args[i])));
			else reasons[i] = "Повтор";
			
		}
		return reasons;
	}
	
	public void unmutePlayer(GDPlayer p, String target, String description) {
		if (!p.hasPermission("gsm.mutes.moder")) {
			p.sendMessage(ConfigsDaemon.noPermission);
			return;
		}
		
		GDPlayer tp = GD.getGDPlayer(target);
		if (tp == null) {
			p.sendMessage(ConfigsDaemon.bukkitPlayerNotExists.replace("{player}", target));
			return;
		} else if (!tp.isOnline()) {
			
		} else if (tp.isOp() || tp.hasPermission("gsm.mutes.immune")) {
			p.sendMessage(ConfigsDaemon.bukkitPlayerNotExists.replace("{player}", target));
			return;
		}
		
		tp.removeMute();
		MuteHandler.removePlayerFromMute(target);
		Message.unmute.replace("{player}", target).send(p);
		if (!tp.isOnline()) {
			tp.saveData();
			return;
		}
		Message.muteCleared.send(tp);
	}
	
	SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss/dd_MM_yyyy");
	public void muteHistory(GDPlayer p, String target) {
		if (!p.hasPermission("gsm.mutes.helper")) {
			p.sendMessage(ConfigsDaemon.noPermission);
			return;
		}
		
		GDPlayer tp = GD.getGDPlayer(target);
		if (tp == null) {
			p.sendMessage(ConfigsDaemon.bukkitPlayerNotExists.replace("{player}", target));
			return;
		}
		
		List<HashMap<String, Object>> history = tp.getMuteHistory();
		if (history.isEmpty()) {
			Message.info_error.send(p);
			return;
		}
		Message.info_title.replace("{player}", target).send(p);
		for (HashMap<String, Object> log : history) {
			Message.info_history.replace("{when}", sdf.format(log.get("unixDate")))
				.replace("{who}", log.get("punisher").toString())
				.replace("{reason}", log.get("reason").toString())
				.replace("{penalty}", MathOperation.makeTimeToString(timeFormat, (long) log.get("timeMillis")))
				.send(p);
		}
	}
	
	public void help(GDSender p) {
		if (!p.hasPermission("gsm.mutes.helper")) {
			p.sendMessage(ConfigsDaemon.noPermission);
			return;
		}
		
		Message.help_title.send(p);
		Message.help_helper.send(p);
		if (p.hasPermission("gsm.mutes.moder")) Message.help_moder.send(p);
	}
	
	public void helpFull(GDPlayer p) {
		if (!p.hasPermission("gsm.mutes.helper")) {
			p.sendMessage(ConfigsDaemon.noPermission);
			return;
		}

		Message.help_title.send(p);
		Message.help_helper.send(p);
		if (p.hasPermission("gsm.mutes.moder")) Message.help_moder.send(p);
		for (Entry<String, Integer> e : timers.entrySet()) {
			String reason = descReasons.get(e.getKey());
			Message.help_reason.replace("{description}", reason == null?"No description":reason)
				.replace("{symbol}", e.getKey()).replace("{time}", MathOperation.makeTimeToString(timeFormat, e.getValue()))
				.send(p);
		}
	}
	
	public void checkMute(GDPlayer p, String target) {
		
		GDPlayer tp = GD.getGDPlayer(target);
		if (tp == null) {
			p.sendMessage(ConfigsDaemon.bukkitPlayerNotExists.replace("{player}", target));
			return;
		}
		
		long left = tp.getMuteTimeLeft();
		if (left == 0) {
			Message.missingMute.send(p);
			tp.removeMute();
			return;
		}
		
		Message.checkMute.replace("{time}", MathOperation.makeTimeToString(timeFormat, left)).send(p);
		if (!tp.isOnline()) {
			tp.saveData();
			return;
		}
	}
	
	
}
