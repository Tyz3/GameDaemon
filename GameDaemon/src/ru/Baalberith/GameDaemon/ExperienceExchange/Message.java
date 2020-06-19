package ru.Baalberith.GameDaemon.ExperienceExchange;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import ru.Baalberith.GameDaemon.GDSender;

@SuppressWarnings("unchecked")
public enum Message {
	
	notEnoughExp, notEnoughBottles, success;

	private static final int PLACEHOLDERS_LIMIT = 10;

	private String[] message;
	
	public static void load(FileConfiguration c) {
		for (Message m : values()) {
			m.message = null;
			Object obj = c.get(m.name().replace("_", "."));
			if (obj instanceof List<?>) {
				List<String> list = (new ArrayList<String>()).getClass().cast(obj);
				m.message = new String[list.size()];
				for (int i = 0; i < list.size(); i++) {
					m.message[i] = list.get(i).replace("&", "§");
				}
			} else if (obj instanceof String) {
				m.message = new String[] {obj.toString().replace("&", "§")};
			}
		}
	}
	
	public static void load(ConfigurationSection c) {
		for (Message m : values()) {
			m.message = null;
			Object obj = c.get(m.name().replace("_", "."));
			if (obj instanceof List<?>) {
				List<String> list = (new ArrayList<String>()).getClass().cast(obj);
				m.message = new String[list.size()];
				for (int i = 0; i < list.size(); i++) {
					m.message[i] = list.get(i).replace("&", "§");
				}
			} else if (obj instanceof String) {
				m.message = new String[] {obj.toString().replace("&", "§")};
			}
		}
	}


	public void send(CommandSender sender) {
		new Sender().send(sender);
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
		
		public void send(CommandSender player) {
			if(player == null) return;
			if (Message.this.message == null) return;

			for (String m : Message.this.message) {
				if(m.isEmpty()) continue;
				String s = placeholders(m);
				player.sendMessage(s);
			}
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

		private String placeholders(String input) {
			if (!input.contains("%") && !input.contains("{")) {
				return input;
			}
			
			if (cache != null && cache.length % 2 == 0 && cache.length > 1) {
				for (int i = 0; i < cache.length; i+=2) {
					input = input.replace(cache[i], cache[i+1]);
				}
			}
			
			return input;
		}
	}
}
