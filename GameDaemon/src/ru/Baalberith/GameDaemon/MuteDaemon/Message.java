package ru.Baalberith.GameDaemon.MuteDaemon;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.configuration.ConfigurationSection;

import ru.Baalberith.GameDaemon.ConfigsDaemon;
import ru.Baalberith.GameDaemon.GDSender;

@SuppressWarnings("unchecked")
public enum Message {
	
	help_title, help_helper, help_moder, help_reason,
	
	info_error, info_title, info_history,
	
	broadcast, unmute, alert, checkMute, missingMute, alreadyMuted, muteCleared,
	incorrectArgs, description
	
	;

	private static final int PLACEHOLDERS_LIMIT = 5;

	private String[] message;
	
	public static void load(ConfigurationSection c, String label) {
		for (Message m : values()) {
			m.message = null;
			Object obj = c.get(m.name().replace("_", "."));
			if (obj instanceof List<?>) {
				List<String> list = (new ArrayList<String>()).getClass().cast(obj);
				m.message = new String[list.size()];
				for (int i = 0; i < list.size(); i++) {
					m.message[i] = list.get(i).replace("{label}", ConfigsDaemon.label).replace("{label}", label).replace("&", "§");
				}
			} else if (obj instanceof String) {
				m.message = new String[] {obj.toString().replace("{label}", ConfigsDaemon.label).replace("{label}", label).replace("&", "§")};
			}
		}
	}

	public void send(GDSender p) {
		new Sender().send(p);
	}
	
	// Первая замена выполняется здесь, последующие в классе Sender
	public Sender replace(String from, String to) {
		Sender s = new Sender();
		s.replace(from, to);
		return s;
	}
	
	public class Sender {
		private String[] cache = new String[PLACEHOLDERS_LIMIT*2];
		private int placePosition = 0;
		
		public Sender replace(String from, String to) {
			cache[placePosition] = from;
			cache[placePosition+1] = to;
			placePosition += 2;
			return this;
		}

		public void send(GDSender p) {
			if (p == null) return;
			if (Message.this.message == null) return;
			
			for (int i = 0; i < Message.this.message.length; i++) {
				if (placePosition == 0)
					p.sendMessage(Message.this.message[i]);
				else
					p.sendMessage(Message.this.message[i], cache);
			}
		}
	}
	
	public String get() {
		return Message.this.message[0];
	}
	
	public String[] gets() {
		return Message.this.message;
	}
}
