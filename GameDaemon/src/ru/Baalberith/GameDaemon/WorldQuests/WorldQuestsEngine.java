package ru.Baalberith.GameDaemon.WorldQuests;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import ru.Baalberith.GameDaemon.ConfigsDaemon;
import ru.Baalberith.GameDaemon.GD;
import ru.Baalberith.GameDaemon.GDPlayer;
import ru.Baalberith.GameDaemon.Extra.Installation.TellRawText;
import ru.Baalberith.GameDaemon.Extra.Installation.TellRawText.ClickEvent;
import ru.Baalberith.GameDaemon.Extra.Installation.TellRawText.Color;
import ru.Baalberith.GameDaemon.Extra.Installation.TellRawText.Element;
import ru.Baalberith.GameDaemon.Utils.LocationManager;
import ru.Baalberith.GameDaemon.WorldQuests.WorldQuest.WorldQuestType;
import ru.Baalberith.GameDaemon.WorldQuests.Commands.WorldQuestCMD;
import ru.Baalberith.GameDaemon.WorldQuests.Quests.ArtisanQuest;
import ru.Baalberith.GameDaemon.WorldQuests.Quests.MercenaryQuest;
import ru.Baalberith.GameDaemon.WorldQuests.Quests.MinerQuest;
import ru.Baalberith.GameDaemon.WorldQuests.Quests.SupplierQuest;

public class WorldQuestsEngine {
	
	public static List<WorldQuest> allQuests = new ArrayList<WorldQuest>();
	public static List<String> scheduler = new ArrayList<String>();
	
	public static WorldQuestsManager questsManager = new WorldQuestsManager();
	public static WorldQuestCMD worldQuestCMD = new WorldQuestCMD();
	
	public static int updateTime;
	public static boolean enabled;
	
	private static ConfigurationSection confSect;
	private static ConfigurationSection questSect;
	
	private static HashMap<GDPlayer, WorldQuest> installers = new HashMap<GDPlayer, WorldQuest>();
	
	public static void reload() {
		try {
			ConfigsDaemon.worldQuestsConfig.reload();
			ConfigurationSection m = ConfigsDaemon.messagesConfig.getConfigurationSection("worldQuests");
			Message.load(m, m.getString("label"));
			
			scheduler.clear();
			allQuests.clear();
			installers.clear();

			confSect = ConfigsDaemon.worldQuestsConfig.getConfigurationSection("configuration");
			questSect = ConfigsDaemon.worldQuestsConfig.getConfigurationSection("quests");
			
			updateTime = confSect.getInt("updateTime", 60);
			enabled = confSect.getBoolean("enabled", true);
			scheduler = confSect.getStringList("queue");
			
			Set<String> keys = questSect.getKeys(false);
			for (String k : keys) {
				
				WorldQuestType type = WorldQuestType.valueOf(k.toLowerCase());
				if (type == null) continue;
				
				switch (type) {
				case supplier:
					loadSupplierQuests(questSect.getConfigurationSection("supplier"));
					break;
				case miner:
					loadMinerQuests(questSect.getConfigurationSection("miner"));
					break;
				case artisan:
					loadArtisanQuests(questSect.getConfigurationSection("artisan"));
					break;
				case mercenary:
					loadMercenaryQuests(questSect.getConfigurationSection("mercenary"));
					break;
				default: continue;
				}
				
			}
			
			GD.log("[WorldQuests] Loaded "+allQuests.size()+" quests into memory.");
			
			questsManager.reload();
		} catch (Exception e) {e.printStackTrace();}
	}
	
	public static void saveAllToDisk() {
		if (WorldQuestsManager.activeQuestTask == null) return;
		WorldQuestsManager.saveTaskIntoFile(WorldQuestsManager.activeQuestTask);
	}
	
	public static void removeEntryFromScheduler() {
		scheduler.remove(0);
		confSect.set("scheduler", scheduler);
		ConfigsDaemon.worldQuestsConfig.save();
	}
	
	public static WorldQuest getWorldQuest(WorldQuestType type, String id) {
		for (WorldQuest q : allQuests) {
			if (q.type == type && q.id.equals(id)) {
				return q;
			}
		}
		return null;
	}
	
	private static void loadSupplierQuests(ConfigurationSection c) {
		Set<String> keys = c.getKeys(false); // 1, 2, 3 ...
		for (String k : keys) {
			SupplierQuest quest = new SupplierQuest();
			quest.reload(c.getConfigurationSection(k));
			allQuests.add(quest);
		}
	}
	
	private static void loadMinerQuests(ConfigurationSection c) {
		Set<String> keys = c.getKeys(false); // 1, 2, 3 ...
		for (String k : keys) {
			MinerQuest quest = new MinerQuest();
			quest.reload(c.getConfigurationSection(k));
			allQuests.add(quest);
		}
	}
	
	private static void loadArtisanQuests(ConfigurationSection c) {
		Set<String> keys = c.getKeys(false); // 1, 2, 3 ...
		for (String k : keys) {
			ArtisanQuest quest = new ArtisanQuest();
			quest.reload(c.getConfigurationSection(k));
			allQuests.add(quest);
		}
	}
	
	private static void loadMercenaryQuests(ConfigurationSection c) {
		Set<String> keys = c.getKeys(false); // 1, 2, 3 ...
		for (String k : keys) {
			MercenaryQuest quest = new MercenaryQuest();
			quest.reload(c.getConfigurationSection(k));
			allQuests.add(quest);
		}
	}
	
	// Меню редактирования квестов
	
	public static void showAllQuests(GDPlayer p) {
		TellRawText quests = new TellRawText();
		Element[] elements = new Element[2*allQuests.size()];
		for (int i = 0, j = 0; j < elements.length; i++, j+=2) {
			WorldQuest wq = allQuests.get(i);
			Element e1 = quests.createElement().setText(wq.getName().concat("_").concat(wq.getId())).setColor(Color.green).setClickEvent(ClickEvent.run_command, "/worldquests install ".concat(wq.getType().name()).concat(" ").concat(wq.getId()));
			Element e2 = quests.createElement().setText(", ");
			elements[j] = e1;
			elements[j+1] = e2;
		}
		quests.addSeparator1();
		quests.addLine(quests.createLine(elements));
		Element add = quests.createElement().setText("  ✚  ").setColor(Color.dark_purple).setClickEvent(ClickEvent.suggest_command, "/worldquests new <name> <id>").setTip("Создать новый квест\\nИспользуюй Tab!");
		quests.addSeparator2();
		quests.addLine(quests.createLine(add));
		quests.addSeparator2();
		quests.send(p);
	}
	
	public static void showMainPage(GDPlayer p) {
		if (!installers.containsKey(p)) return;
		WorldQuest wq = installers.get(p);
		if (wq == null) return;
		
		WorldQuestCMD.getInstallPage(0).send(p, "{type}", wq.getType().name(), "{id}", wq.getId(), "{name}", wq.getName());
		WorldQuestCMD.getInstallPage(1).send(p, "{duration}", String.valueOf(wq.getDuration()), "{location}", LocationManager.serializeLocation(wq.hologramLocation));
	
		switch (wq.type) {
		case artisan:
			
			break;
		case mercenary:
			
			break;
		case miner:
			
			break;
		case supplier:
			// TODO
			break;
		default: break;
		}
	}
	
	public static void install(GDPlayer p, String type, String id) {
		WorldQuest wq = getWorldQuest(WorldQuestType.valueOf(type), id);
		if (wq == null) {
			p.sendMessage("This id doesn't exists.");
			return;
		}
		
		installers.put(p, wq);
		
		showMainPage(p);
	}
	
	public static void uninstall(GDPlayer p) {
		installers.remove(p);
	}
	
	// Вызывается когда нужно поставить portal, spawn, returnSpawn или обавить wayback.
	public static void showCoordsChooseMenu(GDPlayer p, String type) {
//		DungeonCMD.getInstallPage(15).send(p, "{type}", type);
	}
	
	// --->>>
	public static HashMap<GDPlayer, String> waitingBlockClick = new HashMap<GDPlayer, String>();
	
	public static void coordsClick(GDPlayer p, String type) {
		waitingBlockClick.put(p, type);
		p.sendMessage("Waiting for click...");
	}
	
	public static void coordsClick_(GDPlayer p, Location clickLoc, String type) {
		
		if (!installers.containsKey(p)) {
			waitingBlockClick.remove(p);
			return;
		}
		WorldQuest quest = installers.get(p);
		
		switch (type) {
		case "hologram":
			quest.hologramLocation = clickLoc;
			quest.rebuildHologram();
			showMainPage(p);
			break;
		default:
			p.sendMessage("Что ты ввёл вообще???");
			break;
		}
		
		waitingBlockClick.remove(p);
//		Dungeons.saveDungeonToFile(dungeon);
//		Dungeons.reloadExact(dungeon.getId());
	}
	
	// Rewards 
	
	public static void showRewards(GDPlayer p) {
		if (!installers.containsKey(p)) return;
		WorldQuest wq = installers.get(p);
		if (wq == null) return;
		
		for (int i = 0; i < wq.rewards.size(); i++) {
			ItemStack reward = wq.rewards.get(i);
			TellRawText rewards = new TellRawText();
			Element r1 = rewards.createElement().setText("  Предмет: ");
			Element r2 = rewards.createElement().setText("&d*Наведи*  ").setTip(reward, true);
			Element r3 = rewards.createElement().setText("  Количество: &a{amount}  ").setClickEvent(ClickEvent.suggest_command, "/worldquests rewards {id} amount {amount}").setTip("Установить кол-во выдаваемого предмета");
			Element r4 = rewards.createElement().setText("  Шанс: &a{chance}%  ").setColor(Color.red).setTip("&cComing soon...");
			Element r5 = rewards.createElement().setText("  ♲  ").setColor(Color.red).setClickEvent(ClickEvent.run_command, "/worldquests rewards {id} remove").setTip("Удалить предмет");
			rewards.addLine(rewards.createLine(r1, r2, r3, r4, r5));
			rewards.send(p, "{amount}", String.valueOf(reward.getAmount()), "{chance}", "100", "{id}", String.valueOf(i));
		}
		
		TellRawText rewAdd = new TellRawText();
		Element r = rewAdd.createElement().setText("  ✚  ").setColor(Color.dark_purple).setClickEvent(ClickEvent.run_command, "/worldquests rewards new").setTip("Добавить предмет");
		rewAdd.addLine(rewAdd.createLine(r));
		rewAdd.addSeparator1();
		rewAdd.addLine(rewAdd.createLine(r));
		rewAdd.send(p);
	}
	
	public static void newReward(GDPlayer p) {
		if (!installers.containsKey(p)) return;
		WorldQuest wq = installers.get(p);
		if (wq == null) return;
		
		if (p.p.getItemInHand() == null) {
			p.sendMessage("§cВозьмите предмет в руки, чтобы его добавить.");
			return;
		}
		
		wq.rewards.add(p.p.getItemInHand());

		showMainPage(p);
		showRewards(p);
	}
	
	public static void removeReward(GDPlayer p, int id) {
		if (!installers.containsKey(p)) return;
		WorldQuest wq = installers.get(p);
		if (wq == null) return;
		
		if (wq.rewards.size() <= id) return;
		
		wq.rewards.remove(id);
		
		showMainPage(p);
		showRewards(p);
	}
	
	// Description
	
	public static void showDesc(GDPlayer p) {
		if (!installers.containsKey(p)) return;
		WorldQuest wq = installers.get(p);
		if (wq == null) return;
		
		for (int i = 0; i < wq.getDescription().size(); i++) {
			String desc = wq.getDescription().get(i);
			WorldQuestCMD.getInstallPage(2).send(p, "{description}", desc, "{index}", String.valueOf(i));
		}
		WorldQuestCMD.getInstallPage(3).send(p);
	}
	
	public static void newDesc(GDPlayer p, String desc) {
		if (!installers.containsKey(p)) return;
		WorldQuest wq = installers.get(p);
		if (wq == null) return;
		
		wq.description.add(desc);
		
		showMainPage(p);
		showDesc(p);
	}
	
	public static void removeDesc(GDPlayer p, int index) {
		if (!installers.containsKey(p)) return;
		WorldQuest wq = installers.get(p);
		if (wq == null) return;
		
		if (wq.description.size() <= index) return;
		
		wq.description.remove(index);
		
		showMainPage(p);
		showDesc(p);
	}
	
	// Areas
	
	public static void showAreas(GDPlayer p) {
		if (!installers.containsKey(p)) return;
		WorldQuest wq = installers.get(p);
		if (wq == null) return;
		
		for (int i = 0; i < wq.areas.size(); i++) {
			EllipsoidArea area = wq.areas.get(i);
			WorldQuestCMD.getInstallPage(4).send(p,
					"{center}", LocationManager.serializeLocation(area.getCenter()),
					"{width}", String.valueOf(area.getWidth()),
					"{length}", String.valueOf(area.getLength()),
					"{height}", String.valueOf(area.getHeight()),
					"{id}", String.valueOf(i));
		}
		WorldQuestCMD.getInstallPage(5).send(p);
	}
	
	public static void addArea(GDPlayer p) {
		if (!installers.containsKey(p)) return;
		WorldQuest wq = installers.get(p);
		if (wq == null) return;
		
		wq.addArea(p.getLocation());

		showMainPage(p);
		showAreas(p);
	}
	
	public static void removeArea(GDPlayer p, int id) {
		if (!installers.containsKey(p)) return;
		WorldQuest wq = installers.get(p);
		if (wq == null) return;
		
		if (wq.areas.size() <= id) return;
		
		wq.removeArea(id);

		showMainPage(p);
		showAreas(p);
	}
	
	// Blocks
	
	// Cargo items
	
	// Filters
	
	// Scores
	
	// Scheduler
	
}
