package ru.Baalberith.GameDaemon.CloudMail.Latters;

import java.util.List;

import org.bukkit.inventory.ItemStack;

import ru.Baalberith.GameDaemon.CloudMail.Latter;
import ru.Baalberith.GameDaemon.Extra.Installation.TellRawText.ClickEvent;
import ru.Baalberith.GameDaemon.Extra.Installation.TellRawText.Color;
import ru.Baalberith.GameDaemon.Extra.Installation.TellRawText.Element;

public class BoxLatter extends Latter {

	private List<ItemStack> items;
	
	public BoxLatter(String sender, String receiver, long createAt, boolean marked, String message, List<ItemStack> items) {
		super(LatterType.BOX, sender, receiver, createAt, marked, message);
		this.items = items;
	}
	
	public BoxLatter(String sender, String receiver, String message, List<ItemStack> items) {
		this(sender, receiver, System.currentTimeMillis(), false, message, items);
	}
	
	@Override
	public void load() {
		tellRawTextMarked.clear();
		tellRawTextNoMarked.clear();
		// ✔ ✕  ☑ ☐
		Element eSender = tellRawTextMarked.createElement().setText(sender).setColor(Color.gold).setTip("Написать в лс (/msg)?").setClickEvent(ClickEvent.suggest_command, "/msg ".concat(sender).concat(" "));
		Element eSep1 = tellRawTextMarked.createElement().setText(" -> ");
		Element eMsg = tellRawTextMarked.createElement().setText(message).setColor(Color.aqua).setTip("Получено: ".concat(getCreateDate()));
		Element eSep2 = tellRawTextMarked.createElement().setText(" ");
		
		Element eItemsOpen = tellRawTextMarked.createElement().setText("[Забрать вещи]").setColor(Color.light_purple).setClickEvent(ClickEvent.run_command, "/mail open ".concat(receiver).concat(" {id}")).setTip("Содержит ".concat(String.valueOf(items.size())).concat(" предмет(a/ов)\\n \\nЕсли у вас нет места, то некоторые\\nвещи выпадут рядом."));
		
		Element eMarked = tellRawTextMarked.createElement().setText(" ✔  ").setColor(Color.green).setClickEvent(ClickEvent.run_command, "/mail marked ".concat(receiver).concat(" {id}")).setTip("Снять прочтение");
		Element eNoMarked = tellRawTextMarked.createElement().setText(" ✕  ").setColor(Color.yellow).setClickEvent(ClickEvent.run_command, "/mail marked ".concat(receiver).concat(" {id}")).setTip("Отметить прочтение");
		
		Element eDelete = tellRawTextMarked.createElement().setText(" ♲ ").setColor(Color.dark_red).setClickEvent(ClickEvent.run_command, "/mail remove ".concat(receiver).concat(" {id}")).setTip("Удалить письмо");
		
		tellRawTextMarked.addLine(tellRawTextMarked.createLine(eDelete, eMarked, eSender, eSep2, eItemsOpen, eSep1, eMsg));
		tellRawTextNoMarked.addLine(tellRawTextNoMarked.createLine(eDelete, eNoMarked, eSender, eSep2, eItemsOpen, eSep1, eMsg));
	}
	
	public List<ItemStack> getItems() {
		return items;
	}

}
