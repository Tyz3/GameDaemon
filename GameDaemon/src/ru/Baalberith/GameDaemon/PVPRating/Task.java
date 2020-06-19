package ru.Baalberith.GameDaemon.PVPRating;

import java.util.Calendar;
import java.util.Map;
import java.util.Set;

import org.bukkit.scheduler.BukkitTask;

import ru.Baalberith.GameDaemon.GD;
import ru.Baalberith.GameDaemon.Utils.ThreadDaemon;

public class Task {

	private BukkitTask task;
	
	public Task() {
	}
	
	public void reload(int prizePlaces) {
		ThreadDaemon.cancelTask(task);
		task = ThreadDaemon.syncLater(() -> start(prizePlaces), 100);
	}
	
	private void start(int prizePlaces) {
//		if (!Storage.fileExists(ConfigsDaemon.SETTINGS_FOLDER, ConfigsDaemon.SETTINGS_FOLDER+".json")) {
//			GD.settings.set("pvp.currentSeason", 0);
//		}
		int serverSeasonMonth = GD.settings.getInt("pvp.currentSeason", 0);
		if (serverSeasonMonth == 0) {
			GD.log("[TRPGRating] Start of a season is not set.");
			return;
		}
		
		Calendar calendar = Calendar.getInstance();
		int currentMonth = calendar.get(Calendar.MONTH)+1;
		if (!(currentMonth == serverSeasonMonth+1 || currentMonth == serverSeasonMonth-11)) {
//			long daysLeft = (calendar.getTime().getTime()-System.currentTimeMillis())/3600000/24;
			GD.log("[TRPGRating] Current PVP season is not over yet.");
			return;
		}
		
		Map<String, Long> map = TopEngine.getTopList();
		Set<String> keys = map.keySet();
		
		int i = 0;
		
		String path = "pvp.season_"+(currentMonth-1)+"_"+calendar.get(Calendar.YEAR);
		GD.log("[TRPGRating] PVP season ended.");
		if (keys.isEmpty()) {
			GD.log("[TRPGRating] Has not participants.");
			GD.settings.addObjectToList(path, "Has not participants");
		} else {
			for (String k : keys) {
				GD.settings.addKeyValueToArray(path, k, map.get(k).toString());
				for (int j = 0; j < RatingEngine.prizesCount; j++) Rewards.engine.writeRandomReward(k);
				GD.log("[TRPGRating] Player "+k+" got prize for "+(i+1)+" place in pvp of rating.");
				i++;
				if (i >= prizePlaces) break;
			}
		}
		
		GD.log("[TRPGRating] Starting new PVP season.");
		Rewards.engine.zeroize();
		
		GD.settings.set("pvp.currentSeason", currentMonth);
		GD.settings.save();
	}
}
