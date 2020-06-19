package ru.Baalberith.GameDaemon.CloudMail;

import ru.Baalberith.GameDaemon.GDPlayer;
import ru.Baalberith.GameDaemon.Utils.ThreadDaemon;

public class MailListener {
	
	public static void onJoin(GDPlayer p) {
		ThreadDaemon.async(() -> {
			MailEngine.getOrCreateMailbox(p.getName()).setUpdateAt().sendNotificationToOwner();
		});
	}
	
	public static void onQuit(GDPlayer p) {
		ThreadDaemon.async(() -> {
			MailEngine.getOrCreateMailbox(p.getName()).saveToDisk();
		});
	}
}
