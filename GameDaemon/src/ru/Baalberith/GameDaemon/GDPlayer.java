package ru.Baalberith.GameDaemon;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.Statistic;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import ru.Baalberith.GameDaemon.Clans.Groups.Party.Party;
import ru.Baalberith.GameDaemon.Extra.TeamDaemon;
import ru.Baalberith.GameDaemon.Extra.TeamDaemon.SidebarType;
import ru.Baalberith.GameDaemon.LightLevelingSystem.Trait.TraitType;
import ru.Baalberith.GameDaemon.Utils.ThreadDaemon;

public class GDPlayer extends GDSender {
	public Player p;
	private TeamDaemon team;
	
	
	public Party party;
	private boolean partyChat = false;
	private SidebarType sidebarType = SidebarType.party;
	
	public GDPlayer(Player player, String folder) {
		super(player.getName(), folder);
		this.p = player;
		this.team = TeamDaemon.getOrCreate(player.getName());
		this.team.getScoreboard().send(p);
		this.sender = (CommandSender) p;
		this.consoleSender = false;
		
		// Уровневая система
		initLiLS();
	}
	
	public GDPlayer(String name, String folder) {
		super(name, folder);
		this.p = null;
		this.sender = null;
		this.consoleSender = false;
		
		// Уровневая система
		initLiLS();
	}
	
	@Override
	public String toString() {
		return "[ "+getName()+ ", Level: "+getLevel()+" ]";
	}
	
	@Override
	public boolean equals(Object o) {
		return (o instanceof GDPlayer) ? (GDPlayer) o == this : false;
	}
	
	public boolean equals(String name) {
		return p.getName().equalsIgnoreCase(name);
	}
	
	// TEAM DAEMON
	
	public void setSidebarType(SidebarType type) {
		sidebarType = type;
	}
	
	public boolean sidebarEquals(SidebarType type) {
		return type == sidebarType;
	}
	
	// DUNGEON
	
	public boolean inDungeon() {
		return sidebarType != null && sidebarType == SidebarType.dungeon;
	}
	
	// PARTY
	
	public TeamDaemon getTeamDaemon() {
		return team;
	}
	
	public boolean hasParty() {
		return party != null;
	}
	
	public Party getParty() {
		return party;
	}
	
	public void setParty(Party party) {
		this.party = party;
	}
	
	public boolean hasPartySidebar() {
		return sidebarType != null && sidebarType == SidebarType.party;
	}
	
	public boolean partyChat() {
		return partyChat;
	}
	
	public void setPartyChat(boolean mode) {
		partyChat = mode;
	}
	
	
	// LIGHT LEVELING SYSTEM
	
	private HashMap<TraitType, Integer> traitLevel = new HashMap<TraitType, Integer>();
	
	private void initLiLS() {
		for (TraitType type : TraitType.values()) {
			traitLevel.put(type, data.getInt("lightLevelingSystem.traits."+type.name(), 0));
		}
	}
	
	public void addTraitLevel(TraitType type, int level) {
		data.increment("lightLevelingSystem.traits."+type.name(), level, 0);
		int current = data.getInt("lightLevelingSystem.traits."+type.name(), 0);
		traitLevel.put(type, current);
	}
	
	public int getTraitLevel(TraitType type) {
		return traitLevel.get(type);
	}
	
	public int getPowerLevel() {
		int powerLevel = 0;
		for (Entry<TraitType, Integer> e : traitLevel.entrySet()) {
			powerLevel += e.getValue();
		}
		return powerLevel;
	}
	
	// Common
	
	public void createUUID() {
		data.set("general.uuid", p.getUniqueId().toString());
	}
	
	public long getPlayTime() {
		return p.getStatistic(Statistic.PLAY_ONE_TICK)*50;
	}
	
	public void addPotionEffect(PotionEffectType pet, int duration, int amplifier, boolean ambient) throws NullPointerException {
		p.addPotionEffect(new PotionEffect(pet, duration, amplifier), ambient);
	}
	
	public void addPotionEffects(Collection<PotionEffect> effects) throws NullPointerException {
		p.addPotionEffects(effects);
	}
	
	public void addPotionEffect(PotionEffect... effects) {
		if (effects.length == 0) return;
		for (PotionEffect e : effects) {
			addPotionEffect(e.getType(), e.getDuration(), e.getAmplifier(), e.isAmbient());
		}
	}
	
	/**
	 * @param item предмет на проверку.
	 * @return Возвращает номер слота найденного предмета в инвентаре, иначе вернёт -1.
	*/
	public int hasItem(ItemStack item) {
		return hasItem(item, 1, false, false, false);
	}
	
	/**
	 * @param item предмет на проверку.
	 * @param amount количество проверяемого предмета.
	 * @return Возвращает номер слота найденного предмета в инвентаре, иначе вернёт -1.
	*/
	public int hasItem(ItemStack item, int amount) {
		return hasItem(item, amount, false, false, false);
	}
	
	/**
	 * @param item предмет на проверку.
	 * @param displayName сравнивать ли отображаемые названия предметов.
	 * @param lore сравнивать ли лоры предметов.
	 * @param enchants сравнивать ли зачарования предметов.
	 * @return Возвращает номер слота найденного предмета в инвентаре, иначе вернёт -1.
	*/
	public int hasItem(ItemStack item, boolean displayName, boolean lore, boolean enchants) {
		return hasItem(item, 1, displayName, lore, enchants);
	}
	
	/**
	 * @param item предмет на проверку.
	 * @param amount количество проверяемого предмета.
	 * @param displayName сравнивать ли отображаемые названия предметов.
	 * @param lore сравнивать ли лоры предметов.
	 * @param enchants сравнивать ли зачарования предметов.
	 * @return Возвращает номер слота найденного предмета в инвентаре, иначе вернёт -1.
	*/
	public int hasItem(ItemStack item, int amount, boolean displayName, boolean lore, boolean enchants) throws NullPointerException {
		ItemStack[] contents = p.getInventory().getContents();
		if (contents == null) return -1;
		if (item == null) return -1;
		int result = -1;
		
		for (int i = 0; i < contents.length; i++) {
			if (contents[i] == null) continue;
			if (contents[i].getType() != item.getType()) continue;
			if (contents[i].getDurability() != item.getDurability()) continue;
			if (contents[i].getAmount() < amount) continue;
			if (displayName && !isValidDisplayName(contents[i], item)) continue;
			if (lore && !isValidLore(contents[i], item)) continue;
			if (enchants && !isValidEnchants(contents[i], item)) continue;
			result = i;
			break;
		}
		
		return result;
	}
	
	private boolean isValidDisplayName(ItemStack i1, ItemStack i2) {
		if (!i1.hasItemMeta() && !i2.hasItemMeta()) return true;
		String name1 = i1.getItemMeta().getDisplayName();
		String name2 = i2.getItemMeta().getDisplayName();
		if (name1 == null && name2 == null) return true;
		if (name1 != null && name2 != null) return name1.equals(name2);
		return false;
	}
	
	private boolean isValidLore(ItemStack i1, ItemStack i2) {
		if (!i1.hasItemMeta() && !i2.hasItemMeta()) return true;
		List<String> lore1 = i1.getItemMeta().getLore();
		List<String> lore2 = i2.getItemMeta().getLore();
		if (lore1 == null && lore2 == null) return true;
		if (lore1 != null && lore2 != null) {
			Collections.sort(lore1);
			Collections.sort(lore2);
			return lore1.equals(lore2);
		}
		return false;
	}
	
	private boolean isValidEnchants(ItemStack i1, ItemStack i2) {
		if (!i1.hasItemMeta() && !i2.hasItemMeta()) return true;
		Map<Enchantment, Integer> enchants1 = i1.getItemMeta().getEnchants();
		Map<Enchantment, Integer> enchants2 = i2.getItemMeta().getEnchants();
		if (enchants1 == null && enchants2 == null) return true;
		if (enchants1 != null && enchants2 != null) {
			Set<Entry<Enchantment, Integer>> set1 = enchants1.entrySet();
			Set<Entry<Enchantment, Integer>> set2 = enchants2.entrySet();
			if (set1.size() != set2.size()) return false;
			return set1.containsAll(set2);
		}
		return false;
	}
	
	/**
	 * @param items предметы на изъятие.
	*/
	public void clearItems(List<ItemStack> items) {
		PlayerInventory inv = p.getInventory();
		for (ItemStack i : items) {
			int index = hasItem(i);
			if (index != -1) inv.remove(index);
		}
	}
	
	/**
	 * @param item предмет на изъятие.
	 * @return Возвращает номер состояние действия: true если выполнение успешно, иначе - false.
	*/
	public boolean takeItem(ItemStack item) {
		return takeItem(item, 1, false, false, false);
	}
	
	/**
	 * @param item предмет на изъятие.
	 * @param amount количество забираемого предмета.
	 * @return Возвращает номер состояние действия: true если выполнение успешно, иначе - false.
	*/
	public boolean takeItem(ItemStack item, int amount) {
		return takeItem(item, amount, false, false, false);
	}
	
	/**
	 * @param item предмет на изъятие.
	 * @param displayName сравнивать ли отображаемые названия предметов.
	 * @param lore сравнивать ли лоры предметов.
	 * @param enchants сравнивать ли зачарования предметов.
	 * @return Возвращает номер состояние действия: true если выполнение успешно, иначе - false.
	*/
	public boolean takeItem(ItemStack item, boolean displayName, boolean lore, boolean enchants) {
		return takeItem(item, 1, displayName, lore, enchants);
	}
	
	/**
	 * @param item предмет на изъятие.
	 * @param amount количество забираемого предмета.
	 * @param displayName сравнивать ли отображаемые названия предметов.
	 * @param lore сравнивать ли лоры предметов.
	 * @param enchants сравнивать ли зачарования предметов.
	 * @return Возвращает номер состояние действия: true если выполнение успешно, иначе - false.
	*/
	public boolean takeItem(ItemStack item, int amount, boolean displayName, boolean lore, boolean enchants) {
		if (item == null) return true;
		
		int slot = hasItem(item, amount, displayName, lore, enchants);
		if (slot == -1) return false;
		ItemStack[] contents = p.getInventory().getContents();
		ItemStack i = contents[slot];
		if (i.getAmount() > amount) {
			i.setAmount(i.getAmount() - amount);
		} else {
			p.getInventory().setItem(slot, ConfigsDaemon.EMPTY_ITEM);
		}
		p.updateInventory();
		return true;
	}
	
	public Location getLocation() {
		return p.getLocation();
	}
	
	public String getName() {
		return p.getName();
	}
	
	public void kick(String msg) {
		p.kickPlayer(msg);
	}
	
	public boolean hasSpace(int cells) {
		ItemStack[] contents = p.getInventory().getContents();
		int airSlots = 0;
		for (ItemStack i : contents) {
			if (i == null || i.getType() == Material.AIR) airSlots++;
		}
		return airSlots >= cells ? true : false;
	}
	
	public void giveItem(ItemStack item, int amount) {
		item.setAmount(1);
		for (int i = 0; i < amount; i++) { giveItem(item); }
	}
	
	public void giveItem(ItemStack item) {
		if (item == null) return;
		if (item.getAmount() != 1) {
			giveItem(item, item.getAmount());
		}
		if (hasSpace(1)) p.getInventory().addItem(item);
		else ThreadDaemon.sync(() -> getWorld().dropItemNaturally(getLocation(), item));
	}
	
	public void giveItems(List<ItemStack> items) {
		items.forEach(i -> giveItem(i));
	}
	
	public boolean hasPermission(String perm) {
		return p.hasPermission(perm);
	}
	
	public void teleport(Location loc) {
		loc.getChunk().load();
		p.teleport(loc);
	}
	
	public void teleportSync(Location loc) {
		ThreadDaemon.sync(() -> {
			loc.getChunk().load();
			p.teleport(loc);
		});
	}
	
	public boolean isOp() {
		return p.isOp();
	}
	
	public World getWorld() {
		return p.getWorld();
	}
	
	public boolean isOnline() {
		return p == null ? false : p.isOnline();
	}
	
	public Player getBukkitPlayer() {
		return p;
	}
	
	/**
	 * @param sound - воспроизводимый звук.
	 * @param volume - Громкость звука 0.0 - 1.0.
	 * @param pitch - Скорость воспроизведения: 1.0 - нормально, 2.0 - быстро.
	 */
	public void playSound(Sound sound, float volume, float pitch) {
		p.playSound(getLocation(), sound, volume, pitch);
	}
	
	public void giveExperience(int value) {
		p.giveExp(value);
	}
	
	public boolean takeExperience(int value) {
		if (p.getTotalExperience() < value) return false;
		recursionTakeExperience(value);
		return true;
	}
	
	private void recursionTakeExperience(int value) {
		int expAtLevel = Math.round(p.getExpToLevel() * p.getExp());
		if (expAtLevel <= value) {
			p.giveExp(-expAtLevel);
			p.setLevel(p.getLevel() - 1);
			p.setExp(1.0F);
			takeExperience(value - expAtLevel);
		} else p.giveExp(-value);
	}
	
	public int getLevel() {
		return p.getLevel();
	}
	
	public boolean takeLevel(int value) {
		int lvl = getLevel()-value;
		if (lvl < 0) return false;
		p.setLevel(lvl);
		return true;
	}
	
	public boolean hasLevel(int lvl) {
		return getLevel() >= lvl ? true : false;
	}
	
	public int getTotalExperience() {
		return p.getTotalExperience();
	}
	
	public double getHealth() {
		return ((Damageable) p).getHealth();
	}
}
