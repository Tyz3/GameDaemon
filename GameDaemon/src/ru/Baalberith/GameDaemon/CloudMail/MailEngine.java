package ru.Baalberith.GameDaemon.CloudMail;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import ru.Baalberith.GameDaemon.ConfigsDaemon;
import ru.Baalberith.GameDaemon.GD;
import ru.Baalberith.GameDaemon.GDPlayer;
import ru.Baalberith.GameDaemon.GDSender;
import ru.Baalberith.GameDaemon.SynStorage;
import ru.Baalberith.GameDaemon.CloudMail.Commands.MailCMD;
import ru.Baalberith.GameDaemon.CloudMail.Latters.BoxLatter;
import ru.Baalberith.GameDaemon.CloudMail.Latters.MessageLatter;
import ru.Baalberith.GameDaemon.CloudMail.Latters.ParcelLatter;
import ru.Baalberith.GameDaemon.Utils.ItemDaemon;
import ru.Baalberith.GameDaemon.CloudMail.Latter.LatterType;

public class MailEngine {
	
	public static SynStorage storage;
	
	public static Map<String, Mailbox> mailboxes = new ConcurrentHashMap<String, Mailbox>();
	
	public static MailManager mailManager = new MailManager();
	public static MailListener mailListener = new MailListener();
	public static MailCMD mailCMD = new MailCMD();
	
	public static int showNotificationEverySeconds;
	public static int saveDataEverySeconds;
	public static boolean compressData;
	
	public static Sound notificationSound;
	public static float notificationVolume;
	public static float notificationPitch;
	
	public static int clearOldMessagesEveryDays;
	public static int clearOldMailboxesEveryDays;
	
	public static void reload() {
		try {
			if (!mailboxes.isEmpty()) saveAllToDisk();
			mailboxes.clear();
			
			ConfigurationSection c = ConfigsDaemon.mainConfig.getConfigurationSection("cloudMail");
			ConfigurationSection m = ConfigsDaemon.messagesConfig.getConfigurationSection("cloudMail");
			
			Message.load(m, m.getString("label"));
			
			showNotificationEverySeconds = c.getInt("showNotificationEverySeconds", 60);
			saveDataEverySeconds = c.getInt("saveDataEverySeconds", 60);
			compressData = c.getBoolean("compressData", false);
			
			notificationSound = Sound.valueOf(c.getString("notification.sound", "ORB_PICKUP"));
			notificationVolume = (float) c.getDouble("notification.volume", 1.0);
			notificationPitch = (float) c.getDouble("notification.pitch", 1.0);
			
			clearOldMessagesEveryDays = c.getInt("clearOldMessagesEveryDays", 30);
			clearOldMailboxesEveryDays = c.getInt("clearOldMailboxesEveryDays", 30);
			
			storage = new SynStorage("mailboxData.json", GD.inst.getDataFolder().getPath(), compressData);
			
			
			Set<String> receivers = storage.keySet();
			for (String receiver : receivers) {
				long updateAt = storage.getLong(receiver.concat(".updateAt"), System.currentTimeMillis());
				
				// Не загружаем почтовые ящики, когда те устарели.
				if ((updateAt + (long) clearOldMailboxesEveryDays*24*60*60*1000) < System.currentTimeMillis()) continue;
				
				List<Latter> latters = loadExact(receiver);
				
				mailboxes.put(receiver, new Mailbox(receiver, latters, updateAt));
				GD.log("[CloudMail] Loaded "+latters.size()+" mails for player "+receiver);
			}
			
			GD.log("[CloudMail] Loaded "+mailboxes.size()+" mailboxes from file.");
			
			mailManager.reload();
			mailCMD.reload();
		} catch (Exception e) { e.printStackTrace(); }
	}
	
	public static List<Latter> loadExact(String receiverName) {
		List<Latter> latters = new ArrayList<Latter>();
		
		Set<String> content = storage.keySet(receiverName.concat(".content"));
		if (content == null) return latters;
		
		for (String cont : content) {
			String path = receiverName.concat(".content.").concat(cont);
			LatterType type = LatterType.valueOf(storage.getString(path.concat(".type"), "Пусто"));
			if (type == null) continue;
			
			String sender = storage.getString(path.concat(".sender"), "Пусто");
			String receiver = receiverName;
			String message = storage.getString(path.concat(".message"), "Пусто");
			
			
			long createAt = storage.getLong(path.concat(".createAt"), System.currentTimeMillis());
			boolean marked = storage.getBoolean(path.concat(".marked"), false);
			
			if ((createAt + (long) clearOldMessagesEveryDays*24*60*60*1000) < System.currentTimeMillis()) continue;
			
			Latter latter = null;
			switch (type) {
			case MESSAGE:
				latter = new MessageLatter(sender, receiver, createAt, marked, message);
				break;
			case PARCEL:
				ItemStack item = ItemDaemon.deSerializeItem(storage.getString(path.concat(".item"), null));
				if (item == null) continue;
				latter = new ParcelLatter(sender, receiver, createAt, marked, message, item);
				break;
			case BOX:
				List<ItemStack> items = new ArrayList<ItemStack>();
				List<String> itemsString = storage.getStringList(path.concat(".items"));
				itemsString.forEach(is -> items.add(ItemDaemon.deSerializeItem(is)));
				latter = new BoxLatter(sender, receiver, createAt, marked, message, items);
				break;
			default: continue;
			}
			
			latter.load();
			
			latters.add(latter);
		}
		
		return latters;
	}
	
	public static void saveAllToDisk() {
		storage.clear();
		for (Entry<String, Mailbox> entry : mailboxes.entrySet()) {
			Mailbox mb = entry.getValue();
			mb.saveToDisk();
		}
	}
	
	public static Mailbox getOrCreateMailbox(String name) {
		Mailbox mb = mailboxes.get(name);
		if (mb == null) {
			mb = new Mailbox(name, new ArrayList<Latter>(), System.currentTimeMillis());
			mailboxes.put(name, mb);
		}
		return mb;
	}
	
	// Команды
	
	public static void help(GDSender p) {
		Message.help_title.send(p);
		Message.help_default.send(p);
		if (p.hasPermission("gsm.mail.moder")) {
			Message.help_moder.send(p);
		}
		if (p.hasPermission("gsm.mail.admin")) {
			Message.help_admin.send(p);
		}
	}
	
	public static void showMailbox(GDSender p, String mailboxName) {
		Mailbox mb = getOrCreateMailbox(mailboxName);
		
		if (mb == null) {
			Message.mailboxNotExists.send(p);
			return;
		}
		
		// Проверяем владельца ящика.
		if (!p.getName().equals(mailboxName) && !p.hasPermission("gsm.mail.admin")) return;
		
		mb.showContent(p);
	}
	
	public static void toggleMarkedMessage(GDSender p, String mailboxName, String id) {
		Mailbox mb = getOrCreateMailbox(mailboxName);
		
		if (mb == null) {
			Message.mailboxNotExists.send(p);
			return;
		}
		
		// Проверяем владельца ящика.
		if (!p.getName().equals(mailboxName) && !p.hasPermission("gsm.mail.admin")) return;
		
		try {
			mb.toggleMarkLatter(Integer.parseInt(id));
		} catch (NumberFormatException e) {
			p.sendMessage("Чё кнопок не хватает? Указывай число, а набор символов.");
		}
		
		mb.showContent(p);
	}
	
	public static void removeSelectedLetter(GDSender p, String mailboxName, String id) {
		Mailbox mb = getOrCreateMailbox(mailboxName);
		
		if (mb == null) {
			Message.mailboxNotExists.send(p);
			return;
		}
		
		// Проверяем владельца ящика.
		if (!p.getName().equals(mailboxName) && !p.hasPermission("gsm.mail.admin")) return;
		
		try {
			mb.removeLatter(Integer.parseInt(id));
		} catch (NumberFormatException e) {
			p.sendMessage("Чё кнопок не хватает? Указывай число, а набор символов.");
		}
		
		mb.showContent(p);
	}
	
	public static void openSelectedLetter(GDPlayer p, String mailboxName, String id) {
		Mailbox mb = getOrCreateMailbox(mailboxName);
		
		if (mb == null) {
			Message.mailboxNotExists.send(p);
			return;
		}
		
		// Проверяем владельца ящика.
		if (!p.getName().equals(mailboxName) && !p.hasPermission("gsm.mail.admin")) return;
		
		try {
			mb.giveLatterContent(p, Integer.parseInt(id));
		} catch (NumberFormatException e) {
			p.sendMessage("Чё кнопок не хватает? Указывай число, а набор символов.");
		}

		mb.showContent(p);
	}
	
	
	public static void sendMessageLetter(GDSender sender, String receiver, String message) {
		sendMessageLetter(sender.getName(), receiver, message);
	}
	
	// Only plugins call.
	public static void sendMessageLetter(String sender, String receiver, String message) {
		Mailbox target = MailEngine.getOrCreateMailbox(receiver);
		
		MessageLatter latter = new MessageLatter(sender, receiver, message);
		latter.load();
		target.putLatter(latter);
	}
	
	public static void sendParcelLetter(GDPlayer sender, String receiver, String message) {
		ItemStack hand = sender.p.getItemInHand();
		if (hand == null) {
			Message.requiredItemInHand.send(sender);
			return;
		}
		ItemStack item = hand.clone();
		item.setAmount(1);
		sender.takeItem(hand);
		sendParcelLetter(sender.getName(), receiver, message, item);
	}
	
	// Only plugins call.
	public static void sendParcelLetter(String sender, String receiver, String message, ItemStack item) {
		Mailbox target = MailEngine.getOrCreateMailbox(receiver);
		
		ParcelLatter latter = new ParcelLatter(sender, receiver, message, item);
		latter.load();
		target.putLatter(latter);
	}
	
	// Only plugins call.
	public static void sendBoxLetter(String sender, String receiver, String message, List<ItemStack> items) {
		if (items.size() == 1) {
			sendParcelLetter(sender, receiver, message, items.get(0));
			return;
		}
		
		Mailbox target = MailEngine.getOrCreateMailbox(receiver);
		
		BoxLatter latter = new BoxLatter(sender, receiver, message, items);
		latter.load();
		target.putLatter(latter);
	}
	
	
}
