package ru.Baalberith.GameDaemon.Clans.Dungeons;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.Baalberith.GameDaemon.GD;

public class Level {
	
	private boolean enabled = false;
	private HashMap<Integer, List<String>> commands = new HashMap<Integer, List<String>>();
	private int time = 0;
	private int levelNumber = 0;
	
	private static Pattern yPattern = Pattern.compile("\\{start:[0-9]+\\}"); 
	
	public Level(boolean enabled, List<String> commands, int levelNumber, int time, int layersAmount, int heightOffset) {
		this.enabled = enabled;
		this.levelNumber = levelNumber;
		this.time = time;
		
		// string: 1-3: /command param1
		for (String string : commands) {
			String[] args = string.split(":", 2);
			// [1-3] [ /command param1]
			//   0            1
			
			if (args.length != 2) return;
			
			if (args[0].contains("-")) {
				String[] range = args[0].trim().split("-");
				// [1] [3]
				//  0   1
				
				int min = Integer.parseInt(range[0]);
				int max = Integer.parseInt(range[1]);
				
				if (levelNumber < min || levelNumber > max) continue;
				
				addCommandToLayers(args[1], heightOffset, layersAmount);
			} else if (args[0].contains(",")) {
				String[] range = args[0].trim().split(",");
				for (String numStr : range) {
					int num = Integer.parseInt(numStr);
					
					if (levelNumber != num) continue;
					
					addCommandToLayers(args[1], heightOffset, layersAmount);
				}
			} else {
				int num = Integer.parseInt(args[0].trim());
				
				if (levelNumber != num) continue;

				addCommandToLayers(args[1], heightOffset, layersAmount);
				
			}
		}
	}
	
	private void addCommandToLayers(String arg, int heightOffset, int layersAmount) {
		for (int i = 1; i <= layersAmount; i++) {
			List<String> list = this.commands.get(i);
			if (list == null) list = new ArrayList<String>();
			
			String cmd = parseHeightOffset(arg, heightOffset, i);
			
			list.add(cmd);
			this.commands.put(i, list);
		}
	}
	
	private String parseHeightOffset(String cmd, int heightOffset, int layerNumber) {
		cmd = cmd.trim().replace("{lvl}", ""+levelNumber).replaceFirst("/", "");
		
		Matcher m = yPattern.matcher(cmd);
		
		while (m.find()) {
			String reg = cmd.substring(m.start(), m.end());
			// {start:x}
			int startY = Integer.parseInt(reg.split(":")[1].replace("}", ""));
			// x
			cmd = cmd.replace(reg, String.valueOf(startY + (layerNumber-1)*heightOffset) );
		}
		return cmd;
	}
	
	public void setTime(int time) {
		this.time = time;
	}
	
	public void runCommands(int layerNumber) {
		if (this.commands == null || this.commands.isEmpty()) return;
		for (String cmd : this.commands.get(layerNumber)) {
			GD.dispatchCommand(cmd);
		}
	}
	
	public int getTime() {
		return time;
	}
	
	public boolean on() {
		if (time == 0 || levelNumber == 0) return false;
		return (enabled = true);
	}
	
	public void off() {
		enabled = false;
	}
	
	public boolean isEnabled() {
		return enabled;
	}
	
	public int number() {
		return levelNumber;
	}
}
