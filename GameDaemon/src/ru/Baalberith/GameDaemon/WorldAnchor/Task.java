package ru.Baalberith.GameDaemon.WorldAnchor;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitTask;

import ru.Baalberith.GameDaemon.GD;
import ru.Baalberith.GameDaemon.GDPlayer;
import ru.Baalberith.GameDaemon.GDWorld;
import ru.Baalberith.GameDaemon.Utils.ThreadDaemon;
import ru.Baalberith.GameDaemon.Utils.Utils;

import org.bukkit.Chunk;

public class Task implements Runnable {
	
	private Dolmen dolmen;
	private List<GDPlayer> participants = new ArrayList<GDPlayer>();;
	
	private BukkitTask bukkitTask;
	private int stageId = 0;
	private int phaseId = 0;
	private boolean running = true;

	private int warmup = DolmenEngine.waitingWarmup;
	private int countdown = DolmenEngine.countdownToSummon;
	private int duration;
	private boolean firstPhaseStart = true;
	
	public Task(Dolmen dolmen) {
		this.dolmen = dolmen;
		this.duration = dolmen.getFirstPhase().maxDuration;
		this.bukkitTask = ThreadDaemon.asyncTimer(this, 0, 20);
	}
	
	public void stop() {
		ThreadDaemon.cancelTask(bukkitTask);
	}
	
	@Override
	public void run() {
		Phase phase = dolmen.getPhaseById(phaseId);
		if (phase == null) {
			if (running) {
				List<String> parts = new ArrayList<String>();
				participants.stream().forEach(p -> parts.add(p.getName()));
				GD.sendMessageForAll(Message.close.replace("{title}", dolmen.getTitle()).replace("{participants}", Utils.rawArray(parts)).get());
			}
			running = false;
		}
		if (!running) return;
		
		
		// Стадия сбора игроков.
		if (stageId == 0) {
			if (warmup <= 0) {
				GD.sendMessageForAll(Message.cancel.replace("{title}", dolmen.getTitle()).get());
				running = false;
			}
			if (DolmenEngine.inst.waitingPattern.contains(warmup))
				DolmenEngine.broadcast(dolmen, warmup);
			if (hasPlayers()) {
				// TODO Добавить сообщение о первом запуске призыва + звук WITHER_SPAWN.
				stageId = 1;
			}
			warmup--;
		}
		
		// Стадия запуска обратного отсчёта до призыва мобов.
		if (stageId == 1) {
			// Если это первая фаза
			if (firstPhaseStart) {
				GD.sendMessageForAll(Message.activated.replace("{title}", dolmen.getTitle()).get());
				firstPhaseStart = false;
			}
			
			if (DolmenEngine.inst.warmupPattern.contains(countdown))
				GDWorld.sendMessageToNearest2D(Message.announce.replace("{waveNumber}", String.valueOf(phaseId+1)).replace("{S}", String.valueOf(countdown)).get(),
						dolmen.getCenter(), DolmenEngine.dolmenRadius);
			
			if (countdown <= 0) {
				if (phase.hasBoss)
					GDWorld.sendMessageToNearest2D(Message.summonBoss.replace("{title}", dolmen.getTitle()).replace("{waveNumber}", String.valueOf(phaseId+1)).get(), 
							dolmen.getCenter(), DolmenEngine.dolmenRadius);
				else
					GDWorld.sendMessageToNearest2D(Message.starting.replace("{title}", dolmen.getTitle()).replace("{waveNumber}", String.valueOf(phaseId+1)).get(), 
							dolmen.getCenter(), DolmenEngine.dolmenRadius);
				
				// Если это последняя стадия, то фиксируем участников.
				if (phaseId == dolmen.getPhasesAmount()-1)
					participants = GDWorld.getNearestPlayers2D(dolmen.getCenter(), DolmenEngine.dolmenRadius);
				
				phase.runCommands();
				lightning();
				stageId = 2;
			}
			countdown--;
		}
		
		// Стадия проверки чанков на наличие призванных мобов.
		if (stageId == 2) {
			
			// Если duration = 0, то время на закрытие этой фазы закончилось и якорь закрывается.
			if (duration <= 0) {
				running = false;
				GD.sendMessageForAll(Message.timeout.replace("{title}", dolmen.getTitle()).get());
			} else {
				// Иначе идёт напомниание об оставшемся времени.
				if (DolmenEngine.inst.durationPattern.contains(duration))
					GDWorld.sendMessageToNearest2D(Message.duration.replace("{title}", dolmen.getTitle()).replace("{S}", String.valueOf(duration)).get(),
							dolmen.getCenter(), DolmenEngine.dolmenRadius);
			}
			
			int i = 0;
			List<Chunk> chunks = GDWorld.getChunks(dolmen.getCenter(), 1);
			for (Chunk c : chunks) {
				if (GDWorld.getEntitiesInChunkFilter1(c).size() > 0) break;
				i++;
				if (i == chunks.size()) {
					phaseId++;
					warmup = DolmenEngine.waitingWarmup;
					countdown = DolmenEngine.countdownToSummon;
					duration = phase.maxDuration;
					stageId = 1;
					// Если это последняя стадия, то выдаём награду участникам.
					if (phaseId == dolmen.getPhasesAmount()) {
						for (GDPlayer np : GDWorld.getNearestPlayers2D(dolmen.getCenter(), DolmenEngine.dolmenRadius)) {
							if (!participants.contains(np)) participants.remove(np);
						}
						if (phase.giveRewards(participants))
							participants.forEach(p -> Message.reward.send(p));
					}
				}
			}
			duration--;
		}
		
	}
	
	public boolean isRunning() {
		return running;
	}
	
	private void lightning() {
		ThreadDaemon.sync(() -> {
			for (Chunk c : GDWorld.getChunks(dolmen.getCenter(), DolmenEngine.dolmenRadius)) {
				List<Entity> entities = GDWorld.getEntitiesInChunkFilter1(c);
				entities.forEach(e -> c.getWorld().strikeLightningEffect(e.getLocation()));
			}
		});
	}
	
	private boolean hasPlayers() {
		return GDWorld.amountOfNearestPlayers2D(dolmen.getCenter(), DolmenEngine.dolmenRadius) >= DolmenEngine.minPlayersToStart ? true : false;
	}
	
	public boolean equals(Dolmen dolmen) {
		return this.dolmen.getTitle().equalsIgnoreCase(dolmen.getTitle()) ? true : false;
	}
	
}
