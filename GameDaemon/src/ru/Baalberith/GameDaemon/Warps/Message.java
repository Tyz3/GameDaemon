package ru.Baalberith.GameDaemon.Warps;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;

import ru.Baalberith.GameDaemon.ConfigsDaemon;
import ru.Baalberith.GameDaemon.GDSender;

@SuppressWarnings("unchecked")
public enum Message {
	
	common_notExists, common_public, common_private, 
	common_notOwner, common_hidden, common_shown,
	
	warp_help_title, warp_help_default, warp_help_admin,
	warp_info_title, warp_info_default, warp_info_owner, warp_info_admin,
	warp_whitelist_alreadyIn, warp_whitelist_add, warp_whitelist_remove, warp_whitelist_removeFail,
	warp_blacklist_alreadyIn, warp_blacklist_add, warp_blacklist_remove, warp_blacklist_removeFail,
	warp_withdrawMoney, warp_fee_notEnough, warp_fee_description, warp_fee_set, warp_fee_setThreshold, warp_fee_get, warp_fee_getThreshold, warp_fee_take,
	warp_accessMode, warp_hideMode, warp_tpFailure, warp_descriptionSet, warp_movehere, warp_inBlacklist,
	warp_notInWhitelist, warp_blacklistLimit, warp_whitelistLimit,
	warp_list_title, warp_list_all, warp_list_your, warp_list_invited, warp_list_nothing, warp_list_hidden,
	
	warps_title, warps_raw, warps_invited, warps_nothing,
	
	setwarp_disallowedWorld, setwarp_successfully, setwarp_alreadyExists, setwarp_nameFail, setwarp_lengthFail,
	setwarp_publicLimit, setwarp_privateLimit, setwarp_totalLimit,
	
	delwarp_successfully, delwarp_bankNotEmpty,
	
	holographic_title, holographic_visitors, holographic_balance;
	
	private static final int PLACEHOLDERS_LIMIT = 7;
	
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


	public void send(CommandSender sender) {
		new Sender().send(sender);
	}

	public void send(GDSender p) {
		new Sender().send(p);
	}

	public Sender replace(String from, int to) {
		return replace(from, String.valueOf(to));
	}

	public Sender replace(String from, double to) {
		return replace(from, String.valueOf(to));
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
		
		public Sender replace(String from, int to) {
			return replace(from, String.valueOf(to));
		}
		
		public Sender replace(String from, double to) {
			return replace(from, String.valueOf(to));
		}
		
		public void send(CommandSender sender) {
			if(sender == null) return;
			if (Message.this.message == null) return;

			for (String m : Message.this.message) {
				if(m.isEmpty()) continue;
				String s = placeholders(m);
				sender.sendMessage(s);
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
			
			if (placePosition != 0 && cache != null && cache.length % 2 == 0 && cache.length > 1) {
				for (int i = 0; i < cache.length; i+=2) {
					if (cache[i] == null) break;
					input = input.replace(cache[i], cache[i+1]);
				}
			}
			
			return input;
		}
		
		public String get() {
			return placeholders(Message.this.message[0]);
		}
		
		public String[] gets() {
			String[] msgs = new String[Message.this.message.length];
			for (int i = 0; i < Message.this.message.length; i++) {
				msgs[i] = placeholders(Message.this.message[i]);
			}
			return msgs;
		}
	}
	
	public String get() {
		return Message.this.message[0];
	}
	
	public String[] gets() {
		return Message.this.message;
	}
}
