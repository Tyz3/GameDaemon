package ru.Baalberith.GameDaemon.CloudMail;

import java.util.Map.Entry;

import org.bukkit.scheduler.BukkitTask;

import ru.Baalberith.GameDaemon.Utils.ThreadDaemon;

public class MailManager {
	
	private BukkitTask notificationTask;
	private BukkitTask saveTask;
	
	public void reload() {
		ThreadDaemon.cancelTask(notificationTask);
		ThreadDaemon.cancelTask(saveTask);
		notificationTask = ThreadDaemon.asyncTimer(() -> showNotification(), 0, 20*MailEngine.showNotificationEverySeconds);
		saveTask = ThreadDaemon.asyncTimer(() -> saveData(), 0, 20*MailEngine.saveDataEverySeconds);
	}
	
	public void showNotification() {
		for (Entry<String, Mailbox> e : MailEngine.mailboxes.entrySet()) {
			e.getValue().sendNotificationToOwner();
		}
	}
	
	public void saveData() {
		MailEngine.mailboxes.entrySet().forEach(e -> e.getValue().saveToDisk());
	}
}
