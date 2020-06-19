package ru.Baalberith.GameDaemon.AutoStopServer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.scheduler.BukkitTask;

import ru.Baalberith.GameDaemon.ConfigsDaemon;
import ru.Baalberith.GameDaemon.GD;
import ru.Baalberith.GameDaemon.Utils.CountingPattern;
import ru.Baalberith.GameDaemon.Utils.MathOperation;
import ru.Baalberith.GameDaemon.Utils.ThreadDaemon;

public class AutoStop {
	
	private BukkitTask task;
	private ConfigurationSection c;
	private ConfigurationSection m;
	
	private boolean isStarted;
	private List<String> stopIn;
	private CountingPattern countingPattern;
	private SimpleDateFormat sdf;
	private int delay;
	private static String MSG_BROADCAST;
	
	public AutoStop() {
		stopIn = new ArrayList<String>();
	}
	
	public void reload() {
		try {
			c = ConfigsDaemon.mainConfig.getConfigurationSection("auto-stop-server");
			m = ConfigsDaemon.messagesConfig.getConfigurationSection("auto-stop-server");
			
			stopIn.clear();
			
			for (String s : c.getString("start-announce").split(",")) {
				stopIn.add(s);
			}
			countingPattern = new CountingPattern(c.getString("announce"));
			delay = c.getInt("delay");
			MSG_BROADCAST = m.getString("announce").replace("&", "\u00a7");
			sdf = new SimpleDateFormat("HH:mm");
			
			if (task != null) Bukkit.getScheduler().cancelTask(task.getTaskId());
			run();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void run() {
		task = ThreadDaemon.asyncTimer(() -> stopping(), 0, 20*60);
	}
	
	private void stopping() {
		if (isStarted) return;
		Date date1 = new Date();
		long time = date1.getTime(); // + date1.getTimezoneOffset()*60*1000;
		
		String format = sdf.format(time);
		GD.log("[AutoStop] Checking time for restart. Now "+format+".");
		if (stopIn.contains(format)) {
			isStarted = true;
			try {
				int timer = delay;
				for (int i = timer; i != 0; i--) {
					if (countingPattern.contains(i)) {
						Date date = new Date(i*1000);
						Bukkit.broadcastMessage(MathOperation.makeTimeToString(MSG_BROADCAST, date.getTime()));
					}
					Thread.sleep(1000);
				}
				ThreadDaemon.sync(() -> {
					GD.kickAll();
					Bukkit.getServer().shutdown();
				});
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
}
