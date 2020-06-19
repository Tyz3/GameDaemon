package ru.Baalberith.GameDaemon.Jail;

import org.bukkit.Location;

import ru.Baalberith.GameDaemon.GD;
import ru.Baalberith.GameDaemon.GDPlayer;
import ru.Baalberith.GameDaemon.Utils.LocationManager;

public class ImprisonedPlayer {
	
	private String name;
	private short penalty;
	private short questId;
	private String reason;
	private String punisher;
	private Location location;
	private String permissionGroup;
	private long imprisonedDate;
	
	public ImprisonedPlayer(String name, short penalty, short questId, String reason, String punisher, Location location, String permissionGroup, long imprisonedDate) {
		this.name = name;
		this.penalty = penalty;
		this.questId = questId;
		this.reason = reason;
		this.punisher = punisher;
		this.location = location;
		this.permissionGroup = permissionGroup;
		this.imprisonedDate = imprisonedDate;
	}

	public String getName() {
		return name;
	}

	public short getPenalty() {
		return penalty;
	}

	public short getQuestId() {
		return questId;
	}

	public String getReason() {
		return reason;
	}

	public String getPunisher() {
		return punisher;
	}

	public Location getLocation() {
		return location;
	}
	
	public String getStringLocation() {
		return LocationManager.serializeLocation(location);
	}
	
	public GDPlayer getPlayer() {
		return GD.getGDPlayer(name);
	}

	public String getPermissionGroup() {
		return permissionGroup;
	}

	public long getImprisonedDate() {
		return imprisonedDate;
	}
}
