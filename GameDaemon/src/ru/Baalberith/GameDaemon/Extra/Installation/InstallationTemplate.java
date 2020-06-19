package ru.Baalberith.GameDaemon.Extra.Installation;

import org.bukkit.command.CommandExecutor;

import ru.Baalberith.GameDaemon.GD;

public abstract class InstallationTemplate implements CommandExecutor {
	
	private static TellRawText[] pages;
	private static int currentPage = 0;
	private static boolean inited = false;
	
	public abstract void reload();
	
	public InstallationTemplate(String cmdName) {
		GD.inst.getCommand(cmdName).setExecutor(this);
	}
	
	protected static void initialize(int pagesAmount) {
		pages = new TellRawText[pagesAmount];
		currentPage = 0;
		inited = true;
	}
	
	public static TellRawText getInstallPage(int number) {
		return inited ? pages[number] : null;
	}
	
	public void addInstallPage(TellRawText installPage) {
		if (!inited) return;
		pages[currentPage] = installPage;
		currentPage++;
	}
}
