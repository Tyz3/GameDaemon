package ru.Baalberith.GameDaemon.Clans.Dungeons.Game;

import java.util.concurrent.CopyOnWriteArrayList;

import org.bukkit.scheduler.BukkitTask;

import ru.Baalberith.GameDaemon.Clans.Dungeons.Dungeon;
import ru.Baalberith.GameDaemon.Clans.Dungeons.DungeonEngine;
import ru.Baalberith.GameDaemon.Clans.Dungeons.Message;
import ru.Baalberith.GameDaemon.Utils.ThreadDaemon;

public class Sessions implements Runnable {
	
	private BukkitTask task;
	public static CopyOnWriteArrayList<Session> sessions = new CopyOnWriteArrayList<Session>();
	
	public void reload() {
		ThreadDaemon.cancelTask(task);
		task = ThreadDaemon.asyncTimer(this, 20, 20);
		closeAll();
	}
	
	@Override
	public void run() {
		for (int i = 0; i < sessions.size(); i++) {
			Session s = sessions.get(i);
			if(s.isEnded()) {
				if (s.layer != null) {
					s.layer.setBusy(false);
					s.dungeon.updateHologram();
				}
				s.close();
				sessions.remove(i);
				s = null;
				System.out.println("Sessions close, size "+sessions.size());
				continue;
			}
			if(!s.isFull()) {
				if (s.getCreateTime() + DungeonEngine.sessionTimeout*1000 < System.currentTimeMillis()) {
					s.ended = true;
					s.returnKeys();
					s.sendMessageToSession(Message.sessionTimedOut.replace("{name}", s.dungeon.getName()).replace("{size}", s.level.number()).get());
				}
				continue;
			}
			s.run();
		}
	}
	
	public static void addSession(Session session) {
		sessions.add(session);
	}
	
	public static void closeAll() {
		sessions.stream().forEach(s -> s.close());
		sessions.clear();
	}
	
	public static Session getSessionWithPlayer(String name) {
		for (Session s : sessions) {
			if (s.containsPlayer(name)) return s;
		}
		return null;
	}
	
	public static void closeSessionsWithDungeon(Dungeon dung) {
		for (Session s : sessions) {
			if (s.getDungeon() == dung) {
				s.sendMessageToSession("Данж был перезагружен/выключен администрацией для технических работ.");
				if (s.full) {
					dung.giveReward(s.getMembers(), s.getLevel().number());
					s.sendMessageToSession("Награда выдана в качестве компенсации.");
				}
				s.ended = true;
			}
		}
	}
}
