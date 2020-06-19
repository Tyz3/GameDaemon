package ru.Baalberith.GameDaemon.CloudMail;

import java.util.List;

import org.bukkit.inventory.ItemStack;

public class MailboxAPI {
	
	public static void sendMessageLetter(String sender, String receiver, String message) {
		MailEngine.sendMessageLetter(sender, receiver, message);
	}
	
	public static void sendParcelLetter(String sender, String receiver, String message, ItemStack item) {
		MailEngine.sendParcelLetter(sender, receiver, message, item);
	}
	
	public static void sendBoxLetter(String sender, String receiver, String message, List<ItemStack> items) {
		MailEngine.sendBoxLetter(sender, receiver, message, items);
	}
}
