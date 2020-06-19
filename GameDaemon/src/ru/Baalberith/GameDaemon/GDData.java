package ru.Baalberith.GameDaemon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.inventory.ItemStack;
import org.json.simple.JSONObject;

import ru.Baalberith.GameDaemon.Utils.ItemDaemon;

public class GDData {

	public Storage data;
	
	public GDData() { }
	
	public GDData(String fileName, String folder) {
		this.data = new Storage(fileName.concat(".json"), folder, ConfigsDaemon.compressJson);
	}
	
	public void saveData() {
		data.save();
	}
	
	@Override
	public boolean equals(Object o) {
		return o instanceof String ? data.getName().equals(o.toString()) : false;
	}
	
	// LIGHT LEVELING SYSTEM (LiLS)
	
	public void setExpBoostBonus(double value) {
		data.set("lightLevelingSystem.booster.bonus", value);
	}
	
	public double getExpBoostBonus() {
		return data.getDouble("lightLevelingSystem.booster.bonus", 0D);
	}
	
	public void addExpReceivedWithBooster(long value) {
		data.increment("lightLevelingSystem.booster.receivedExp", value, 0);
	}
	
	public long getExpReceivedWithBooster() {
		return data.getLong("lightLevelingSystem.booster.receivedExp", 0);
	}
	
	// Если время действия закончилось, то вернёт отрицательное
	public int getExpBoostRemainingTime() {
		long expBoostExpires = data.getLong("lightLevelingSystem.booster.expires", 0);
		if (expBoostExpires == 0) return 0;
		return (int) (expBoostExpires - System.currentTimeMillis());
	}
	
	public void setExpBoostRemainingTime(long durationMillis) {
		data.set("lightLevelingSystem.booster.expires", durationMillis + System.currentTimeMillis());
	}
	
	public void setExpBoost(boolean value) {
		data.set("lightLevelingSystem.booster.enabled", value);
	}
	
	public boolean hasExpBoost() {
		return data.getBoolean("lightLevelingSystem.booster.enabled", false);
	}
	
	public void removeExpBoost() {
		data.set("lightLevelingSystem.booster.receivedExp", 0L);
		data.set("lightLevelingSystem.booster.enabled", false);
	}
	
	public int getFreeTraitPoints() {
		return data.getInt("lightLevelingSystem.freePoints", 0);
	}
	
	public void setFreeTraitPoints(int value) {
		data.set("lightLevelingSystem.freePoints", value);
	}
	
	public void addFreeTraitPoints(int value) {
		data.increment("lightLevelingSystem.freePoints", value, 0);
	}
	
	public void removeFreeTraitPoints(int value) {
		data.increment("lightLevelingSystem.freePoints", -value);
	}
	
	// GENERAL
	
	public void addJoinsAmount() {
		data.increment("general.joins", 1);
	}
	
	public long getJoinsAmount() {
		return data.getLong("general.joins", 0);
	}
	
	public void addJoinIp(String value) {
		data.addObjectToList("JQNotes.ipJoin", value, 0);
	}
	
	public String getJoinIp(int position) {
		List<String> list = data.getStringList("JQNotes.ipJoin");
		if (list == null || position >= list.size()) return "пусто";
		return list.get(position);
	}
	
	public List<String> getJoinIps() {
		return data.getStringList("JQNotes.ipJoin");
	}
	
	public void addJoinDate(long value) {
		data.addObjectToList("JQNotes.dateJoin", value, 0);
	}
	
	public long getJoinDate(int position) {
		List<Long> list = data.getLongList("JQNotes.dateJoin");
		if (list == null || position >= list.size()) return 0;
		return list.get(position);
	}
	
	public List<Long> getJoinDates() {
		return data.getLongList("JQNotes.dateJoin");
	}
	
	public void addQuitIp(String value) {
		data.addObjectToList("JQNotes.ipQuit", value, 0);
	}
	
	public String getQuitIp(int position) {
		List<String> list = data.getStringList("JQNotes.ipQuit");
		if (list == null || position >= list.size()) return "пусто";
		return list.get(position);
	}
	
	public List<String> getQuitIps() {
		return data.getStringList("JQNotes.ipQuit");
	}
	
	public void addQuitDate(long value) {
		data.addObjectToList("JQNotes.dateQuit", value, 0);
	}
	
	public long getQuitDate(int position) {
		List<Long> list = data.getLongList("JQNotes.dateQuit");
		if (list == null || position >= list.size()) return 0;
		return list.get(position);
	}
	
	public List<Long> getQuitDates() {
		return data.getLongList("JQNotes.dateQuit");
	}
	
	public void setTimePlayingSpent(long value) {
		data.set("general.timePlayingSpent", value);
	}
	
	public long getTimePlayingSpent() {
		return data.getLong("general.timePlayingSpent", 0);
	}
	
	public void setDateFirstJoin(long value) {
		data.set("general.dateFirstJoin", value);
	}
	
	public long getDateFirstJoin() {
		return data.getLong("general.dateFirstJoin", 0);
	}
	
	// DUNGEONS
	
	public void setDungeonCooldown(String dungeonId, long cooldown) {
		data.set("dungeons.cooldowns.".concat(dungeonId), System.currentTimeMillis() + cooldown*1000);
	}
	
	/**
	 * @return возвращает n < 0, если кулдаун ещё не закончился, иначе закончился.
	 */
	public long getDungeonRemCooldown(String dungeonId) {
		return System.currentTimeMillis() - data.getLong("dungeons.cooldowns.".concat(dungeonId), 0);
	}
	
	public void addDungeonsCleared(String name) {
		data.increment("dungeons.".concat(name), 1);
		data.increment("dungeons.all", 1);
	}
	
	public long getDungeonsCleared(String name) {
		if (name == null || name.equals("")) name = "all";
		return data.getLong("dungeons.".concat(name), 0);
	}
	
	// CNPCs FACTIONS
	
	public void addReceivedFactionPoints(long value) {
		data.increment("general.receivedfactionPoints", value);
	}
	
	// DROP BAGS
	
	public void addOpenedBags(String name, long value) {
		data.increment("general.openedBags.".concat(name), value);
		data.increment("general.openedBags.all", value);
	}
	
	public long getOpenedBags(String name) {
		if (name == null || name.equals("")) name = "all";
		return data.getLong("general.openedBags.".concat(name), 0);
	}
	
	// EXPERIENCE CONVERTER

	public void addBottledExp(long value) {
		data.increment("general.bottledExp", value);
	}
	
	public long getBottledExp() {
		return data.getLong("general.bottledExp", 0);
	}

	// MUTES
	
	public boolean isMuted() {
		return data.getBoolean("violations.blockingChat.active", false);
	}
	
	public boolean checkMute() {
		boolean state = isMuted();
		if (state) {
			long expire = data.getLong("violations.blockingChat.expires", 0);
			return System.currentTimeMillis() > expire ? false : true;
		} else return false;
	}
	
	public void setMute(boolean value) {
		data.set("violations.blockingChat.active", value);
	}
	
	// Возвращает false, если по такой причине мут уже выдан.
	public boolean addMute(String punisher, long time, String... reasons) {
		if (checkMute()) {
			int f = 0;
			for (int i = 0; i < reasons.length; i++) {
				if (hasMuteReason(reasons[i])) continue;
				addMuteAmount(1, reasons[i]);
				addMuteReason(reasons[i]);
				f++;
			}
			if (f == 0) return false;
			setMute(true);
		} else {
			setMute(true);
			for (int i = 0; i < reasons.length; i++) {
				addMuteAmount(1, reasons[i]);
				addMuteReason(reasons[i]);
			}
		}
		addMuteTime(time);
		saveMuteToHistory(punisher, time, reasons);
		return true;
	}
	
	public void removeMute() {
		setMute(false);
		setMuteExpires(0);
		removeMuteReasons();
	}
	
	public long getMuteTimeLeft() {
		long l = data.getLong("violations.blockingChat.expires", 0) - System.currentTimeMillis();
		return l < 0 ? 0 : l;
	}
	
	private void addMuteTime(long value) {
		long now = data.getLong("violations.blockingChat.expires", System.currentTimeMillis());
		now = now == 0 ? System.currentTimeMillis() : now;
		setMuteExpires(now+value);
	}
	
	private void setMuteExpires(long value) {
		data.set("violations.blockingChat.expires", value);
	}
	
	private void addMuteAmount(long value, String reason) {
		data.increment("violations.blockingChat.amount.".concat(reason), value);
		data.increment("violations.blockingChat.amount.all", value);
	}
	
	public boolean hasMuteReason(String reason) {
		List<String> list = data.getStringList("violations.blockingChat.reasons");
		if (list == null) return false;
		return list.contains(reason);
	}
	
	private void addMuteReason(String reason) {
		data.addObjectToList("violations.blockingChat.reasons", reason);
	}
	
	private void removeMuteReasons() {
		data.set("violations.blockingChat.reasons", new ArrayList<String>());
	}
	
	private void saveMuteToHistory(String punisher, long time, String... reasons) {
		HashMap<String, Object> hm = new HashMap<String, Object>();
		hm.put("unixDate", System.currentTimeMillis());
		hm.put("punisher", punisher);
		hm.put("reason", String.join("+", reasons));
		hm.put("timeMillis", time);
		data.addObjectToList("violations.blockingChat.history", hm, 0);
	}

	@SuppressWarnings("unchecked")
	public List<HashMap<String, Object>> getMuteHistory() {
		List<JSONObject> list = data.getJSONObjectsArray("violations.blockingChat.history");
		List<HashMap<String, Object>> newList = new ArrayList<HashMap<String, Object>>();
		if (list == null || list.isEmpty()) return newList;
		for (HashMap<String, Object> j : list) {
			newList.add(j);
		}
		return newList;
	}
	
	public long getMuteAmount(String reason) {
		if (reason == null || reason.equals("")) reason = "all";
		return data.getLong("violations.blockingChat.amount.".concat(reason), 0);
	}
	
	// JAIL

	public boolean isImprisoned() {
		return data.getBoolean("violations.jail.active", false);
	}
	
	public void setImprisoned(boolean value) {
		data.set("violations.jail.active", value);
	}
	
	public void addImprisonedAmount(long value) {
		data.increment("violations.jail.amount", value);
	}
	
	public long getImprisonedAmount() {
		return data.getLong("violations.jail.amount", 0);
	}
	
	// MCMMO BOOST

	public void addReceivedMcmmoExp(long value) {
		data.increment("general.receivedMcmmoExp", value);
	}
	
	public long getReceivedMcmmoExp() {
		return data.getLong("general.receivedMcmmoExp", 0);
	}
	
	// PVP RATING
	
	public void setPvpRating(long value) {
		data.set("pvp.rating", value);
	}
	
	public void addPvpRating(long value) {
		data.increment("pvp.rating", value);
	}
	
	public long getPvpRating() {
		return data.getLong("pvp.rating", 0);
	}
	
	public void addKillsAmount(long value) {
		data.increment("pvp.kills", value);
	}
	
	public long getKillsAmount() {
		return data.getLong("pvp.kills", 0);
	}
	
	public void addDeathsAmount(long value) {
		data.increment("pvp.deaths", value);
	}
	
	public long getDeathsAmount() {
		return data.getLong("pvp.deaths", 0);
	}
	
	public void addLoyaltyRatio(long value) {
		data.increment("pvp.loyaltyRatio", value);
	}
	
	public long getLoyaltyRatio() {
		return data.getLong("pvp.loyaltyRatio", 100);
	}
	
	public List<String> getBattleHistory() {
		return Storage.JSONObjectListToStringList(data.getJSONObjectsArray("pvp.battleHistory"));
	}
	
	public void addNoteToBattleHistory(String key, String value) {
		data.addKeyValueToArray("pvp.battleHistory", key, value, 0);
	}
	
	public void addLastKill(String value) {
		data.addObjectToList("pvp.lastKills", value, 0);
	}
	
	public List<String> getLastKills() {
		List<String> list = new ArrayList<String>();
		list.addAll(data.getStringList("pvp.lastKills"));
		if (list.size() > 5) for (int i = list.size()-1; i >= 5; i--) list.remove(i);
		return list;
	}
	
	public void addPvpReward(String value) {
		data.addObjectToList("pvp.rewards", value);
	}
	
	public String getPvpReward(int position) {
		List<String> list = data.getStringList("pvp.rewards");
		if (list == null || position >= list.size()) return null;
		return list.get(position);
	}
	
	public void setPvpRewards(List<String> list) {
		data.set("pvp.rewards", list);
	}
	
	public void removePvpReward(int position) {
		data.removeObjectFromList("pvp.rewards", position);
	}
	
	// RPG SPAWNS
	
	public void setRpgSpawn(String value) {
		data.set("general.rpgSpawn", value);
	}
	
	public String getRpgSpawn() {
		return data.getString("general.rpgSpawn", ConfigsDaemon.defaultRpgSpawn);
	}
	
	// SHARPENING
	
	public void addSharpSuccessAmount(long value) {
		data.increment("crafts.sharpening.successAmount", value);
	}
	
	public long getSharpSuccessAmount() {
		return data.getLong("crafts.sharpening.successAmount", 0);
	}
	
	public void addSharpFailureAmount(long value) {
		data.increment("crafts.sharpening.failureAmount", value);
	}
	
	public long getSharpFailureAmount() {
		return data.getLong("crafts.sharpening.failureAmount", 0);
	}
	
	public void saveSharpeningInv(ItemStack... contents) {
		List<String> list = new ArrayList<String>();
		for (ItemStack i : contents) {
			list.add(ItemDaemon.serializeItem(i, true));
		}
		data.set("crafts.sharpening.savedItems", list);
	}
	
	public ItemStack[] takeSharpeningInv() {
		ItemStack[] contents = new ItemStack[3];
		List<Object> list = data.getObjectList("crafts.sharpening.savedItems");
		if (list.isEmpty()) return contents;
		for (int i = 0; i < contents.length; i++) {
			contents[i] = ItemDaemon.deSerializeItem(list.get(i).toString());
		}
		data.set("crafts.sharpening.savedItems", null);
		return contents;
	}
	
	public boolean hasSharpeningInv() {
		List<Object> list = data.getObjectList("crafts.sharpening.savedItems");
		return list != null;
	}
	
	// CARGO ITEMS
	
	public void addCargoDeliveredAmount(String name, long value) {
		data.increment("delivery.".concat(name), value);
		data.increment("delivery.amount", value);
	}
	
	public long getCargoDeliveredAmount() {
		return data.getLong("delivery.amount", 0);
	}
	
	// NEWBIES
	
	public boolean hasPeaceMode() {
		return data.getBoolean("general.peaceMode", true);
	}
	
	public void setPeaceMode(boolean value) {
		data.set("general.peaceMode", value);
	}
	
	// OTHERS
	
	public void setStartIntroTime(long startTime) {
		data.set("general.startIntroTime", startTime);
	}
	
	public long getStartIntroTime() {
		return data.getLong("general.startIntroTime", 0);
	}
	
	public void setJoinSpawn(String src) {
		data.set("general.joinSpawn", src);
	}
	
	public String getJoinSpawn() {
		return data.getString("general.joinSpawn", null);
	}
	
}
