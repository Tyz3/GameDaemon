package ru.Baalberith.GameDaemon.WorldAnchor;

import org.bukkit.configuration.ConfigurationSection;

import ru.Baalberith.GameDaemon.ConfigsDaemon;
import ru.Baalberith.GameDaemon.GD;
import ru.Baalberith.GameDaemon.Utils.CountingPattern;
import ru.Baalberith.GameDaemon.Utils.Utils;

public class DolmenEngine {
	static int dolmenRadius;      // Радиус действия якоря.
	static int checkTime;         // Через сколько секунд проверять запланированный запуск якоря.
	static int minPlayersToStart; // Минимальное кол-во игроков до старта якоря.
	static int waitingWarmup;     // Время ожидания якоря минимального кол-ва игроков.
	static int countdownToSummon; // Обратный отсчёт до призыва.
	
	private ConfigurationSection c;
	private ConfigurationSection m;

	CountingPattern waitingPattern;
	CountingPattern warmupPattern;
	CountingPattern durationPattern;
	private Tasks tasks;
	private Dolmens dolmens;
	
	public static DolmenEngine inst;
	
	public DolmenEngine() {
		inst = this;
		dolmens = new Dolmens();
		tasks = new Tasks();
	}
	
	public void reload() {
		try {
			c = ConfigsDaemon.mainConfig.getConfigurationSection("worldAnchors");
			m = ConfigsDaemon.messagesConfig.getConfigurationSection("worldAnchors");
			
			dolmenRadius = c.getInt("dolmenRadius", 24);
			checkTime = c.getInt("checkTime", 60);
			minPlayersToStart = c.getInt("minPlayersToStart", 1);
			waitingWarmup = c.getInt("waitingWarmup", 900);
			countdownToSummon = c.getInt("countdownToSummon", 30);
			
			Message.load(m);
			
			String waitPattern = c.getString("waitingPattern", "600,300,60");
			waitingPattern = new CountingPattern(waitPattern);
			
			String wPattern = c.getString("warmupPattern", "30,20,10,5-1");
			warmupPattern = new CountingPattern(wPattern);
			
			String dPattern = c.getString("durationPattern", "30,20,10,5-1");
			durationPattern = new CountingPattern(dPattern);
			
			dolmens.reload();
			tasks.reload(dolmens);
		} catch (Exception e) {e.printStackTrace();}
	}

	static void broadcast(Dolmen dolmen, int warmup) {
		GD.sendMessageForAll(Message.broadcast.replace("{title}", dolmen.getTitle())
			.replace("{X}", String.valueOf(dolmen.getX())) 
			.replace("{Y}", String.valueOf(dolmen.getY()))
			.replace("{Z}", String.valueOf(dolmen.getZ()))
			.replace("{rules}", Utils.rawArray(Message.rules.gets()))
			.replace("{M}", String.valueOf(warmup/60)).get()
		);
	}
}
