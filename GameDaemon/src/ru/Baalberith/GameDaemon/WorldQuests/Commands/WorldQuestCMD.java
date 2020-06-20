package ru.Baalberith.GameDaemon.WorldQuests.Commands;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import ru.Baalberith.GameDaemon.Extra.Installation.InstallationTemplate;
import ru.Baalberith.GameDaemon.Extra.Installation.TellRawText;
import ru.Baalberith.GameDaemon.Extra.Installation.TellRawText.ClickEvent;
import ru.Baalberith.GameDaemon.Extra.Installation.TellRawText.Color;
import ru.Baalberith.GameDaemon.Extra.Installation.TellRawText.Element;
import ru.Baalberith.GameDaemon.Utils.ThreadDaemon;
import ru.Baalberith.GameDaemon.WorldQuests.WorldQuestsEngine;
import ru.Baalberith.GameDaemon.WorldQuests.WorldQuestsListener;

public class WorldQuestCMD extends InstallationTemplate implements TabCompleter {

	public WorldQuestCMD() {
		super("worldquests");
	}

	@Override
	public void reload() {
		
		// 0
		TellRawText mainHeaderON = new TellRawText();
		mainHeaderON.addSeparator1();
		Element m11 = mainHeaderON.createElement().setText("  Тип: &a{type}  ");
		Element m12 = mainHeaderON.createElement().setText("  Ид: &a{id}  ");
		Element m13 = mainHeaderON.createElement().setText("  Название: &a{name}  ");
		Element m14 = mainHeaderON.createElement().setText("  ✎  ").setColor(Color.yellow).setClickEvent(ClickEvent.suggest_command, "/worldquests changename {name}").setTip("Изменить название");
		Element m16 = mainHeaderON.createElement().setText("  ♲  ").setColor(Color.dark_red).setClickEvent(ClickEvent.suggest_command, "/worldquests remove {id}").setTip("Нажмите, чтобы удалить квест");
		Element m17 = mainHeaderON.createElement().setText("  ☒  ").setColor(Color.aqua).setClickEvent(ClickEvent.run_command, "/worldquests uninstall").setTip("Закончить настройку квеста");
		mainHeaderON.addLine(mainHeaderON.createLine(m11, m12, m13, m14, m16, m17));
		addInstallPage(mainHeaderON);
		
		// 1
		TellRawText mainBody = new TellRawText();
		Element mb1 = mainBody.createElement().setText("  Duration  ").setColor(Color.green).setClickEvent(ClickEvent.suggest_command, "/worldquests duration {duration}").setTip("Длительность: {duration} c.");
		Element mb2 = mainBody.createElement().setText("  Hologram  ").setColor(Color.green).setClickEvent(ClickEvent.run_command, "/worldquests hologram").setTip("{location}");
		Element mb3 = mainBody.createElement().setText("  Description...  ").setColor(Color.green).setClickEvent(ClickEvent.run_command, "/worldquests description");
		Element mb4 = mainBody.createElement().setText("  Rewards...  ").setColor(Color.green).setClickEvent(ClickEvent.run_command, "/worldquests rewards");
		Element mb5 = mainBody.createElement().setText("  Reward type").setColor(Color.red).setTip("&cComing soon...");
		mainBody.addLine(mainBody.createLine(mb1, mb2, mb3, mb4, mb5));
		mainBody.addSeparator2();
		addInstallPage(mainBody);
		
		// Description
		
		// 2
		TellRawText desc = new TellRawText();
		Element d1 = desc.createElement().setText("  Строка: ");
		Element d2 = desc.createElement().setText("&f'&7{description}&f'  ").setColor(Color.gray).setClickEvent(ClickEvent.suggest_command, "/worldquests description set {index} {description}").setTip("Нажми, чтобы изменить эту строку");
		Element d3 = desc.createElement().setText("  ♲  ").setColor(Color.dark_red).setClickEvent(ClickEvent.run_command, "/worldquest description remove {index}");
		desc.addLine(desc.createLine(d1, d2, d3));
		addInstallPage(desc);
		
		// 3
		TellRawText newDesc = new TellRawText();
		Element nd1 = newDesc.createElement().setText("  ✚  ").setColor(Color.dark_purple).setClickEvent(ClickEvent.suggest_command, "/worldquests description new %desc%").setTip("Добавить строку");
		newDesc.addLine(newDesc.createLine(nd1));
		newDesc.addSeparator2();
		addInstallPage(newDesc);
		
		// Areas
		
		// 4
		TellRawText areas = new TellRawText();
		Element a1 = areas.createElement().setText("  Центр: &7{center}  ").setClickEvent(ClickEvent.run_command, "/worldquest areas {id} center").setTip("Установить центр области");
		Element a2 = areas.createElement().setText("  Длина: &7{length}  ").setClickEvent(ClickEvent.run_command, "/worldquest areas {id} length {length}").setTip("Установить длину по X");
		Element a3 = areas.createElement().setText("  Высота: &7{height}  ").setClickEvent(ClickEvent.run_command, "/worldquest areas {id} height {height}").setTip("Установить высоту по Y");
		Element a4 = areas.createElement().setText("  Ширина: &7{width}  ").setClickEvent(ClickEvent.run_command, "/worldquest areas {id} width {width}").setTip("Установить ширину по Z");
		Element a5 = areas.createElement().setText("  ♲  ").setColor(Color.dark_red).setClickEvent(ClickEvent.run_command, "/worldquests areas {id} remove").setTip("Удалить область");
		areas.addLine(areas.createLine(a1, a2, a3, a4, a5));
		addInstallPage(areas);
		
		// 5
		TellRawText newArea = new TellRawText();
		Element na1 = newArea.createElement().setText("  ✚  ").setColor(Color.dark_purple).setClickEvent(ClickEvent.suggest_command, "/worldquests areas new").setTip("Добавить область с центром,\\nгде вы стоите");
		newArea.addLine(newArea.createLine(na1));
		newArea.addSeparator2();
		addInstallPage(newArea);
		
		
		// Blocks
		
		// 6
		TellRawText blocks = new TellRawText();
		Element b1 = blocks.createElement().setText("  Блок: '{block}'  ");
		Element b2 = blocks.createElement().setText("  ♲  ").setColor(Color.dark_red).setClickEvent(ClickEvent.run_command, "/worldquests blocks {id} remove");
		blocks.addLine(blocks.createLine(b1, b2));
		addInstallPage(blocks);
		
		// 7
		TellRawText newBlock = new TellRawText();
		Element nb1 = newBlock.createElement().setText("  ✚  ").setColor(Color.dark_purple).setClickEvent(ClickEvent.suggest_command, "/worldquests blocks new").setTip("Добавить блок, который у\\nвас в руке");
		newBlock.addLine(newBlock.createLine(nb1));
		newBlock.addSeparator2();
		addInstallPage(newBlock);
		
		// Cargo items
		
		// 8
		TellRawText cargoItems = new TellRawText();
		Element ci1 = cargoItems.createElement().setText("  Предмет: '{item}'  ");
		Element ci2 = cargoItems.createElement().setText("  ♲  ").setColor(Color.dark_red).setClickEvent(ClickEvent.run_command, "/worldquests cargoitems {id} remove");
		cargoItems.addLine(cargoItems.createLine(ci1, ci2));
		addInstallPage(cargoItems);
		
		// 9
		TellRawText newCargoItem = new TellRawText();
		Element nci1 = newCargoItem.createElement().setText("  ✚  ").setColor(Color.dark_purple).setClickEvent(ClickEvent.suggest_command, "/worldquests cargoitems new").setTip("Добавить предмет, который у\\nвас в руке");
		newCargoItem.addLine(newCargoItem.createLine(nci1));
		addInstallPage(newCargoItem);
		
		// Craft Filters
		TellRawText filters = new TellRawText();
		
		// Scores
		TellRawText scores = new TellRawText();
		
		// sdsdsd
		
		// Coords
		TellRawText coords = new TellRawText();
		coords.addSeparator2();
		Element cr1 = coords.createElement().setText("  [Click]  ").setColor(Color.light_purple).setClickEvent(ClickEvent.run_command, "/worldquests coordsClick {type}").setTip("Указать координаты кликом ПКМ");
		Element cr2 = coords.createElement().setText("  [Current]  ").setColor(Color.yellow).setClickEvent(ClickEvent.run_command, "/worldquests coordsCurrent {type}").setTip("Ваше текущее положение");
		coords.addLine(coords.createLine(cr1, cr2));
		coords.addSeparator2();
		addInstallPage(coords);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		if (!(sender.isOp() || sender.hasPermission("gsm.worldquests.admin"))) return false;
		
		if (args.length == 1) {
			if (args[0].equalsIgnoreCase("reload")) {
				ThreadDaemon.async(() -> WorldQuestsEngine.reload());
				return true;
			}
		}
		
		// worldquest artisan {player} smith 1
		
		if (args.length == 4) {
			if (args[0].equalsIgnoreCase("artisan")) {
				WorldQuestsListener.onCraftItem(args[1], args[2], Integer.parseInt(args[3]));
			}
		}
		
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		
		return null;
	}

}
