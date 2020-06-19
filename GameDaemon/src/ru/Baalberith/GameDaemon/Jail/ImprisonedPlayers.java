package ru.Baalberith.GameDaemon.Jail;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

import ru.Baalberith.GameDaemon.ConfigsDaemon;
import ru.Baalberith.GameDaemon.GD;
import ru.Baalberith.GameDaemon.Jail.Commands.JailCMD;
import ru.Baalberith.GameDaemon.Jail.Commands.TojailCMD;
import ru.Baalberith.GameDaemon.Jail.Commands.UnjailCMD;
import ru.Baalberith.GameDaemon.Utils.LocationManager;

public class ImprisonedPlayers {
	
	public List<ImprisonedPlayer> imprisonedPlayers;
	private ConfigurationSection i;
	private ConfigurationSection c;
	private ConfigurationSection m;
	
	public JailEngine engine;
	public static ImprisonedPlayers inst;
	
	public static String funcPrefix;
	
	private TojailCMD tojail = new TojailCMD();
	private UnjailCMD unjail = new UnjailCMD();
	private JailCMD jail = new JailCMD();
	
	
	public ImprisonedPlayers() {
		inst = this;
		imprisonedPlayers = new ArrayList<ImprisonedPlayer>();
		engine = new JailEngine();

		GD.inst.getCommand("tojail").setExecutor(tojail);
		GD.inst.getCommand("unjail").setExecutor(unjail);
		GD.inst.getCommand("jail").setExecutor(jail);
	}
	
	
	public void reload() {
		try {
			if (!imprisonedPlayers.isEmpty()) imprisonedPlayers.clear();
			engine.reload();
	
			m = ConfigsDaemon.messagesConfig.getConfigurationSection("violations.jail");
			c = ConfigsDaemon.mainConfig.getConfigurationSection("violations.jail");
			i = ConfigsDaemon.jailConfig.getConfigurationSection("players");
			funcPrefix = ConfigsDaemon.label.replace("{label}", m.getString("label"));
			
			if (i != null) {
				Set<String> players = i.getKeys(false);
				if (!players.isEmpty()) {
					for (String p : players) {
						String name = p;
						short penalty = (short) i.getInt(p+".penalty", 0);
						short questId = (short) i.getInt(p+".questId", 0);
						String reason = i.getString(p+".reason", "no reason");
						String punisher = i.getString(p+".punisher", "unknown");
						Location location = LocationManager.deserializeLocation(i.getString(p+".location", "world 0 0 0"));
						long imprisonedDate = i.getLong(p+".imprisonedDate", 0);
						
						imprisonedPlayers.add(new ImprisonedPlayer(name, penalty, questId, reason, punisher, location, c.getString("permissionsEx-group", "imprisoned"), imprisonedDate));
					}
				}
			}
			
			tojail.reload();
			unjail.reload();
			jail.reload();
			GD.log("[TRPGJail] Loaded "+imprisonedPlayers.size()+" imprisoned players.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public ImprisonedPlayer getImprisonedPlayer(String name) {
		
		for (ImprisonedPlayer ip : imprisonedPlayers) {
			if (ip.getName().equalsIgnoreCase(name)) return ip;
		}
		return null;
	}
	
}
