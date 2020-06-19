package ru.Baalberith.GameDaemon.CloudMail;

import java.text.SimpleDateFormat;

import ru.Baalberith.GameDaemon.GDSender;
import ru.Baalberith.GameDaemon.Extra.Installation.TellRawText;

public abstract class Latter {
	
	public enum LatterType {
		MESSAGE, PARCEL, BOX;
	}
	
	private SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
	
	protected LatterType type;
	protected String sender;
	protected String receiver;
	protected long createAt;
	protected TellRawText tellRawTextMarked = new TellRawText();
	protected TellRawText tellRawTextNoMarked = new TellRawText();
	protected boolean marked;
	
	protected String message;
	
	public Latter(LatterType type, String sender, String receiver, long createAt, boolean marked, String message) {
		this.type = type;
		this.sender = sender;
		this.receiver = receiver;
		this.createAt = createAt;
		this.marked = marked;
		this.message = message;
	}
	
	public abstract void load(); // Задаёт формат tellRaw сообщению.

	public void show(GDSender p, String... args) {
		if (marked) tellRawTextMarked.send(p, args);
		else tellRawTextNoMarked.send(p, args);
	}
	
	public boolean toggleMarked() {
		return (marked = marked ? false : true);
	}
	
	public String getCreateDate() {
		return sdf.format(createAt);
	}
	
}
