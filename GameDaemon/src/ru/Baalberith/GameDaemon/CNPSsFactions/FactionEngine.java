package ru.Baalberith.GameDaemon.CNPSsFactions;

import ru.Baalberith.GameDaemon.ConfigsDaemon;
import ru.Baalberith.GameDaemon.GD;
import ru.Baalberith.GameDaemon.GDPlayer;
import ru.Baalberith.GameDaemon.Utils.ThreadDaemon;

public class FactionEngine {

	private String cmd;
	private String message;
	
	public FactionEngine() {
		cmd = ConfigsDaemon.mainConfig.get().getString("faction-cmd");
		message = ConfigsDaemon.messagesConfig.get().getString("other.inspire.add");
	}
	
	public void activate(GDPlayer p, FactionItem item) {
		ThreadDaemon.async(() -> {
			if (p.takeItem(item.getItem())) exec(p, item);
		});
	}
	
	public void activateShift(GDPlayer p, FactionItem item, int amount) {
		ThreadDaemon.async(() -> {
			if (p.takeItem(item.getItem(), amount)) execShift(p, item, amount);
		});
	}
	
	private void exec(GDPlayer p, FactionItem item) {
		String cmd = this.cmd;
		cmd = cmd.replace("[player]", p.getName()).replace("[factionId]", ""+item.getFactionId()).replace("[increaseMode]", item.getIncreaseMode()).replace("[points]", ""+item.getPoints());
		GD.dispatchCommand(cmd);
		p.sendMessage(message.replace("[points]", ""+item.getPoints()).replace("&", "\u00a7"));
		p.addReceivedFactionPoints(item.getPoints());
	}
	
	private void execShift(GDPlayer p, FactionItem item, int amount) {
		String cmd = this.cmd;
		cmd = cmd.replace("[player]", p.getName())
				.replace("[factionId]", ""+item.getFactionId())
				.replace("[increaseMode]", item.getIncreaseMode())
				.replace("[points]", ""+item.getPoints()*amount);
		GD.dispatchCommand(cmd);
		p.sendMessage(message.replace("[points]", ""+item.getPoints()*amount).replace("&", "\u00a7"));
		p.addReceivedFactionPoints(item.getPoints());
	}
}
