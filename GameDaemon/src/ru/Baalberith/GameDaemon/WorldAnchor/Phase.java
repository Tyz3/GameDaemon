package ru.Baalberith.GameDaemon.WorldAnchor;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import ru.Baalberith.GameDaemon.GD;
import ru.Baalberith.GameDaemon.GDPlayer;

public class Phase {
	
	public String name;
	public List<String> commands;
	public int maxDuration;
	public boolean hasBoss;
	public List<ItemStack> rewards;
	
	public Phase(String name, List<String> commands, int maxDuration, boolean hasBoss, List<ItemStack> rewards, Location center) {
		this.name = name;
		this.commands = replaceRegexes(commands, center);
		this.maxDuration = maxDuration;
		this.hasBoss = hasBoss;
		this.rewards = rewards;
	}
	
	// Расчёт относительных координат от центра якоря.
	private List<String> replaceRegexes(List<String> commands, Location center) {
		Pattern px = Pattern.compile("\\{x[-+]*[0-9]+[0-9]*\\}"); // {x+122}
		Pattern py = Pattern.compile("\\{y[-+]*[0-9]+[0-9]*\\}");
		Pattern pz = Pattern.compile("\\{z[-+]*[0-9]+[0-9]*\\}");
		List<String> newCommands = new ArrayList<String>();
		for (String cmd : commands) {
			Matcher mx = px.matcher(cmd);
			Matcher my = py.matcher(cmd);
			Matcher mz = pz.matcher(cmd);
			if (mx.find()) {
				String foundx = mx.group();
				double x = center.getX() + Double.parseDouble(foundx.replace("{x", "").replace("}", ""));
				cmd = cmd.replace(foundx, ""+x);
			}
			if (my.find()) {
				String foundy = my.group();
				double y = center.getY() + Double.parseDouble(foundy.replace("{y", "").replace("}", ""));
				cmd = cmd.replace(foundy, ""+y);
			}
			if (mz.find()) {
				String foundz = mz.group();
				double z = center.getZ() + Double.parseDouble(foundz.replace("{z", "").replace("}", ""));
				cmd = cmd.replace(foundz, ""+z);
			}
			newCommands.add(cmd);
		}
		return newCommands;
	}
	
	public void runCommands() {
		commands.stream().forEach(cmd -> GD.dispatchCommand(cmd));
	}
	
	public boolean giveRewards(List<GDPlayer> players) {
		if (rewards.isEmpty()) return false;
		players.stream().forEach(p -> p.giveItems(rewards));
		return true;
	}
	
}
