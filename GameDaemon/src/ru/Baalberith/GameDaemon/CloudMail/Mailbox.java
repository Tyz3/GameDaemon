package ru.Baalberith.GameDaemon.CloudMail;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.inventory.ItemStack;

import ru.Baalberith.GameDaemon.GD;
import ru.Baalberith.GameDaemon.GDPlayer;
import ru.Baalberith.GameDaemon.GDSender;
import ru.Baalberith.GameDaemon.CloudMail.Latters.BoxLatter;
import ru.Baalberith.GameDaemon.CloudMail.Latters.ParcelLatter;
import ru.Baalberith.GameDaemon.Utils.ItemDaemon;

public class Mailbox {

	private String owner;
	private long updateAt;
	private List<Latter> latters = new ArrayList<Latter>();
	
	public Mailbox(String owner, List<Latter> latters, long updateAt) {
		this.owner = owner;
		this.latters = latters;
		this.updateAt = updateAt;
	}
	
	public Mailbox setUpdateAt() {
		updateAt = System.currentTimeMillis();
		return this;
	}
	
	public Mailbox putLatter(Latter latter) {
		latters.add(latter);
		return this;
	}
	
	public void removeLatter(int id) {
		if (id >= latters.size()) return;
		latters.remove(id);
	}
	
	public void toggleMarkLatter(int id) {
		if (id >= latters.size()) return;
		latters.get(id).toggleMarked();
	}
	
	public boolean hasNoMarkedLatter() {
		for (Latter latter : latters) {
			if (!latter.marked) return true;
		}
		return false;
	}
	
	public int size() {
		return latters.size();
	}
	
	public boolean equals(String name) {
		return name.equals(owner);
	}
	
	public void saveToDisk() {
		MailEngine.storage.set(owner, null);
		MailEngine.storage.set(owner.concat(".updateAt"), updateAt);
		
		for (int i = 0; i < latters.size(); i++) {
			Latter l = latters.get(i);
			String path = owner.concat(".content.").concat(String.valueOf(i));
			
			MailEngine.storage.set(path.concat(".sender"), l.sender);
			MailEngine.storage.set(path.concat(".type"), l.type.name());
			MailEngine.storage.set(path.concat(".createAt"), l.createAt);
			MailEngine.storage.set(path.concat(".marked"), l.marked);
			MailEngine.storage.set(path.concat(".message"), l.message);
			
			switch (l.type) {
			case MESSAGE:
				// Нет доп данных.
				break;
			case PARCEL:
				ParcelLatter pl = (ParcelLatter) l;
				MailEngine.storage.set(path.concat(".item"), ItemDaemon.serializeItem(pl.getItem(), false));
				break;
			case BOX:
				BoxLatter bl = (BoxLatter) l;
				List<ItemStack> items = bl.getItems();
				List<String> itemsString = new ArrayList<String>();
				items.forEach(item -> itemsString.add(ItemDaemon.serializeItem(item, false)));
				MailEngine.storage.set(path.concat(".items"), itemsString);
				break;
			default: continue;
			}
		}
		
		MailEngine.storage.save();
	}
	
	public void showContent(GDSender p) {
		if (size() == 0) {
			Message.emptyMailbox.send(p);
			return;
		}
		
		Message.mailboxTitle.send(p);
		for (int i = 0; i < latters.size(); i++) {
			Latter l = latters.get(i);
			l.show(p, "{id}", String.valueOf(i));
		}
	}
	
	public void giveLatterContent(GDPlayer p, int id) {
		if (id >= latters.size()) return;
		Latter l = latters.get(id);
		
		switch (l.type) {
		case PARCEL:
			ParcelLatter pl = (ParcelLatter) l;
			p.giveItem(pl.getItem());
			removeLatter(id);
			break;
		case BOX:
			BoxLatter bl = (BoxLatter) l;
			p.giveItems(bl.getItems());
			removeLatter(id);
			break;
		default: break;
		}
	}
	
	public void sendNotificationToOwner() {
		if (!hasNoMarkedLatter()) return;
		GDPlayer p = GD.getGDPlayer(owner);
		
		if (p == null || !p.isOnline()) return;
		
		Message.notification.sendRandom(p);
		p.playSound(MailEngine.notificationSound, MailEngine.notificationVolume, MailEngine.notificationPitch);
	}
}
