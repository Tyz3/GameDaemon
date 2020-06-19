package ru.Baalberith.GameDaemon.Extra.Installation;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import ru.Baalberith.GameDaemon.GD;
import ru.Baalberith.GameDaemon.GDSender;
import ru.Baalberith.GameDaemon.Extra.Installation.TellRawText.ClickEvent;
import ru.Baalberith.GameDaemon.Extra.Installation.TellRawText.Color;
import ru.Baalberith.GameDaemon.Extra.Installation.TellRawText.Element;
import ru.Baalberith.GameDaemon.Extra.Installation.TellRawText.Style;

public class InstallationTest extends InstallationTemplate {

	public InstallationTest() {
		super("install");
	}
	
	public void reload() {
		initialize(2);
		
		TellRawText page1 = new TellRawText();
		page1.addSeparator1();
		Element e1 = page1.createElement().setText("GameMode change {status}").setClickEvent(ClickEvent.run_command, "/install test").setColor(Color.gold).setTip("§bClick to run a command!");
		page1.addLine(page1.createLine(e1));
		addInstallPage(page1);
		
		TellRawText page2 = new TellRawText();
		page2.addSeparator2();
		page2.addBlankLine();
		Element e2 = page2.createElement().setText("Text 1   ").setColor(Color.aqua);
		Element e3 = page2.createElement().setTip("Это ховер текст").setText("Наведи сюда курсором").setStyle(Style.obfuscated);
		page2.addLine(page2.createLine(e2, e3));
		page2.addSeparator2();
		addInstallPage(page2);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		if (GD.isConsoleSender(sender)) return false;
		GDSender s = GD.getGDSender(sender);
		
		if (args.length == 0) {
			getInstallPage(0).send(s, "{status}", sender.isOp()?"OP":"noOP");
			return true;
		}
		
		if (args[0].equalsIgnoreCase("test")) {
			getInstallPage(0).send(s, "{status}", sender.isOp()?"OP":"noOP");
			getInstallPage(1).send(s);
			return true;
		}
		
		return true;
	}

}
