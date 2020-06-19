package ru.Baalberith.GameDaemon.Statistics;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Statistic;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import ru.Baalberith.GameDaemon.ConfigsDaemon;
import ru.Baalberith.GameDaemon.GD;
import ru.Baalberith.GameDaemon.GDPlayer;
import ru.Baalberith.GameDaemon.Clans.Dungeons.DungeonListener;
import ru.Baalberith.GameDaemon.Clans.Groups.Party.PartyListener;
import ru.Baalberith.GameDaemon.CloudMail.MailListener;
import ru.Baalberith.GameDaemon.MuteDaemon.MuteHandler;
import ru.Baalberith.GameDaemon.Utils.LocationManager;
import ru.Baalberith.GameDaemon.Utils.ThreadDaemon;

public class StatEngine implements Listener {
    
	
	public StatEngine() {
		Bukkit.getPluginManager().registerEvents(this, GD.inst);
	}
	
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
    	GDPlayer p = GD.addGDPlayer(e.getPlayer());
    	join(p);
    	
    	DungeonListener.onJoin(p);
    	PartyListener.onJoin(p);
    	MailListener.onJoin(p);
    	
    	joinSpawn(p); // Телепортация игрока на новую локацию после входа на сервер.
    }
    
    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e) {
    	GDPlayer p = GD.getGDPlayer(e.getPlayer());
    	quit(p);
    	
    	DungeonListener.onQuit(p);
    	PartyListener.onQuit(p);
    	MailListener.onQuit(p);
    	
    	GD.removeGDPlayer(p);
    	ThreadDaemon.async(() -> p.saveData());
    }
    
    private void join(GDPlayer p) {
    	if (!p.p.hasPlayedBefore()) {
    		p.setPeaceMode(true);
    		p.createUUID();
    		p.setDateFirstJoin(System.currentTimeMillis());
    		p.addLoyaltyRatio(100);
    		p.setPvpRating(0);
    		p.setRpgSpawn(ConfigsDaemon.defaultRpgSpawn);
//        	Newbies.inst.addNewbie(p.getName());
    	} else {
//    		boolean peaceMode = p.hasPeaceMode();
//    		if (peaceMode) {
//        		long now = p.p.getStatistic(Statistic.PLAY_ONE_TICK) * 50;
//        		if (Newbies.EXPIRE_TIME > now)
//        			Newbies.inst.addNewbie(p.getName());
//        		else p.setPeaceMode(false);
//    		}
    	}
    	p.addJoinDate(System.currentTimeMillis());
    	p.addJoinIp(p.p.getAddress().getAddress().toString());
    	p.addJoinsAmount();
    	
    	//
    	if (p.checkMute()) MuteHandler.addPlayerToMute(p.getName());
    }
    
    private void quit(GDPlayer p) {
    	p.addQuitDate(System.currentTimeMillis());
    	p.addQuitIp(p.p.getAddress().getAddress().toString());
    	p.setTimePlayingSpent(p.p.getStatistic(Statistic.PLAY_ONE_TICK) * 50);
    }
    
    private void joinSpawn(GDPlayer p) {
    	Location loc = LocationManager.deserializeLocation(p.getJoinSpawn());
    	if (loc == null) return;
    	p.teleport(loc);
    	p.setJoinSpawn(null);
    }
}

