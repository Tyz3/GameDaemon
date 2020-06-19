package ru.Baalberith.GameDaemon.PVPRating;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;

import ru.Baalberith.GameDaemon.ConfigsDaemon;
import ru.Baalberith.GameDaemon.GD;
import ru.Baalberith.GameDaemon.GDPlayer;
import ru.Baalberith.GameDaemon.PVPRating.Commands.PvpCMD;

public class RatingEngine {
	
	public static RatingEngine inst;
	private ConfigurationSection c;
	public ConfigurationSection m;
	
	private short loyaltyRange;
	private short stableBonus;
	private short onlineBonus;
	
	private RatingHandler ratingHandler;
	private Rewards rewards;
	private PvpCMD pvpCmd;
	
	public static String funcPrefix;
	public static int prizesCount;
	
	public RatingEngine() {
		inst = this;
		ratingHandler = new RatingHandler();
		rewards = new Rewards();
		pvpCmd = new PvpCMD();
		
		Bukkit.getPluginManager().registerEvents(ratingHandler, GD.inst);
		GD.inst.getCommand("pvp").setExecutor(pvpCmd);
	}
	
	public Rewards getRewards() {
		return this.rewards;
	}
	
	public void reload() {
		try {
			c = ConfigsDaemon.mainConfig.getConfigurationSection("pvp");
			m = ConfigsDaemon.messagesConfig.getConfigurationSection("pvp");
			
			loyaltyRange = (short) c.getInt("loyalty");
			stableBonus = (short) c.getInt("stable-bonus");
			onlineBonus = (short) c.getInt("online-bonus");
			funcPrefix = ConfigsDaemon.label.replace("{label}", m.getString("label"));
			prizesCount = c.getInt("prizes-count");
	
			ratingHandler.reload();
			rewards.reload();
			pvpCmd.reload();
		} catch (Exception e) {e.printStackTrace();}
	}

	public void process(GDPlayer killer, GDPlayer victim) {
		
		String k = killer.getName();
		String v = victim.getName();

		victim.addNoteToBattleHistory(k, v);
		killer.addNoteToBattleHistory(k, v);
		
		List<String> lastKills = killer.getLastKills();
		if (lastKills == null) lastKills = new ArrayList<String>();
		
		// Уменьшаем на 20% параметр сходства последних 10 убийств, схожих с текущим.
		short SIMILAR_LAST_KILLS = 5;
		for (String lk : lastKills) {
			SIMILAR_LAST_KILLS -= (v.equalsIgnoreCase(lk)) ? 1 : 0;
		}
		SIMILAR_LAST_KILLS = SIMILAR_LAST_KILLS < 0 ? 0 : SIMILAR_LAST_KILLS;
		
		// Добавляем в историю убийств игрока.
		killer.addLastKill(v);
		
		// Модифицируем параметр лояльности рейтинга к игроку, который уменьшается,
		// если жертва была в списке последних убийств киллера.
		long KILLER_LOYALTY = killer.getLoyaltyRatio(); 
		if (SIMILAR_LAST_KILLS != 5) {
			killer.addLoyaltyRatio((KILLER_LOYALTY - loyaltyRange) < 0 ? 0 : -loyaltyRange);
		} else if (KILLER_LOYALTY >= 0 && KILLER_LOYALTY <= 100-loyaltyRange) {
			killer.addLoyaltyRatio(loyaltyRange);
		} else if (KILLER_LOYALTY < 0) {
			killer.addLoyaltyRatio(Math.abs(KILLER_LOYALTY));
		}
		
		long VICTIM_LOYALTY = killer.getLoyaltyRatio();
		
		// Получаем текущий рейтинг игроков.
		long KILLER_RATING = killer.getPvpRating();
		long VICTIM_RATING = victim.getPvpRating();
		
		// Текущий онлайн
		int ONLINE = GD.online.size();
		
		long RANDOM = Math.round(1+Math.random()*19);
		
		// Расчёт рейтинга
		int GENERAL_VALUE = Math.round((1 + VICTIM_RATING)/(1 + KILLER_RATING) + (1 + onlineBonus/100)*ONLINE + (stableBonus/RANDOM));
		
		long KR = (long) Math.floor(0.50 * GENERAL_VALUE * SIMILAR_LAST_KILLS/5 * KILLER_LOYALTY/100);
		long VR = (long) Math.floor(0.25 * GENERAL_VALUE * (2 - VICTIM_LOYALTY/100));
		
		GD.log("[PVPRating] "+k+" got "+KR+" pvp points <AND> "+v+" lost "+VR+" pvp points.");
		
		KILLER_RATING += Math.abs(KR);
		if (VICTIM_RATING-VR < 0) {
			VR = VICTIM_RATING;
			VICTIM_RATING = 0;
		} else {
			VICTIM_RATING -= Math.abs(VR);
		}
		
		killer.sendMessage(funcPrefix+m.getString("rating-increase").replace("[change]", ""+KR).replace("[current]", ""+KILLER_RATING).replace("[symbol]", TopEngine.SYMBOL).replace("&", "\u00a7"));
		victim.sendMessage(funcPrefix+m.getString("rating-decrease").replace("[change]", ""+VR).replace("[current]", ""+VICTIM_RATING).replace("[symbol]", TopEngine.SYMBOL).replace("&", "\u00a7"));
		
		killer.setPvpRating(KILLER_RATING);
		victim.setPvpRating(VICTIM_RATING);
		
		Rewards.engine.changeRatingInTop(k, KILLER_RATING);
		Rewards.engine.changeRatingInTop(v, VICTIM_RATING);
	}
}
