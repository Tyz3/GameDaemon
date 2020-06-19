package ru.Baalberith.GameDaemon.Clans.Dungeons.Commands;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import com.sk89q.worldedit.IncompleteRegionException;

import ru.Baalberith.GameDaemon.GD;
import ru.Baalberith.GameDaemon.GDSender;
import ru.Baalberith.GameDaemon.Clans.Dungeons.DungeonEngine;
import ru.Baalberith.GameDaemon.Clans.Dungeons.Dungeons;
import ru.Baalberith.GameDaemon.Extra.Installation.TellRawText;
import ru.Baalberith.GameDaemon.Extra.Installation.TellRawText.ClickEvent;
import ru.Baalberith.GameDaemon.Extra.Installation.TellRawText.Color;
import ru.Baalberith.GameDaemon.Extra.Installation.TellRawText.Element;
import ru.Baalberith.GameDaemon.Extra.Installation.InstallationTemplate;
import ru.Baalberith.GameDaemon.Utils.CompleteHelper;
import ru.Baalberith.GameDaemon.Utils.ThreadDaemon;

public class DungeonCMD extends InstallationTemplate implements CommandExecutor, TabCompleter {
	
	public DungeonCMD() {
		super("dungeons");
		GD.inst.getCommand("dungeons").setTabCompleter(this);
	}
	
	@Override
	public void reload() {
		initialize(17);
		
		// 0
		TellRawText mainHeaderON = new TellRawText();
		mainHeaderON.addSeparator1();
		Element mhe11 = mainHeaderON.createElement().setText("  Ид: §a{id}  ");
		Element mhe12 = mainHeaderON.createElement().setText("  Данж: §a{name}  ");
		Element mhe13 = mainHeaderON.createElement().setText("  ✎  ").setColor(Color.yellow).setClickEvent(ClickEvent.suggest_command, "/dungeons changename {name}").setTip("Изменить название");
		Element mhe14 = mainHeaderON.createElement().setText("  ✔  ").setColor(Color.green).setClickEvent(ClickEvent.run_command, "/dungeons off").setTip("Нажми, чтобы выключить данж");
		Element mhe15 = mainHeaderON.createElement().setText("  ♲  ").setColor(Color.dark_red).setClickEvent(ClickEvent.suggest_command, "/dungeons remove {id}").setTip("Нажмите, чтобы удалить данж");
		Element mhe16 = mainHeaderON.createElement().setText("  ☒  ").setColor(Color.aqua).setClickEvent(ClickEvent.run_command, "/dungeons uninstall").setTip("Закончить настройку данжа");
		mainHeaderON.addLine(mainHeaderON.createLine(mhe11, mhe12, mhe13, mhe14, mhe15, mhe16));
		addInstallPage(mainHeaderON);
		
		// 1
		TellRawText mainHeaderOFF = new TellRawText();
		mainHeaderOFF.addSeparator1();
		Element mhe21 = mainHeaderOFF.createElement().setText("  Ид: §a{id}  ");
		Element mhe22 = mainHeaderOFF.createElement().setText("  Данж: §a{name}  ");
		Element mhe23 = mainHeaderOFF.createElement().setText("  ✎  ").setColor(Color.yellow).setClickEvent(ClickEvent.suggest_command, "/dungeons changename {name}").setTip("Изменить название");
		Element mhe24 = mainHeaderOFF.createElement().setText("  ✘  ").setColor(Color.red).setClickEvent(ClickEvent.run_command, "/dungeons on").setTip("Нажми, чтобы включить данж");
		Element mhe25 = mainHeaderOFF.createElement().setText("  ♲  ").setColor(Color.dark_red).setClickEvent(ClickEvent.suggest_command, "/dungeons remove {id}").setTip("Нажмите, чтобы удалить данж");
		Element mhe26 = mainHeaderOFF.createElement().setText("  ☒  ").setColor(Color.aqua).setClickEvent(ClickEvent.run_command, "/dungeons uninstall").setTip("Закончить настройку данжа");
		mainHeaderOFF.addLine(mainHeaderOFF.createLine(mhe21, mhe22, mhe23, mhe24, mhe25, mhe26));
		addInstallPage(mainHeaderOFF);
		
		// 2
		TellRawText mainBody = new TellRawText();
		Element mb1 = mainBody.createElement().setColor(Color.green).setText("  Portal  ").setClickEvent(ClickEvent.run_command, "/dungeons portal").setTip("Установить портал");
		Element mb2 = mainBody.createElement().setColor(Color.green).setText("  ReturnSpawn  ").setClickEvent(ClickEvent.run_command, "/dungeons returnSpawn").setTip("Установить точку возврата");
		Element mb3 = mainBody.createElement().setColor(Color.green).setText("  HeightOffset  ").setClickEvent(ClickEvent.suggest_command, "/dungeons heightOffset {heightOffset}").setTip("Установить высоту слота");
		Element mb4 = mainBody.createElement().setColor(Color.green).setText("  Spawn  ").setClickEvent(ClickEvent.run_command, "/dungeons spawn").setTip("Установить точку появления первого слота");
		mainBody.addLine(mainBody.createLine(mb1, mb2, mb3, mb4));
		Element mb5 = mainBody.createElement().setColor(Color.green).setText("  Levels...  ").setClickEvent(ClickEvent.run_command, "/dungeons levels").setTip("Настроить уровни");
		Element mb6 = mainBody.createElement().setColor(Color.green).setText("  Layers...  ").setClickEvent(ClickEvent.run_command, "/dungeons layers").setTip("Настроить слоты");
		mainBody.addLine(mainBody.createLine(mb5, mb6));
		Element mb7 = mainBody.createElement().setColor(Color.yellow).setText("  Hologram  ").setClickEvent(ClickEvent.run_command, "/dungeons hologram").setTip("Установить голограмму");
		Element mb8 = mainBody.createElement().setColor(Color.yellow).setText("  Key  ").setClickEvent(ClickEvent.suggest_command, "/dungeons key {key}").setTip("Установить ключ");
		Element mb9 = mainBody.createElement().setColor(Color.yellow).setText("  TopSize  ").setClickEvent(ClickEvent.suggest_command, "/dungeons topSize {topSize}").setTip("Установить размер топа");
		Element mb10 = mainBody.createElement().setColor(Color.yellow).setText("  JoinCooldown  ").setClickEvent(ClickEvent.suggest_command, "/dungeons joinCooldown {joinCooldown}").setTip("Установить кулдаун данжа");
		Element mb11 = mainBody.createElement().setColor(Color.yellow).setText("  Reward  ").setClickEvent(ClickEvent.suggest_command, "/dungeons reward {reward}").setTip("Установить награду за прохождение");
		mainBody.addLine(mainBody.createLine(mb7 ,mb8, mb9, mb10, mb11));
		Element mb12 = mainBody.createElement().setColor(Color.yellow).setText("  Waybacks...  ").setClickEvent(ClickEvent.run_command, "/dungeons waybacks").setTip("Настроить точки выхода");
		Element mb13 = mainBody.createElement().setColor(Color.yellow).setText("  Regions...  ").setClickEvent(ClickEvent.run_command, "/dungeons regions").setTip("Настроить область очистки");
		Element mb14 = mainBody.createElement().setColor(Color.yellow).setText("  Commands...  ").setClickEvent(ClickEvent.run_command, "/dungeons commands").setTip("Настроить список команд");
		Element mb15 = mainBody.createElement().setColor(Color.yellow).setText("  ExitCommand  ").setClickEvent(ClickEvent.suggest_command, "/dungeons exitCommand {exitCommand}").setTip("Выполняется после завершения");
		mainBody.addLine(mainBody.createLine(mb12, mb13, mb14, mb15));
		mainBody.addSeparator2();
		addInstallPage(mainBody);
		
		// 3
		TellRawText commands = new TellRawText();
		Element c = commands.createElement().setText("Настраивать через чат - самоубийство. Пусть это лучше Женя сделает)").setColor(Color.red);
//		Element c1 = commands.createElement().setText("[{minLevel}-{maxLevel}] ");
//		Element c2 = commands.createElement().setText("♲ ").setColor(Color.dark_red).setClickEvent(ClickEvent.run_command, "/dungeons cmd remove {index}").setTip("Удалить команду");
//		Element c3 = commands.createElement().setText("/{command}").setColor(Color.gray).setClickEvent(ClickEvent.suggest_command, "/{command}");
		commands.addLine(commands.createLine(c));
		addInstallPage(commands);
		
		// 4
		TellRawText newCommand = new TellRawText();
//		Element nc1 = newCommand.createElement().setText("✚").setColor(Color.dark_purple).setClickEvent(ClickEvent.suggest_command, "/dungeons cmd new 1,3,4 /your_command").setTip("Новая команда");
//		newCommand.addLine(newCommand.createLine(nc1));
		newCommand.addSeparator2();
		addInstallPage(newCommand);
		
		// 5
		TellRawText levelON = new TellRawText();
		Element lv1 = levelON.createElement().setText("  Уровень §a{level}  ");
		Element lv2 = levelON.createElement().setText("  Время {time} c.  ").setClickEvent(ClickEvent.suggest_command, "/dungeons level {level} time {time}").setTip("Установить время прохождения");
		Element lv3 = levelON.createElement().setText("  ✔  ").setColor(Color.green).setClickEvent(ClickEvent.run_command, "/dungeons level {level} off").setTip("Нажми, чтобы выключить");
		Element lv4 = levelON.createElement().setText("  ♲  ").setColor(Color.dark_red).setClickEvent(ClickEvent.run_command, "/dungeons level {level} remove").setTip("Удалить уровень");
		levelON.addLine(levelON.createLine(lv1, lv2, lv3, lv4));
		addInstallPage(levelON);
		
		// 6
		TellRawText levelOFF = new TellRawText();
		Element lv5 = levelOFF.createElement().setText("  Уровень §a{level}  ");
		Element lv6 = levelOFF.createElement().setText("  Время {time} c.  ").setClickEvent(ClickEvent.suggest_command, "/dungeons level {level} time {time}").setTip("Установить время прохождения");
		Element lv7 = levelOFF.createElement().setText("  ✘  ").setColor(Color.red).setClickEvent(ClickEvent.run_command, "/dungeons level {level} on").setTip("Нажми, чтобы включить");
		Element lv8 = levelOFF.createElement().setText("  ♲  ").setColor(Color.dark_red).setClickEvent(ClickEvent.run_command, "/dungeons level {level} remove").setTip("Удалить уровень");
		levelOFF.addLine(levelOFF.createLine(lv5, lv6, lv7, lv8));
		addInstallPage(levelOFF);
		
		// 7
		TellRawText newLevel = new TellRawText();
		Element nlv = newLevel.createElement().setText("  ✚  ").setColor(Color.dark_purple).setClickEvent(ClickEvent.suggest_command, "/dungeons level new {number}").setTip("Добавить уровень");
		newLevel.addLine(newLevel.createLine(nlv));
		newLevel.addSeparator2();
		addInstallPage(newLevel);
		
		// 8
		TellRawText layerON = new TellRawText();
		Element la1 = layerON.createElement().setText("  Слот §a{layerNumber}  ");
		Element la2 = layerON.createElement().setText("  ✔  ").setColor(Color.green).setClickEvent(ClickEvent.run_command, "/dungeons layer {layerNumber} off").setTip("Выключить слот");
		Element la3 = layerON.createElement().setText("  ♲  ").setColor(Color.dark_red).setClickEvent(ClickEvent.run_command, "/dungeons layer {layerNumber} remove").setTip("Удалить слот");
		layerON.addLine(layerON.createLine(la1, la2, la3));
		addInstallPage(layerON);
		
		// 9
		TellRawText layerOFF = new TellRawText();
		Element la4 = layerOFF.createElement().setText("  Слот §a{layerNumber}  ");
		Element la5 = layerOFF.createElement().setText("  ✘  ").setColor(Color.red).setClickEvent(ClickEvent.run_command, "/dungeons layer {layerNumber} on").setTip("Включить слот");
		Element la6 = layerOFF.createElement().setText("  ♲  ").setColor(Color.dark_red).setClickEvent(ClickEvent.run_command, "/dungeons layer {layerNumber} remove").setTip("Удалить слот");
		layerOFF.addLine(layerOFF.createLine(la4, la5, la6));
		addInstallPage(layerOFF);
		
		// 10
		TellRawText newLayer = new TellRawText();
		Element nla = newLayer.createElement().setText("  ✚  ").setColor(Color.dark_purple).setClickEvent(ClickEvent.suggest_command, "/dungeons layer new {number}").setTip("Добавить слот");
		newLayer.addLine(newLayer.createLine(nla));
		newLayer.addSeparator2();
		addInstallPage(newLayer);
		
		// 11
		TellRawText waybacks = new TellRawText();
		Element w1 = waybacks.createElement().setText("  Wayback {world} {x} {y} {z}  ");
		Element w2 = waybacks.createElement().setText("  ♲  ").setColor(Color.dark_red).setClickEvent(ClickEvent.run_command, "/dungeons wayback {waybackNumber} remove").setTip("Удалить точку выхода");
		waybacks.addLine(waybacks.createLine(w1, w2));
		addInstallPage(waybacks);
		
		// 12
		TellRawText newWayback = new TellRawText();
		Element nw = newWayback.createElement().setText("  ✚  ").setColor(Color.dark_purple).setClickEvent(ClickEvent.run_command, "/dungeons wayback new").setTip("Добавить точку выхода");
		newWayback.addLine(newWayback.createLine(nw));
		newWayback.addSeparator2();
		addInstallPage(newWayback);
		
		// 13
		TellRawText regions = new TellRawText();
		Element r1 = regions.createElement().setText("  Region {world} ({x1} {y1} {z1}) -> ({x2} {y2} {z2})  ");
		Element r2 = regions.createElement().setText("  ♲  ").setColor(Color.dark_red).setClickEvent(ClickEvent.run_command, "/dungeons region {regionNumber} remove").setTip("Удалить выделение");
		regions.addLine(regions.createLine(r1, r2));
		addInstallPage(regions);
		
		// 14
		TellRawText newRegion = new TellRawText();
		Element nr = newRegion.createElement().setText("  ✚  ").setColor(Color.dark_purple).setClickEvent(ClickEvent.run_command, "/dungeons region new").setTip("Добавить выделение");
		newRegion.addLine(newRegion.createLine(nr));
		newRegion.addSeparator2();
		addInstallPage(newRegion);
		
		// 15
		TellRawText coords = new TellRawText();
		coords.addSeparator2();
		Element cr1 = coords.createElement().setText("  [Click]  ").setColor(Color.light_purple).setClickEvent(ClickEvent.run_command, "/dungeons coordsClick {type}").setTip("Указать координаты кликом ПКМ");
		Element cr2 = coords.createElement().setText("  [Current]  ").setColor(Color.yellow).setClickEvent(ClickEvent.run_command, "/dungeons coordsCurrent {type}").setTip("Ваше текущее положение");
		coords.addLine(coords.createLine(cr1, cr2));
		coords.addSeparator2();
		addInstallPage(coords);
		
		// 16
		TellRawText regionConfirm = new TellRawText();
		regionConfirm.addSeparator2();
		Element rc1 = regionConfirm.createElement().setText("Found region §f{world}§7 (§f{x1} {y1} {z1}§7) -> (§f{x2} {y2} {z2}§7)  ");
		Element rc2 = regionConfirm.createElement().setText("  OK?  ").setColor(Color.red).setClickEvent(ClickEvent.run_command, "/dungeons region new {world} {x1} {y1} {z1} {x2} {y2} {z2}").setTip("Выбрать этот регион");
		regionConfirm.addLine(regionConfirm.createLine(rc1, rc2));
		coords.addSeparator2();
		addInstallPage(regionConfirm);
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		if (!(sender.hasPermission("gsm.dungeons.admin") || sender.isOp())) return false;
		if (GD.isConsoleSender(sender)) return false;
		
		GDSender p = GD.getGDSender(sender);
		
		if (args.length > 1) {
			if (args[0].equalsIgnoreCase("exitCommand")) {
				ThreadDaemon.async(() -> DungeonEngine.exitCommand(p, args));
				return true;
			}
		}
		
		if (args.length == 1) {
			if (args[0].equalsIgnoreCase("reload")) {
				ThreadDaemon.async(() -> DungeonEngine.reload());
				sender.sendMessage("Dungeons reloaded.");
				return true;
			} else if (args[0].equalsIgnoreCase("install")) {
				ThreadDaemon.async(() -> DungeonEngine.showAllDungeons(p));
				return true;
			} else if (args[0].equalsIgnoreCase("uninstall")) {
				ThreadDaemon.async(() -> DungeonEngine.uninstall(p));
				return true;
			} else if (args[0].equalsIgnoreCase("help")) {
				ThreadDaemon.async(() -> DungeonEngine.help(p));
				return true;
			} else if (args[0].equalsIgnoreCase("portal")) {
				ThreadDaemon.async(() -> DungeonEngine.portal(p));
				return true;
			} else if (args[0].equalsIgnoreCase("returnSpawn")) {
				ThreadDaemon.async(() -> DungeonEngine.returnSpawn(p));
				return true;
			} else if (args[0].equalsIgnoreCase("levels")) {
				ThreadDaemon.async(() -> DungeonEngine.levels(p));
				return true;
			} else if (args[0].equalsIgnoreCase("layers")) {
				ThreadDaemon.async(() -> DungeonEngine.layers(p));
				return true;
			} else if (args[0].equalsIgnoreCase("waybacks")) {
				ThreadDaemon.async(() -> DungeonEngine.waybacks(p));
				return true;
			} else if (args[0].equalsIgnoreCase("regions")) {
				ThreadDaemon.async(() -> DungeonEngine.regions(p));
				return true;
			} else if (args[0].equalsIgnoreCase("commands")) {
				ThreadDaemon.async(() -> DungeonEngine.commands(p));
				return true;
			} else if (args[0].equalsIgnoreCase("spawn")) {
				ThreadDaemon.async(() -> DungeonEngine.spawn(p));
				return true;
			} else if (args[0].equalsIgnoreCase("hologram")) {
				ThreadDaemon.async(() -> DungeonEngine.hologram(p));
				return true;
			} else if (args[0].equalsIgnoreCase("on")) {
				ThreadDaemon.async(() -> DungeonEngine.on(p));
				return true;
			} else if (args[0].equalsIgnoreCase("off")) {
				ThreadDaemon.async(() -> DungeonEngine.off(p));
				return true;
			}
		}
		
		if (args.length == 2) {
			if (args[0].equalsIgnoreCase("new")) {
				ThreadDaemon.async(() -> DungeonEngine.createDungeon(p, args[1]));
				return true;
			} else if (args[0].equalsIgnoreCase("install")) {
				ThreadDaemon.async(() -> DungeonEngine.install(p, args[1]));
				return true;
			} else if (args[0].equalsIgnoreCase("changename")) {
				ThreadDaemon.async(() -> DungeonEngine.changename(p, args[1]));
				return true;
			} else if (args[0].equalsIgnoreCase("remove")) {
				ThreadDaemon.async(() -> DungeonEngine.remove(p, args[1]));
				return true;
			} else if (args[0].equalsIgnoreCase("heightOffset")) {
				ThreadDaemon.async(() -> DungeonEngine.heightOffset(p, Integer.parseInt(args[1])));
				return true;
			} else if (args[0].equalsIgnoreCase("key")) {
				ThreadDaemon.async(() -> DungeonEngine.key(p, args[1]));
				return true;
			} else if (args[0].equalsIgnoreCase("reward")) {
				ThreadDaemon.async(() -> DungeonEngine.reward(p, args[1]));
				return true;
			} else if (args[0].equalsIgnoreCase("joinCooldown")) {
				ThreadDaemon.async(() -> DungeonEngine.joinCooldown(p, Integer.parseInt(args[1])));
				return true;
			} else if (args[0].equalsIgnoreCase("topSize")) {
				ThreadDaemon.async(() -> DungeonEngine.topSize(p, Integer.parseInt(args[1])));
				return true;
			} else if (args[0].equalsIgnoreCase("coordsClick")) {
				ThreadDaemon.async(() -> DungeonEngine.coordsClick(p, args[1]));
				return true;
			} else if (args[0].equalsIgnoreCase("coordsCurrent")) {
				ThreadDaemon.async(() -> DungeonEngine.coordsCurrent(p, args[1]));
				return true;
			} else if (args[1].equalsIgnoreCase("new")) {
				if (args[0].equalsIgnoreCase("wayback")) {
					ThreadDaemon.async(() -> DungeonEngine.addWayback(p));
					return true;
				} else if (args[0].equalsIgnoreCase("region")) {
					ThreadDaemon.async(() -> {
						try {
							DungeonEngine.addRegion(p);
						} catch (IncompleteRegionException e) { e.printStackTrace(); }
					});
					return true;
				}
			}
		}

		if (args.length == 3) {
			if (args[0].equalsIgnoreCase("level")) {
				if (args[1].equalsIgnoreCase("new")) {
					ThreadDaemon.async(() -> DungeonEngine.addLevel(p, Integer.parseInt(args[2])));
					return true;
				} else if (args[2].equalsIgnoreCase("on")) {
					ThreadDaemon.async(() -> DungeonEngine.levelOn(p, Integer.parseInt(args[1])));
					return true;
				} else if (args[2].equalsIgnoreCase("off")) {
					ThreadDaemon.async(() -> DungeonEngine.levelOff(p, Integer.parseInt(args[1])));
					return true;
				} else if (args[2].equalsIgnoreCase("remove")) {
					ThreadDaemon.async(() -> DungeonEngine.removeLevel(p, Integer.parseInt(args[1])));
					return true;
				}
			} else if (args[0].equalsIgnoreCase("layer")) {
				if (args[1].equalsIgnoreCase("new")) {
					ThreadDaemon.async(() -> DungeonEngine.addLayer(p, Integer.parseInt(args[2])));
					return true;
				} else if (args[2].equalsIgnoreCase("on")) {
					ThreadDaemon.async(() -> DungeonEngine.layerOn(p, Integer.parseInt(args[1])));
					return true;
				} else if (args[2].equalsIgnoreCase("off")) {
					ThreadDaemon.async(() -> DungeonEngine.layerOff(p, Integer.parseInt(args[1])));
					return true;
				} else if (args[2].equalsIgnoreCase("remove")) {
					ThreadDaemon.async(() -> DungeonEngine.removeLayer(p, Integer.parseInt(args[1])));
					return true;
				}
			} else if (args[0].equalsIgnoreCase("wayback")) {
				if (args[2].equalsIgnoreCase("remove")) {
					ThreadDaemon.async(() -> DungeonEngine.removeWayback(p, Integer.parseInt(args[1])));
				}
			} else if (args[0].equalsIgnoreCase("region")) {
				if (args[2].equalsIgnoreCase("remove")) {
					ThreadDaemon.async(() -> DungeonEngine.removeRegion(p, Integer.parseInt(args[1])));
				}
			}
		}
		
		if (args.length == 4) {
			if (args[0].equalsIgnoreCase("level")) {
				if (args[2].equalsIgnoreCase("time")) {
					ThreadDaemon.async(() -> DungeonEngine.setLevelTime(p, Integer.parseInt(args[1]), Integer.parseInt(args[3])));
					return true;
				}
			}
		}
		
		if (args.length == 9) {
			if (args[0].equalsIgnoreCase("region")) {
				if (args[1].equalsIgnoreCase("new")) {
					ThreadDaemon.async(() -> DungeonEngine.newRegion(p, args[2], args[3], args[4], args[5], args[6], args[7], args[8]));
					return true;
				}
			}
		}
		
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		
		if (!(sender.isOp() || sender.hasPermission("gsm.dungeons.admin"))) return null;
		
		if (args.length == 1) {
			return CompleteHelper.filter(args, "install", "new");
		}
		
		if (args.length == 2) {
			if (args[0].equalsIgnoreCase("new")) {
				return CompleteHelper.filter(args, String.valueOf(Dungeons.getAmount()+1));
			}
		}
		
		return null;
	}
	
}
