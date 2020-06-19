package ru.Baalberith.GameDaemon.PVPRating;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import ru.Baalberith.GameDaemon.ConfigsDaemon;
import ru.Baalberith.GameDaemon.GD;
import ru.Baalberith.GameDaemon.GDData;
import ru.Baalberith.GameDaemon.GDPlayer;
import ru.Baalberith.GameDaemon.Storage;
import ru.Baalberith.GameDaemon.Utils.ItemDaemon;
import ru.Baalberith.GameDaemon.Utils.ThreadDaemon;
import ru.Baalberith.GameDaemon.Utils.Utils;

public class TopEngine {
	
	private ConfigurationSection c;
	private ConfigurationSection m;
	private static Map<String, Long> top = new HashMap<String, Long>();
	private Task task = new Task();
	
	private long minTopRating;         // Минимальный рейтинг, чтобы попасть в топ
	private int prizePlaces;           // Количество призовых мест
	private static int visiblePlaces;  // Видимый то игроков для всех
	
	public static final String SYMBOL = "\u2736";
	
	public static Map<String, Long> getTopList() {
		return top;
	}
	
	public void reload() {
		c = ConfigsDaemon.mainConfig.getConfigurationSection("pvp");
		m = ConfigsDaemon.messagesConfig.getConfigurationSection("pvp");
		
		minTopRating = c.getLong("top.min-top-rating");
		prizePlaces = c.getInt("top.prize-places");
		visiblePlaces = c.getInt("top.visible-places");
		
		update();
		GD.log("[TRPGRating] Loaded "+top.size()+" players to pvp-rating top.");
		task.reload(prizePlaces);
	}
	
	// Изменить и обновить рейтинг игрока.
	public void changeRatingInTop(String player, long newRating) {
		if (newRating < minTopRating) return;
		top.put(player, newRating);
		top = Utils.sortByValue(top, true);
	}
	
	// Обновить топ игроков по всем существующим игрокам.
	public void update() {
		ThreadDaemon.async(() -> top = Utils.sortByValue(Storage.getLongMap(ConfigsDaemon.STATS_FOLDER, "pvp.rating", minTopRating-1), true));
	}
	
	// Выдать награду игроку, занявшему топ 3
	public void giveReward(CommandSender sender) {
		GDPlayer p = GD.getGDPlayer(sender);
		
		String o = p.getPvpReward(0);
		if (o == null) {
			sender.sendMessage(RatingEngine.funcPrefix+m.getString("rewards.error").replace("&", "\u00a7"));
			return;
		}
		ItemStack item = ItemDaemon.deSerializeItem(o);
		if (item == null) {
			sender.sendMessage(RatingEngine.funcPrefix+m.getString("rewards.error").replace("&", "\u00a7"));
			return;
		}
		p.removePvpReward(0);
		p.giveItem(item);
		p.sendMessage(RatingEngine.funcPrefix+m.getString("rewards.received").replace("&", "\u00a7"));
	}
	
	// Выдача наград в конце месяца для топ-3 игроков.
	public void writeRandomReward(String player) {
		try {
			int random = (int) Math.round(Math.random()*(Rewards.inst.rewards.size()-1));
			Reward reward = Rewards.inst.rewards.get(random);
			GDData d = GD.getGDData(player);
			d.addPvpReward(reward.getItemString());
			d.saveData();
		} catch (Exception e) { e.printStackTrace(); }
	}
	
	// Показать игроку 5 последних убийств.
	public void seeLastKills(CommandSender sender) {
		GDPlayer p = GD.getGDPlayer(sender);
		List<String> list = p.getLastKills();
		if (list != null) {
			p.sendMessage(m.getString("lastkills.title").replace("[funcPrefix]", RatingEngine.funcPrefix).replace("&", "\u00a7"));
			for (String object : list) {
				p.sendMessage(m.getString("lastkills.raw").replace("[player]", object.toString()).replace("&", "\u00a7"));
			}
		} else {
			p.sendMessage(RatingEngine.funcPrefix+m.getString("lastkills.empty").replace("&", "\u00a7"));
		}
	}
	
	// Отпарвить в чат информацию о топ-10 игроков.
	public void sendTop(CommandSender sender) {
		Set<String> keys = top.keySet();
		if (keys.isEmpty()) {
			sender.sendMessage(RatingEngine.funcPrefix+m.getString("top.empty").replace("&", "\u00a7"));
			return;
		}
		sender.sendMessage(m.getString("top.title").replace("[funcPrefix]", RatingEngine.funcPrefix).replace("&", "\u00a7"));
		
		int i = 1;
		boolean inTop = false;
		String sName = sender.getName();
		for (String k : keys) {
			String msg = null;
			if (i <= prizePlaces) {
				if (k.equalsIgnoreCase(sName)) {
					msg = m.getString("top.prize-raw").replace("[lighter]", m.getString("top.lighter"));
					inTop = true;
				} else
					msg = m.getString("top.prize-raw").replace("[lighter]", "");
			} else {
				if (k.equalsIgnoreCase(sName)) {
					msg = m.getString("top.raw").replace("[lighter]", m.getString("top.lighter"));
					inTop = true;
				} else
					msg = m.getString("top.raw").replace("[lighter]", "");
			}
			msg = String.format(msg, i, top.get(k));
			sender.sendMessage(msg.replace("[player]", k).replace("[symbol]", SYMBOL).replace("&", "\u00a7"));
			i++;
			if (i > visiblePlaces) break;
		}
		
		List<Object> tops = Arrays.asList(top.keySet().toArray());
		
		if (sender instanceof ConsoleCommandSender) return;
		
		if (inTop) return;
		
		sender.sendMessage((top.containsKey(sName) ? 
				String.format(m.getString("top.self-raw"), (tops.indexOf(sName)+1), top.get(sName)).replace("[symbol]", SYMBOL)
				: m.getString("top.little-rating").replace("[symbol]", SYMBOL).replace("[minTopRating]", ""+minTopRating)).replace("&", "\u00a7"));
	}
	
	// Обнулить рейтинг всех игроков.
	public void zeroize() {
		GD.online.forEach(p -> p.setPvpRating(0));
		String[] fs = GD.getPlayerFiles();
		for (String s : fs) {
			Storage st = new Storage(s, ConfigsDaemon.STATS_FOLDER, ConfigsDaemon.compressJson);
			st.set("pvp.rating", 0);
			st.save();
		}
		update();
		GD.log("[TRPGRating] All player has been nulled.");
	}
	
	public void getAllRewards(GDPlayer p) {
		Rewards rew = RatingEngine.inst.getRewards();
		p.giveItems(rew.getItems());
	}
	
}
