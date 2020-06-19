package ru.Baalberith.GameDaemon.CloudMail.Latters;

import org.bukkit.inventory.ItemStack;

import ru.Baalberith.GameDaemon.CloudMail.Latter;
import ru.Baalberith.GameDaemon.Extra.Installation.TellRawText.ClickEvent;
import ru.Baalberith.GameDaemon.Extra.Installation.TellRawText.Color;
import ru.Baalberith.GameDaemon.Extra.Installation.TellRawText.Element;

public class ParcelLatter extends Latter {
	
	private ItemStack item;
	
	public ParcelLatter(String sender, String receiver, long createAt, boolean marked, String message, ItemStack item) {
		super(LatterType.PARCEL, sender, receiver, createAt, marked, message);
		this.item = item;
	}
	
	public ParcelLatter(String sender, String receiver, String message, ItemStack item) {
		this(sender, receiver, System.currentTimeMillis(), false, message, item);
	}
	
	@Override
	public void load() {
		tellRawTextMarked.clear();
		tellRawTextNoMarked.clear();
		// ✔ ✕  ☑ ☐
		Element eSender = tellRawTextMarked.createElement().setText("[".concat(sender).concat("]")).setColor(Color.gold).setTip("Написать в лс (/msg)?").setClickEvent(ClickEvent.suggest_command, "/msg ".concat(sender).concat(" "));
		Element eSep1 = tellRawTextMarked.createElement().setText(" -> ");
		Element eMsg = tellRawTextMarked.createElement().setText(message).setColor(Color.aqua).setTip("Получено: ".concat(getCreateDate()));
		Element eSep2 = tellRawTextMarked.createElement().setText(" ");
		
		Element eItemOpen = tellRawTextMarked.createElement().setText("[Забрать]").setColor(Color.light_purple).setClickEvent(ClickEvent.run_command, "/mail open ".concat(receiver).concat(" {id}")).setTip(item, true);
		
		Element eMarked = tellRawTextMarked.createElement().setText(" ✔  ").setColor(Color.green).setClickEvent(ClickEvent.run_command, "/mail marked ".concat(receiver).concat(" {id}")).setTip("Снять прочтение");
		Element eNoMarked = tellRawTextMarked.createElement().setText(" ✕  ").setColor(Color.yellow).setClickEvent(ClickEvent.run_command, "/mail marked ".concat(receiver).concat(" {id}")).setTip("Отметить прочтение");
		
		Element eDelete = tellRawTextMarked.createElement().setText(" ♲ ").setColor(Color.dark_red).setClickEvent(ClickEvent.run_command, "/mail remove ".concat(receiver).concat(" {id}")).setTip("Удалить письмо");
		
		tellRawTextMarked.addLine(tellRawTextMarked.createLine(eDelete, eMarked, eSender, eSep2, eItemOpen, eSep1, eMsg));
		tellRawTextNoMarked.addLine(tellRawTextNoMarked.createLine(eDelete, eNoMarked, eSender, eSep2, eItemOpen, eSep1, eMsg));
	}
	
	public ItemStack getItem() {
		return item;
	}
}
