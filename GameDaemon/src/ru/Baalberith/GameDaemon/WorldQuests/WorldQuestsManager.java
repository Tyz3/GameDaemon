package ru.Baalberith.GameDaemon.WorldQuests;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import org.bukkit.scheduler.BukkitTask;

import ru.Baalberith.GameDaemon.GD;
import ru.Baalberith.GameDaemon.Storage;
import ru.Baalberith.GameDaemon.Utils.ThreadDaemon;
import ru.Baalberith.GameDaemon.Utils.Utils;
import ru.Baalberith.GameDaemon.WorldQuests.WorldQuest.WorldQuestType;
import ru.Baalberith.GameDaemon.WorldQuests.Quests.ArtisanQuest;
import ru.Baalberith.GameDaemon.WorldQuests.Quests.MercenaryQuest;
import ru.Baalberith.GameDaemon.WorldQuests.Quests.MinerQuest;
import ru.Baalberith.GameDaemon.WorldQuests.Quests.SupplierQuest;

public class WorldQuestsManager implements Runnable {
	
	private static Storage worldQuestStorage = new Storage("worldQuestsData.json", GD.inst.getDataFolder().getPath());
	
	private static Queue<Task> queue = new LinkedList<Task>();
	
	private static BukkitTask task;
	
	public static Task activeQuestTask;
	
	public static WorldQuestsListener worldQuestsListener = new WorldQuestsListener();
	
	public void reload() {
		queue.clear();
		
		// Сохранить текущие квесты в файл, если reload сработал во время работы сервера.
		if (activeQuestTask != null) {
			saveTaskIntoFile(activeQuestTask);
			activeQuestTask.completeAll();
			activeQuestTask = null;
		}
		
		// Восстановить в очереди незавершённые квесты до этого запуска.
		Task restoreTask = restoreTaskFromFile();
		if (restoreTask != null) queue.add(restoreTask);
		
		// Составление очереди из запланированных квестов.
		for (String sch : WorldQuestsEngine.scheduler) {
			String[] args = sch.split(":");
			if (args.length != 2) continue;
			
			int rollback = Integer.parseInt(args[1]);
			
			String[] quests = args[0].split(",");
			
			queue.add(new Task(rollback, getWorldQuests(quests)));
		}
		
		GD.log("[WorldQuests] Loaded "+queue.size()+" tasks from scheduler (list of quests).");
		
		restartRunTask();
	}
	
//	public void reloadExact(String sch) {
//		// TODO
//	}
	
	public static void saveTaskIntoFile(Task task) {
		worldQuestStorage.set("task.rollback", task.getRollback());
		worldQuestStorage.set("task.rollbackExpiresMillis", task.getRollbackExpiresMillis());
		for (WorldQuest wq : task.getActiveQuests()) {
			String path = wq.type.name().concat("_").concat(wq.id);
			worldQuestStorage.set(path.concat(".expires"), wq.expiresMillis);
			
			
			WorldQuestType type = wq.type;
			switch (type) {
			case artisan:
				ArtisanQuest aq = (ArtisanQuest) wq;
				worldQuestStorage.set(path.concat(".top"), aq.getScores());
				break;
			case mercenary:
				MercenaryQuest meq = (MercenaryQuest) wq;
				worldQuestStorage.set(path.concat(".top"), meq.getScores());
				break;
			case miner:
				MinerQuest mq = (MinerQuest) wq;
				worldQuestStorage.set(path.concat(".top"), mq.getScores());
				break;
			case supplier:
				SupplierQuest sq = (SupplierQuest) wq;
				worldQuestStorage.set(path.concat(".nowStatus"), sq.nowStatus);
				break;
			default: continue;
			}
		}
		worldQuestStorage.save();
	}
	
	private Task restoreTaskFromFile() {
		List<WorldQuest> restoredQuestList = new ArrayList<WorldQuest>();
		
		int rollback = -1;
		long rollbackExpiresMillis = -1;
		Set<String> keys = worldQuestStorage.keySet();
		for (String k : keys) {
			if (rollback == -1 && k.equals("task")) {
				rollback = worldQuestStorage.getInt("task.rollback", 0);
				rollbackExpiresMillis = worldQuestStorage.getLong("task.rollbackExpiresMillis", 0);
				continue;
			}
			
			WorldQuest wq = parseWorldQuest(k);
			if (wq == null) continue;
			
			// Достать из файла всю инфу, которая меняется у квеста в работе.
			wq.expiresMillis = worldQuestStorage.getLong(k+".expires", 0);
			switch (wq.type) {
			case supplier:
				SupplierQuest sq = (SupplierQuest) wq;
				sq.nowStatus = worldQuestStorage.getInt(k+".nowStatus", 0);
				break;
			case miner:
				MinerQuest mq = (MinerQuest) wq;
				mq.setScores(Utils.sortByValue(worldQuestStorage.getLongHashMap(k+".top"), true));
				break;
			case artisan:
				ArtisanQuest aq = (ArtisanQuest) wq;
				aq.setScores(Utils.sortByValue(worldQuestStorage.getLongHashMap(k+".top"), true));
				break;
			case mercenary:
				MercenaryQuest meq = (MercenaryQuest) wq;
				meq.setScores(Utils.sortByValue(worldQuestStorage.getLongHashMap(k+".top"), true));
				break;
			default: continue;
			}
			
			restoredQuestList.add(wq);
		}
		
		worldQuestStorage.clear();
		return rollback == -1 ? null : new Task(rollback, restoredQuestList, rollbackExpiresMillis);
	}
	
	private List<WorldQuest> getWorldQuests(String[] quests) { 
		List<WorldQuest> questList = new ArrayList<WorldQuest>();
		
		for (String q : quests) {
			WorldQuest wq = parseWorldQuest(q);
			if (wq == null) continue;
			
			questList.add(wq);
		}
		return questList;
	}
	
	private WorldQuest parseWorldQuest(String quest) {
		String[] args = quest.split("_");
		if (args.length != 2) return null;
		// args -   0:{type}   1:{id}
		
		WorldQuestType type = WorldQuestType.valueOf(args[0]);
		if (type == null) return null;
		
		// Получить из общего списка нужный квест и добавить его в questList.
		// Рабочие квесты уже лежат в Engine
		WorldQuest wq = WorldQuestsEngine.getWorldQuest(type, args[1]);
		if (wq == null) return null;
		
		return wq;
	}
	
	public void restartRunTask() {
		ThreadDaemon.cancelTask(task);
		task = ThreadDaemon.asyncTimer(this, 0, WorldQuestsEngine.updateTime*20);
	}
	
	// Мозг всех квестов - контролирует запуск, смену и обновление статуса квестов.
	@Override
	public void run() {
		// Устанавливаем первые из очереди мировые квесты.
		if (activeQuestTask == null) {
			if (queue.isEmpty()) return;
			activeQuestTask = queue.poll(); // Забираем группу квестов из очереди.
			
			// Удаляем верхнюю запись из планировщика, если activeTask не восстановленный.
			if (!activeQuestTask.isRestored())
				WorldQuestsEngine.removeEntryFromScheduler();
		}
		
		activeQuestTask.run();
		
		// Удаляем активную групп квестов.
		// Сработает, когда все квесты уже удалены и откат прошёл.
		if (activeQuestTask.isEnded()) {
			activeQuestTask = null;
		}
		
	}
	
	public static boolean hasActiveQuest() {
		return activeQuestTask != null;
	}
	
}
