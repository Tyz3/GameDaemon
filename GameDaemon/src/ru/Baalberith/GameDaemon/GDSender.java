package ru.Baalberith.GameDaemon;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import ru.Baalberith.GameDaemon.Utils.MathOperation;

public class GDSender extends GDData {

	protected CommandSender sender;
	protected boolean consoleSender = true;
	
	public GDSender() {
		sender = Bukkit.getServer().getConsoleSender();
	}
	
	public GDSender(String fileName, String folder) {
		super(fileName, folder);
	}
	
	public void sendTellRawMessage(String msg) {
		GD.dispatchCommand("tellraw ".concat(sender.getName()).concat(" ").concat(msg));
		// ((CraftPlayer) sender).getHandle().playerConnection.sendPacket(new PacketPlayOutChat(ChatSerializer.a(msg)));
	}
	
	public void sendMessage(String msg) {
		if (!consoleSender) {
			if (msg.startsWith("json: ")) {
				msg = msg.substring(5).trim();
				sendTellRawMessage(msg);
			} else sender.sendMessage(msg);
		} else sender.sendMessage(msg);
	}
	
	public void sendMessage(String msg, String... args) {
		if (args != null && args.length % 2 == 0 && args.length > 1) {
			for (int i = 0; i < args.length; i+=2) {
				if (!(msg.contains("%") || msg.contains("{") || msg.contains("&"))) continue;
				msg = msg.replace(args[i], args[i+1]);
			}
		}
		sendMessage(msg);
	}
	
	public void sendMessages(String... msgs) {
		for (int i = 0; i < msgs.length; i++) {
			sendMessage(msgs[i]);
		}
	}
	
	public void sendMessageToNearest(String msg, int radius) {
		if (!consoleSender && sender instanceof GDPlayer) {
			GDPlayer p = (GDPlayer) sender;
			GD.online.forEach(o -> {
				if (MathOperation.distance3D(p.getLocation(), o.getLocation()) <= radius)
					o.sendMessage(msg);
			});
		} else sender.sendMessage(msg);
	}
	
	public void sendMessages(List<String> msgs, String... args) {
		msgs.forEach(msg -> sendMessage(msg, args));
	}
	
	public boolean isOp() {
		return sender.isOp();
	}
	
	public boolean hasPermission(String perm) {
		return sender.hasPermission(perm);
	}
	
	public String getName() {
		return sender.getName();
	}
}
