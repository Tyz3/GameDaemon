package ru.Baalberith.GameDaemon.Clans;

import ru.Baalberith.GameDaemon.Clans.Groups.Party.PartyEngine;

public class ClansEngine {
	
	public static ClansEngine inst;
	
	private PartyEngine partyEngine = new PartyEngine();
	
	public ClansEngine() {
		inst = this;
	}
	
	public void reload() {
		
		partyEngine.reload();
	}
	
	
	
	
}
