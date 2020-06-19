package ru.Baalberith.GameDaemon.WorldQuests;

import java.util.ArrayList;
import java.util.List;

public class Task implements Runnable {
	
	private List<WorldQuest> activeQuests = new ArrayList<WorldQuest>();
	private int rollback;
	private long rollbackExpiresMillis = 0;
	
	// Variable data for task work.
	private boolean restored = false;
	private boolean questsInited = false;
	private List<WorldQuest> deactivatedQuests = new ArrayList<WorldQuest>();
	
	public Task(int rollback, List<WorldQuest> quests) {
		this.rollback = rollback;
		quests.forEach(q -> activeQuests.add(q));
	}
	
	// Восстанавливает время отката задания. Используется при восстановлении из файла.
	public Task(int rollback, List<WorldQuest> quests, long rollbackExpiresMillis) {
		this(rollback, quests);
		this.rollbackExpiresMillis = rollbackExpiresMillis;
		this.restored = true;
	}
	
	public boolean isRestored() {
		return restored;
	}
	
	// Перевод квестов в стадию готовности.
	private void initQuests() {
		activeQuests.forEach(activeQuest -> activeQuest.startUsing());
		activeQuests.forEach(aq -> aq.createHologram());
		questsInited = true;
	}
	
	// Начинаем обратный отсчёт до запуска следующей группы мировых квестов.
	private void startRollback() {
		rollbackExpiresMillis = System.currentTimeMillis() + rollback*1000;
	}
	
	private boolean isRollbackStarted() {
		return rollbackExpiresMillis != 0;
	}
	
	private boolean hasActiveQuests() {
		return !activeQuests.isEmpty();
	}
	
	@Override
	public void run() {
		if (isRollbackStarted()) return;
		
		if (questsInited) {
			// Закончилось ли время действия квестов?
			for (WorldQuest quest : activeQuests) {
				if (quest.isExpired()) {
					
					// Удаляем голограмму.
					quest.deleteHologram();
					
					// Выдаём награду участникам за квест.
					quest.giveRewardsViaMail();
					
					// Обнуляем данные квеста.
					quest.restoreDefaultState();
					
					deactivatedQuests.add(quest);
				}
			}
			
			// Удаляем закончавшиеся квесты.
			if (!deactivatedQuests.isEmpty()) {
				deactivatedQuests.forEach(quest -> activeQuests.remove(quest));
			}
		} else initQuests();
		
		
		// Если ли ещё есть активные квесты?
		if (hasActiveQuests()) {
			// Обновляем голограммы со статусом квестов
			activeQuests.forEach(activeQuest -> activeQuest.updateHologram());
			
		} else startRollback();
	}
	
	// Вызывается внешне, нужен для определения: удалить этот таск или он ещё работает.
	public boolean isEnded() {
		return isRollbackStarted() && rollbackExpiresMillis < System.currentTimeMillis();
	}
	
	// Принудительное завершение всех активных квестов.
	public void completeAll() {
		for (WorldQuest quest : activeQuests) {
			quest.deleteHologram();
			quest.restoreDefaultState();
		}
	}
	
	// Принудительное завершение всех активных квестов с выдачей награды участникам.
	public void completeAllWithRewards() {
		for (WorldQuest quest : activeQuests) {
			quest.deleteHologram();
			quest.giveRewardsViaMail();
			quest.restoreDefaultState();
		}
	}
	
	public List<WorldQuest> getActiveQuests() {
		return activeQuests;
	}
	
	public int getRollback() {
		return rollback;
	}
	
	public long getRollbackExpiresMillis() {
		return rollbackExpiresMillis;
	}

}
