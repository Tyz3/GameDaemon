package ru.Baalberith.GameDaemon;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import ru.Baalberith.GameDaemon.Extra.BukkitConfig;

public class ConfigsDaemon {
	
	public static String noPermission;
	public static String fileNotExists;
	public static String notEnoughArgs;
	public static String label;
	public static String hasCooldown;
	public static String warmUpSession;
	public static String warmUpCancelled;
	public static String jsonWaitingRequest;
	public static String requestCancelled;
	public static String defaultRpgSpawn;
	public static String bukkitPlayerNotExists;
	
	public static boolean compressJson = false;
	public static boolean stuffSynchronize = false;
	
	public static final String STATS_FOLDER = "statistics";
	public static final String SETTINGS_FOLDER = "settings";
	
	public static final ItemStack EMPTY_ITEM = new ItemStack(Material.AIR);

	public static BukkitConfig mainConfig = BukkitConfig.create(GD.inst, GD.inst.getDataFolder(), "config.yml");
	public static BukkitConfig messagesConfig = BukkitConfig.create(GD.inst, GD.inst.getDataFolder(), "messages.yml");
	
	public static BukkitConfig menuConfig = BukkitConfig.create(GD.inst, GD.inst.getDataFolder(), "menu.yml");
	public static BukkitConfig jailConfig = BukkitConfig.create(GD.inst, GD.inst.getDataFolder(), "imprisoned.yml");
	public static BukkitConfig cargoItemsConfig = BukkitConfig.create(GD.inst, GD.inst.getDataFolder(), "cargo_items.yml");
	public static BukkitConfig rpgSpawnsConfig = BukkitConfig.create(GD.inst, GD.inst.getDataFolder(), "rpg_spawn.yml");
	public static BukkitConfig pvpRewardsConfig = BukkitConfig.create(GD.inst, GD.inst.getDataFolder(), "pvp_rewards.yml");
	public static BukkitConfig anchorsConfig = BukkitConfig.create(GD.inst, GD.inst.getDataFolder(), "world_anchors.yml");
	public static BukkitConfig dropBagsConfig = BukkitConfig.create(GD.inst, GD.inst.getDataFolder(), "drop_bags.yml");
	public static BukkitConfig warpsConfig = BukkitConfig.create(GD.inst, GD.inst.getDataFolder(), "warps.yml");
	public static BukkitConfig synchronizerConfig = BukkitConfig.create(GD.inst, GD.inst.getDataFolder(), "sync_stuff.yml");
	
	public static BukkitConfig dungeonsConfig = BukkitConfig.create(GD.inst, GD.inst.getDataFolder(), "dungeons.yml");
	public static BukkitConfig worldQuestsConfig = BukkitConfig.create(GD.inst, GD.inst.getDataFolder(), "world_quests.yml");
	
	public void reload() {
		try {
			mainConfig.reload();
			messagesConfig.reload();
			menuConfig.reload();
			jailConfig.reload();
			cargoItemsConfig.reload();
			rpgSpawnsConfig.reload();
			pvpRewardsConfig.reload();
			anchorsConfig.reload();
			dropBagsConfig.reload();
			warpsConfig.reload();
			synchronizerConfig.reload();
			dungeonsConfig.reload();
			worldQuestsConfig.reload();
			
			noPermission = messagesConfig.get().getString("other.no-permission").replace("&", "\u00a7");
			fileNotExists = messagesConfig.get().getString("other.logFileDoesNotExists").replace("&", "\u00a7");
			notEnoughArgs = messagesConfig.get().getString("other.not-enough-args").replace("&", "\u00a7");
			label = messagesConfig.get().getString("other.label").replace("&", "\u00a7");
			hasCooldown = messagesConfig.get().getString("other.has-cooldown").replace("&", "\u00a7");
			warmUpSession = messagesConfig.get().getString("other.warmUpSession").replace("&", "\u00a7");
			warmUpCancelled = messagesConfig.get().getString("other.warmUpCancelled").replace("&", "\u00a7");
			jsonWaitingRequest = messagesConfig.get().getString("other.jsonWaitingRequest").replace("&", "\u00a7");
			requestCancelled = messagesConfig.get().getString("other.requestCancelled").replace("&", "\u00a7");
			bukkitPlayerNotExists = messagesConfig.get().getString("other.bukkitPlayerNotExists").replace("&", "\u00a7");
			
			defaultRpgSpawn = mainConfig.get().getString("rpg-spawn.first-point").replace("&", "\u00a7");
			stuffSynchronize = mainConfig.get().getBoolean("options.stuffSynchronize", false);
		
		} catch (Exception e) { e.printStackTrace(); }
	}
}
