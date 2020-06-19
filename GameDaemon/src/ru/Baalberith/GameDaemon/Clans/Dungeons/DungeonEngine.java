package ru.Baalberith.GameDaemon.Clans.Dungeons;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.Vector;

import ru.Baalberith.GameDaemon.ConfigsDaemon;
import ru.Baalberith.GameDaemon.GD;
import ru.Baalberith.GameDaemon.GDPlayer;
import ru.Baalberith.GameDaemon.GDSender;
import ru.Baalberith.GameDaemon.Storage;
import ru.Baalberith.GameDaemon.Clans.Dungeons.Layer.Region;
import ru.Baalberith.GameDaemon.Clans.Dungeons.Layer.Wayback;
import ru.Baalberith.GameDaemon.Clans.Dungeons.Commands.DungeonCMD;
import ru.Baalberith.GameDaemon.Clans.Dungeons.Game.Session;
import ru.Baalberith.GameDaemon.Clans.Dungeons.Game.Sessions;
import ru.Baalberith.GameDaemon.Clans.Groups.Party.Party;
import ru.Baalberith.GameDaemon.Extra.ActionObject;
import ru.Baalberith.GameDaemon.Extra.WaitingSystem;
import ru.Baalberith.GameDaemon.Extra.Installation.TellRawText;
import ru.Baalberith.GameDaemon.Extra.Installation.TellRawText.ClickEvent;
import ru.Baalberith.GameDaemon.Extra.Installation.TellRawText.Color;
import ru.Baalberith.GameDaemon.Extra.Installation.TellRawText.Element;
import ru.Baalberith.GameDaemon.Linkage.SAAPI;
import ru.Baalberith.GameDaemon.Linkage.WEAPI;
import ru.Baalberith.GameDaemon.Utils.ItemDaemon;
import ru.Baalberith.GameDaemon.Utils.LocationManager;
import ru.Baalberith.GameDaemon.Utils.MathOperation;

public class DungeonEngine implements Listener {
	
	public static int saveShedulerTimeSeconds;
	public static double distance;
	public static int sessionTimeout;
	public static String scoreboard_text;
	public static int scoreboard_score;
	
	public static Storage dungeonStorage = new Storage("dungeonsData.json", GD.inst.getDataFolder().getPath(), false);
	public static HashMap<GDSender, Dungeon> installers = new HashMap<GDSender, Dungeon>();

	public static Sessions sessions = new Sessions();
	public static DungeonCMD dungeonCMD = new DungeonCMD();
	public static DungeonListener dungeonListener = new DungeonListener();
	
	public static void reload() {
		try {
			waitingBlockClick.clear();
			installers.clear();
			ConfigsDaemon.mainConfig.reload();
			ConfigurationSection c = ConfigsDaemon.mainConfig.getConfigurationSection("dungeons");
			
			distance = c.getDouble("distance", 10D);
			sessionTimeout = c.getInt("sessionTimeout", 60);
			scoreboard_text = c.getString("scoreboard.text", "Время: {m}:{s}").replace("&", "§");
			scoreboard_score = c.getInt("scoreboard.score", 999);
			saveShedulerTimeSeconds = c.getInt("saveShedulerTimeSeconds", 300);
			
			sessions.reload();
			Dungeons.reload();
			dungeonCMD.reload();
		} catch (Exception e) { e.printStackTrace(); }
	}
	
	public static void saveAllToDisk() {
		Dungeons.saveTopTimeDungeons();
		Dungeons.saveDungeonsToFile();
	}
	
	public static void dungeonWaybackProcess(Location clickBlock, GDPlayer player) {
	
		// Найти существующую сессию игрока.
		Session session = Sessions.getSessionWithPlayer(player.getName());
		if (session == null) return;
		
		
		// Проверить является ли этот клик по wayback данжа.
		Layer layer = session.getLayer();
		if (layer == null || !layer.isWaybackByLocation(clickBlock)) return;

		
		// Проверить наличие Combat'a.
		if (SAAPI.inCombat(player.getBukkitPlayer())) {
			Message.leaveInCombat.send(player);
			return;
		}
		
		
		// Кинуть запрос на телепорт из данжа.
		WaitingSystem.createRequest(ActionObject.DungeonWayback, player, () -> {
			if (SAAPI.inCombat(player.getBukkitPlayer())) {
				Message.cannotLeaveCombat.send(player);
				return;
			}
			
			player.addDungeonsCleared(session.getDungeon().getId());
			
			session.returnPlayer(player);
			session.deleteGDPlayerByWayback(player);
			session.getDungeon().runExitCommand(player.getName());
			if (session.getMembers().size() == 0) {
				session.saveRunTime();
				session.getDungeon().giveReward(session.getParty().asPlayers(), session.getLevel().number());
			}
			
			// Деактивируем кнопку запроса в чате.
			WaitingSystem.removeRequest(player.getName(), ActionObject.DungeonWayback);
		}, "Телепортироватся из данжа", false);
	}
	
	public static void dungeonGateProcess(Location clickBlock, GDPlayer p) {
		
		// Проверить, если ли уже сессия с этим игроком
		if (Sessions.getSessionWithPlayer(p.getName()) != null) return;
		
		// Найти данж по воротам
		Dungeon dungeon = null;
		for (Dungeon dung : Dungeons.dungeons) {
			if (dung.isEnabled() && dung.isPortal(clickBlock)) {
				dungeon = dung;
			}
		}
		if (dungeon == null) return;
		
		
		// Проверить наличие Combat'a
		if (SAAPI.inCombat(p.getBukkitPlayer())) {
			Message.requirement_combat.send(p);
			return;
		}
		
		
		// Проверить наличие группы и владельца
		if (!p.hasParty()) {
			Message.noParty.send(p);
			return;
		}
		
		
		Party party = p.getParty();
		if (!party.getOwner().equals(p.getName())) {
			Message.requirement_owner.send(p);
			return;
		}
		
		
		// Проверить кулдаун на данж.
		long remCooldown = p.getDungeonRemCooldown(dungeon.getId());
		if (remCooldown < 0) {
			p.sendMessage(MathOperation.makeTimeToString(Message.selfCooldown.replace("{name}", dungeon.getName()).get(), -remCooldown));
			return;
		}
		
		
		// Проверить соответствие уровня группы и уровня данжа
		Level level = dungeon.getLevelByPartySize(party.getSize());
		if (level == null || !level.isEnabled()) {
			Message.noDungeon.replace("{name}", dungeon.getName()).replace("{size}", party.getSize()).send(p);
			return;
		}
		
		
		List<GDPlayer> players = party.asPlayers();
		
		// Все ли игроки из группы онлайн?
		for (GDPlayer gdp : players) {
			if (gdp == null || gdp.p == null) {
				Message.requirement_online.replace("{name}", dungeon.getName()).replace("{size}", party.getSize()).send(p);
				return;
			}
		}

		// Проверяем наличие всех игроков группы у данжа (10 блоков)
		for (GDPlayer gdp : players) {
			if (gdp == null || MathOperation.distance3D(gdp.getLocation(), clickBlock) > DungeonEngine.distance) {
				Message.requirement_distance.replace("{name}", dungeon.getName()).replace("{size}", level.number()).send(p);
				// минимум 1 игрок не рядом с данжем
				return;
			}
		}
		
		
		// Проверить наличие ключа у всех членов группы
		for (GDPlayer gdp : players) {
			if (gdp == null || !dungeon.hasKey(gdp)) {
				// у одного игрока нет ключа в этот данж
				Message.requirement_key.replace("{name}", dungeon.getName()).replace("{size}", level.number()).send(p);
				return;
			}
		}
		
		
		// Отправить всем подтверждение (создаём сессию)
		Session session = new Session(dungeon, party, level);
		Sessions.addSession(session);
		session.sendRequestToMembers();
		
	}
	
	public static boolean deathInDungeon(PlayerDeathEvent e) {
		GDPlayer p = GD.getGDPlayer(e.getEntity());
		if (!p.inDungeon()) return false;
		
		Session session = Sessions.getSessionWithPlayer(p.getName());
		session.deleteGDPlayerByWayback(p);
		return true;
	}
	
	// Настройки данжей
	
	public static void help(GDSender p) {
		p.sendMessage("> /dungeons install - установить данж для редактирования.");
		p.sendMessage("> /dungeons new <id> - создать новый данж.");
	}
	
	public static void showMainPage(GDSender p) {
		if (!installers.containsKey(p)) return;
		Dungeon dungeon = installers.get(p);
		
		if (dungeon.isEnabled()) {
			DungeonCMD.getInstallPage(0).send(p, "{id}", dungeon.getId(), "{name}", dungeon.getName().replace(" ", "_"));
		} else {
			DungeonCMD.getInstallPage(1).send(p, "{id}", dungeon.getId(), "{name}", dungeon.getName().replace(" ", "_"));
		}
		ItemStack key = dungeon.getKey();
		String keyStr = key == null ? "material:data" : key.getType().toString().concat(":"+key.getDurability());
		ItemStack reward = dungeon.getReward();
		String rewardStr = reward == null ? "material:data" : reward.getType().toString().concat(":"+reward.getDurability());
		
		String exitCommand = dungeon.getExitCommand() == null ? "command" : dungeon.getExitCommand();
		
		DungeonCMD.getInstallPage(2).send(p, "{key}", keyStr, "{topSize}", dungeon.getTopSize()+"", "{reward}", rewardStr, "{joinCooldown}", ""+dungeon.getJoinCooldown(), "{heightOffset}", ""+dungeon.getHeightOffset(),
				"{exitCommand}", exitCommand);
	}
	
	public static void showAllDungeons(GDSender p) {
		TellRawText dungeons = new TellRawText();
		Element[] elements = new Element[2*Dungeons.dungeons.size()];
		for (int i = 0, j = 0; j < elements.length; i++, j+=2) {
			Dungeon d = Dungeons.dungeons.get(i);
			Element e1 = dungeons.createElement().setText(d.getName()).setColor(d.isEnabled()?Color.green:Color.red).setClickEvent(ClickEvent.run_command, "/dungeons install ".concat(d.getId()));
			Element e2 = dungeons.createElement().setText(", ");
			elements[j] = e1;
			elements[j+1] = e2;
		}
		dungeons.addSeparator1();
		dungeons.addLine(dungeons.createLine(elements));
		Element add = dungeons.createElement().setText("  ✚  ").setColor(Color.dark_purple).setClickEvent(ClickEvent.suggest_command, "/dungeons new ".concat(String.valueOf(Dungeons.getAmount()+1))).setTip("Создать новый данж");
		dungeons.addSeparator2();
		dungeons.addLine(dungeons.createLine(add));
		dungeons.addSeparator2();
		dungeons.send(p);
	}
	
	public static void install(GDSender p, String id) {
		Dungeon dungeon = Dungeons.getDungeonById(id);
		if (dungeon == null) {
			p.sendMessage("This id doesn't exists.");
			return;
		}
		
		installers.put(p, dungeon);
		
		showMainPage(p);
	}
	
	public static void uninstall(GDSender p) {
		installers.remove(p);
	}
	
	// Вызывается когда нужно поставить portal, spawn, returnSpawn или обавить wayback.
	public static void showCoordsChooseMenu(GDSender p, String type) {
		DungeonCMD.getInstallPage(15).send(p, "{type}", type);
	}
	
	// --->>>
	public static HashMap<GDSender, String> waitingBlockClick = new HashMap<GDSender, String>();
	
	public static void coordsClick(GDSender p, String type) {
		waitingBlockClick.put(p, type);
		p.sendMessage("Waiting for click...");
	}
	
	public static void coordsClick_(GDSender p, Location clickLoc, String type) {
		
		if (!installers.containsKey(p)) {
			waitingBlockClick.remove(p);
			return;
		}
		Dungeon dungeon = installers.get(p);
		
		switch (type) {
		case "wayback":
			dungeon.getLayers().forEach(l -> l.newWayback(clickLoc.clone()));
			showMainPage(p);
			waybacks(p);
			break;
		case "spawn":
			dungeon.setFirstLayerSpawn(clickLoc);
			showMainPage(p);
			break;
		case "returnSpawn":
			dungeon.setReturnSpawn(clickLoc);
			showMainPage(p);
			break;
		case "portal":
			dungeon.setPortal(clickLoc);
			showMainPage(p);
			break;
		case "hologram":
			dungeon.setHologramLocation(clickLoc);
			showMainPage(p);
			break;
		default:
			p.sendMessage("Что ты ввёл вообще???");
			break;
		}
		
		waitingBlockClick.remove(p);
		Dungeons.saveDungeonToFile(dungeon);
		Dungeons.reloadExact(dungeon.getId());
	}
	
	public static void coordsCurrent(GDSender p, String type) {
		if (!installers.containsKey(p)) return;
		Dungeon dungeon = installers.get(p);
		
		Location cur = GD.getGDPlayer(p.getName()).getLocation();

		switch (type) {
		case "wayback":
			dungeon.getLayers().forEach(l -> l.newWayback(cur.clone()));
			showMainPage(p);
			waybacks(p);
			break;
		case "spawn":
			dungeon.setFirstLayerSpawn(cur);
			showMainPage(p);
			break;
		case "returnSpawn":
			dungeon.setReturnSpawn(cur);
			showMainPage(p);
			break;
		case "portal":
			dungeon.setPortal(cur);
			showMainPage(p);
			break;
		case "hologram":
			dungeon.setHologramLocation(cur);
			showMainPage(p);
			break;
		default:
			p.sendMessage("Что ты ввёл вообще???");
			break;
		}
		Dungeons.saveDungeonToFile(dungeon);
		Dungeons.reloadExact(dungeon.getId());
	}
	// <<<---
	
	public static void waybacks(GDSender p) {
		if (!installers.containsKey(p)) return;
		Dungeon dungeon = installers.get(p);
		
		showMainPage(p);
		int i = 1;
		for (Wayback wb : dungeon.getFirstLayer().getWaybacks()) {
			Location loc = wb.getLocation();
			DungeonCMD.getInstallPage(11).send(p, "{world}", loc.getWorld().getName(), "{x}", ""+loc.getBlockX(),
					"{y}", ""+loc.getBlockY(), "{z}", ""+loc.getBlockZ(), "{waybackNumber}", ""+i);
			i++;
		}
		DungeonCMD.getInstallPage(12).send(p);
	}
	
	// Очень сложная реализация, не имеет смысла делать, всё равно проще это сделать через конфиг.
//	public static void commands(GDSender p) {
//		if (!installers.containsKey(p)) return;
//		Dungeon dungeon = installers.get(p);
//	}
	
	public static void regions(GDSender p) {
		if (!installers.containsKey(p)) return;
		Dungeon dungeon = installers.get(p);

		showMainPage(p);
		
		int i = 1;
		for (Region r : dungeon.getRegionsByFirstLayer()) {
			Location pos1 = r.getPos1();
			Location pos2 = r.getPos2();
			String world = r.getWorldName();
			DungeonCMD.getInstallPage(13).send(p, "{world}", world,
					"{x1}", ""+pos1.getBlockX(), "{y1}", ""+pos1.getBlockY(), "{z1}", ""+pos1.getBlockZ(),
					"{x2}", ""+pos2.getBlockX(), "{y2}", ""+pos2.getBlockY(), "{z2}", ""+pos2.getBlockZ(), "{regionNumber}", ""+i);
			i++;
		}
		DungeonCMD.getInstallPage(14).send(p);
	}
	
	public static void layers(GDSender p) {
		if (!installers.containsKey(p)) return;
		Dungeon dungeon = installers.get(p);

		showMainPage(p);
		for (Layer l : dungeon.getLayers()) {
			if (l.isEnabled()) {
				DungeonCMD.getInstallPage(8).send(p, "{layerNumber}", ""+l.number());
			} else {
				DungeonCMD.getInstallPage(9).send(p, "{layerNumber}", ""+l.number());
			}
		}
		DungeonCMD.getInstallPage(10).send(p, "{number}", String.valueOf(dungeon.getLayers().size()+1));
	}
	
	public static void levels(GDSender p) {
		if (!installers.containsKey(p)) return;
		Dungeon dungeon = installers.get(p);

		showMainPage(p);
		for (Level l : dungeon.getLevels()) {
			if (l.isEnabled()) {
				DungeonCMD.getInstallPage(5).send(p, "{level}", ""+l.number(), "{time}", ""+l.getTime());
			} else {
				DungeonCMD.getInstallPage(6).send(p, "{level}", ""+l.number(), "{time}", ""+l.getTime());
			}
		}
		DungeonCMD.getInstallPage(7).send(p, "{number}", String.valueOf(dungeon.getLevels().size()+1));
	}
	
	public static void commands(GDSender p) {
		if (!installers.containsKey(p)) return;
		
		showMainPage(p);
		DungeonCMD.getInstallPage(3).send(p);
	}
	
	public static void changename(GDSender p, String name) {
		if (!installers.containsKey(p)) return;
		Dungeon dungeon = installers.get(p);
		
		dungeon.setName(name.replace("_", " "));
		Dungeons.saveDungeonToFile(dungeon);
		Dungeons.reloadExact(dungeon.getId());
		
		showMainPage(p);
	}
	
	public static void off(GDSender p) {
		if (!installers.containsKey(p)) return;
		Dungeon dungeon = installers.get(p);
		
		Sessions.closeSessionsWithDungeon(dungeon);
		dungeon.off();
		Dungeons.saveDungeonToFile(dungeon);
		Dungeons.reloadExact(dungeon.getId());
		
		showMainPage(p);
	}
	
	public static void on(GDSender p) {
		if (!installers.containsKey(p)) return;
		Dungeon dungeon = installers.get(p);
		
		if (!dungeon.on()) {
			p.sendMessage("§сНе все обязательные параметры были указаны.");
			return;
		}
		Dungeons.saveDungeonToFile(dungeon);
		Dungeons.reloadExact(dungeon.getId());
		
		showMainPage(p);
	}
	
	public static void remove(GDSender p, String id) {
		Dungeon dungeon = Dungeons.getDungeonById(id);
		if (dungeon == null) return;
		
		dungeon.off();
		Dungeons.removeDungeon(dungeon);
		Dungeons.saveDungeonToFile(dungeon);
		Dungeons.reloadExact(dungeon.getId());
		
	}
	
	public static void portal(GDSender p) {
		if (!installers.containsKey(p)) return;

		showMainPage(p);
		DungeonCMD.getInstallPage(15).send(p, "{type}", "portal");
	}
	
	public static void returnSpawn(GDSender p) {
		if (!installers.containsKey(p)) return;

		showMainPage(p);
		DungeonCMD.getInstallPage(15).send(p, "{type}", "returnSpawn");
	}
	
	public static void spawn(GDSender p) {
		if (!installers.containsKey(p)) return;

		showMainPage(p);
		DungeonCMD.getInstallPage(15).send(p, "{type}", "spawn");
	}
	
	public static void hologram(GDSender p) {
		if (!installers.containsKey(p)) return;

		showMainPage(p);
		DungeonCMD.getInstallPage(15).send(p, "{type}", "hologram");
	}
	
	public static void addWayback(GDSender p) {
		if (!installers.containsKey(p)) return;

		showMainPage(p);
		DungeonCMD.getInstallPage(15).send(p, "{type}", "wayback");
	}
	
	public static void removeWayback(GDSender p, int number) {
		if (!installers.containsKey(p)) return;
		Dungeon dungeon = installers.get(p);
		
		dungeon.removeWayback(number-1);
		Dungeons.saveDungeonToFile(dungeon);
		Dungeons.reloadExact(dungeon.getId());
		
		showMainPage(p);
		waybacks(p);
	}
	
	public static void key(GDSender p, String src) {
		if (!installers.containsKey(p)) return;
		Dungeon dungeon = installers.get(p);
		
		ItemStack key = ItemDaemon.fromString(src);
		dungeon.setKey(key);
		Dungeons.saveDungeonToFile(dungeon);
		Dungeons.reloadExact(dungeon.getId());
		
		showMainPage(p);
	}
	
	public static void reward(GDSender p, String src) {
		if (!installers.containsKey(p)) return;
		Dungeon dungeon = installers.get(p);
		
		ItemStack reward = ItemDaemon.fromString(src);
		dungeon.setReward(reward);
		Dungeons.saveDungeonToFile(dungeon);
		Dungeons.reloadExact(dungeon.getId());
		
		showMainPage(p);
	}
	
	public static void topSize(GDSender p, int topSize) {
		if (!installers.containsKey(p)) return;
		Dungeon dungeon = installers.get(p);
		
		dungeon.setTopSize(topSize);
		Dungeons.saveDungeonToFile(dungeon);
		Dungeons.reloadExact(dungeon.getId());

		showMainPage(p);
	}
	
	public static void heightOffset(GDSender p, int heightOffset) {
		if (!installers.containsKey(p)) return;
		Dungeon dungeon = installers.get(p);
		
		dungeon.setHeightOffset(heightOffset);
		Dungeons.saveDungeonToFile(dungeon);
		Dungeons.reloadExact(dungeon.getId());
		
		showMainPage(p);
	}
	
	public static void joinCooldown(GDSender p, int joinCooldown) {
		if (!installers.containsKey(p)) return;
		Dungeon dungeon = installers.get(p);
		
		dungeon.setJoinCooldown(joinCooldown);
		Dungeons.saveDungeonToFile(dungeon);
		
		showMainPage(p);
	}
	
	public static void addRegion(GDSender s) throws IncompleteRegionException {
		if (!installers.containsKey(s)) return;
		
		if (!WEAPI.check()) {
			s.sendMessage("WorldEdit не активирован");
			return;
		}
		
		LocalSession ls = WEAPI.getSession(s.getName());
		if (ls == null) {
			s.sendMessage("§сВыделите территорию, перед добавлением.");
			return;
		}
		
		com.sk89q.worldedit.regions.Region reg = ls.getSelection(ls.getSelectionWorld());
		Vector min = reg.getMinimumPoint();
		Vector max = reg.getMaximumPoint();
		DungeonCMD.getInstallPage(16).send(s, "{world}", reg.getWorld().getName(), 
				"{x1}", ""+min.getBlockX(), "{y1}", ""+min.getBlockY(), "{z1}", ""+min.getBlockZ(),
				"{x2}", ""+max.getBlockX(), "{y2}", ""+max.getBlockY(), "{z2}", ""+max.getBlockZ());
	}
	
	public static void newRegion(GDSender p, String world, String x1, String y1, String z1, String x2, String y2, String z2) {
		if (!installers.containsKey(p)) return;
		Dungeon dungeon = installers.get(p);
		
		// world x1 z1 x2 z2 y1 y2
		dungeon.addRegion(world, x1, z1, x2, z2, y1, y2);
		Dungeons.saveDungeonToFile(dungeon);
		Dungeons.reloadExact(dungeon.getId());
		
		showMainPage(p);
		regions(p);
	}
	
	public static void removeRegion(GDSender p, int number) {
		if (!installers.containsKey(p)) return;
		Dungeon dungeon = installers.get(p);
		
		dungeon.removeRegion(number-1);
		Dungeons.saveDungeonToFile(dungeon);
		Dungeons.reloadExact(dungeon.getId());
		
		showMainPage(p);
		regions(p);
	}
	
	public static void createDungeon(GDSender p, String id) {
		
		Dungeon dungeon = Dungeons.getDungeonById(id);
		if (dungeon != null) {
			p.sendMessage("§cДанж с таким id уже существует.");
			return;
		}
		
		dungeon = Dungeons.createNew(false, id, id, null, null, null, null, null, 0, 0, null, null, null, null, -1, null);
		Dungeons.dungeons.add(dungeon);
		Dungeons.saveDungeonToFile(dungeon);
		Dungeons.reloadExact(dungeon.getId());
		
		installers.put(p, dungeon);
		
		showMainPage(p);
	}
	
	public static void addLevel(GDSender p, int number) {
		if (!installers.containsKey(p)) return;
		Dungeon dungeon = installers.get(p);
		
		if (dungeon.hasLevel(number)) return;
		
		Level level = Dungeons.createNewLevel(false, new ArrayList<String>(), number, 60, dungeon.getLayers().size(), dungeon.getHeightOffset());
		dungeon.getLevels().add(number-1, level);
		Dungeons.saveDungeonToFile(dungeon);
		Dungeons.reloadExact(dungeon.getId());
		
		levels(p);
	}
	
	public static void removeLevel(GDSender p, int number) {
		if (!installers.containsKey(p)) return;
		Dungeon dungeon = installers.get(p);
		
		if (!dungeon.hasLevel(number)) return;
		
		Level level = dungeon.getLevelByPartySize(number);
		dungeon.getLevels().remove(level);
		Dungeons.saveDungeonToFile(dungeon);
		Dungeons.reloadExact(dungeon.getId());
		
		levels(p);
	}
	
	public static void addLayer(GDSender p, int number) {
		if (!installers.containsKey(p)) return;
		Dungeon dungeon = installers.get(p);
		
		if (dungeon.hasLayer(number)) return;
		
		Layer layer = Dungeons.createNewLayer(false, new ArrayList<String>(), new ArrayList<String>(), LocationManager.serializeLocation(dungeon.getFirstLayerSpawn()), dungeon.getHeightOffset(), number);
		dungeon.getLayers().add(number-1, layer);
		Dungeons.saveDungeonToFile(dungeon);
		Dungeons.reloadExact(dungeon.getId());
		
		layers(p);
	}
	
	public static void removeLayer(GDSender p, int number) {
		if (!installers.containsKey(p)) return;
		Dungeon dungeon = installers.get(p);

		if (!dungeon.hasLayer(number)) return;
		
		Layer layer = dungeon.getLayerByNumber(number);
		dungeon.getLayers().remove(layer);
		Dungeons.saveDungeonToFile(dungeon);
		Dungeons.reloadExact(dungeon.getId());
		
		layers(p);
	}
	
	public static void setLevelTime(GDSender p, int number, int time) {
		if (!installers.containsKey(p)) return;
		Dungeon dungeon = installers.get(p);
		
		Level level = dungeon.getLevelByPartySize(number);
		if (level == null) return;
		
		level.setTime(time);
		Dungeons.saveDungeonToFile(dungeon);
		
		levels(p);
	}
	
	public static void levelOn(GDSender p, int number) {
		if (!installers.containsKey(p)) return;
		Dungeon dungeon = installers.get(p);
		
		Level level = dungeon.getLevelByPartySize(number);
		if (level == null) return;
		
		if (!level.on()) {
			p.sendMessage("§cНе все параметры были установлены.");
			return;
		}
		Dungeons.saveDungeonToFile(dungeon);
		Dungeons.reloadExact(dungeon.getId());

		levels(p);
	}
	
	public static void levelOff(GDSender p, int number) {
		if (!installers.containsKey(p)) return;
		Dungeon dungeon = installers.get(p);
		
		Level level = dungeon.getLevelByPartySize(number);
		if (level == null) return;
		
		level.off();
		Dungeons.saveDungeonToFile(dungeon);
		Dungeons.reloadExact(dungeon.getId());

		levels(p);
	}

	public static void layerOn(GDSender p, int number) {
		if (!installers.containsKey(p)) return;
		Dungeon dungeon = installers.get(p);
		
		Layer layer = dungeon.getLayerByNumber(number);
		if (layer == null) return;
		
		if (!layer.on()) {
			p.sendMessage("§cНе все параметры были установлены.");
			return;
		}
		Dungeons.saveDungeonToFile(dungeon);
		Dungeons.reloadExact(dungeon.getId());
		
		layers(p);
	}

	public static void layerOff(GDSender p, int number) {
		if (!installers.containsKey(p)) return;
		Dungeon dungeon = installers.get(p);
		
		Layer layer = dungeon.getLayerByNumber(number);
		if (layer == null) return;
		
		layer.off();
		Dungeons.saveDungeonToFile(dungeon);
		Dungeons.reloadExact(dungeon.getId());
		
		layers(p);
	}
	
	public static void exitCommand(GDSender p, String... args) {
		if (!installers.containsKey(p)) return;
		Dungeon dungeon = installers.get(p);
		
		StringBuilder cmd = new StringBuilder();
		for (int i = 1; i < args.length; i++) {
			cmd.append(args[i]);
			if (i < args.length-1) cmd.append(" ");
		}
		
		dungeon.setExitCommand(cmd.toString());
		Dungeons.saveDungeonToFile(dungeon);
		Dungeons.reloadExact(dungeon.getId());
		
		showMainPage(p);
	}
}
