package ru.Baalberith.GameDaemon.Clans.Dungeons.Game;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.scoreboard.DisplaySlot;

import ru.Baalberith.GameDaemon.GD;
import ru.Baalberith.GameDaemon.GDData;
import ru.Baalberith.GameDaemon.GDPlayer;
import ru.Baalberith.GameDaemon.Clans.Dungeons.DungeonEngine;
import ru.Baalberith.GameDaemon.Clans.Dungeons.Dungeon;
import ru.Baalberith.GameDaemon.Clans.Dungeons.Layer;
import ru.Baalberith.GameDaemon.Clans.Dungeons.Level;
import ru.Baalberith.GameDaemon.Clans.Dungeons.Message;
import ru.Baalberith.GameDaemon.Clans.Groups.Party.Party;
import ru.Baalberith.GameDaemon.Extra.ActionObject;
import ru.Baalberith.GameDaemon.Extra.TeamDaemon.Objective;
import ru.Baalberith.GameDaemon.Extra.TeamDaemon.SidebarType;
import ru.Baalberith.GameDaemon.Extra.TeamDaemon.Trigger;
import ru.Baalberith.GameDaemon.Linkage.SAAPI;
import ru.Baalberith.GameDaemon.Extra.WaitingSystem;
import ru.Baalberith.GameDaemon.Utils.LocationManager;
import ru.Baalberith.GameDaemon.Utils.MathOperation;

public class Session implements Runnable {

	Dungeon dungeon;
	Party party;
	Layer layer;
	Level level;
	
	private int remainingTime;
	private GDPlayer owner;
	private String ownerName;
	private List<GDPlayer> members = new ArrayList<GDPlayer>();
	private List<String> membersName = new ArrayList<String>();
	
	boolean ended = false;
	boolean full = false; // Индикатор наполнения сессии
	long createTime = System.currentTimeMillis();
	int confirms = 0;
	
	public Session(Dungeon dungeon, Party party, Level level) {
		this.dungeon = dungeon;
		this.party = party;
		this.level = level;
		
		members = party.asPlayers();
		owner = members.get(0);
		ownerName = owner.getName();
		
		membersName = party.getMembers();
		
		remainingTime = level.getTime();
	}
	
	public boolean isFull() {
		return full;
	}
	
	public long getCreateTime() {
		return createTime;
	}
	
	@Override
	public void run() {
		
		// Закрываем начатую сессию, если время подошло к концу.
		if (remainingTime <= 0) {
			owner.getParty().sendMessage(Message.gameEndedTimeout.get());
			// Маркируем эту сессию как завершённую.
			ended = true;
			return;
		}
		
		// Если все участники покинули данж через wayback или умерли, то сессия закрывается.
		if (membersName.isEmpty()) {
			ended = true;
			return;
		}
		
		// Обновление скорборда с помощью повторной регистрации.
		int minutes = remainingTime/60;
		int seconds = remainingTime % 60;
		String secFormat = seconds < 10 ? "0".concat(String.valueOf(seconds)) : String.valueOf(seconds);
		String textFormat = DungeonEngine.scoreboard_text.replace("{m}", String.valueOf(minutes)).replace("{s}", secFormat);
		members.stream().forEach(mem -> {
			mem.getTeamDaemon().unregisterObjective(DisplaySlot.SIDEBAR);
			Objective o = mem.getTeamDaemon().getOrRegisterObjective(DisplaySlot.SIDEBAR, dungeon.getName(), Trigger.dummy);
			
			o.setScore(textFormat, DungeonEngine.scoreboard_score);
			for (GDPlayer p : members) {
				o.setScore(p.getName(), (int) p.getHealth());
			}
			o.getScoreboard().send(mem.p);
		});
		
		
		remainingTime--;
	}
	
	public void close() {
		// Когда происходит reload данжей, не начатые сессии обрабатывать не нужно.
		if (!full) { 
			ended = true;
			return;
		}
		
		// Возврат игроков из данжей.
		for (GDPlayer mem : members) {
			returnPlayer(mem);
			deleteGDPlayer(mem);
		}
		members.clear();
		
		// Если кто-то был оффлайн - переставляем точку спавна.
		for (String mem : membersName) {
			GDData data = GD.getGDData(mem);
			data.setJoinSpawn(LocationManager.serializeLocation(dungeon.getReturnLocation()));
		}
		
		// Удаляем всех существ из данжа.
		layer.despawnEntities();
		
	}
	
	public void returnPlayer(GDPlayer p) {
		p.teleportSync(dungeon.getReturnLocation());
	}
	
	public boolean containsPlayer(String playerName) {
		return membersName.contains(playerName);
	}
	
	public void deleteGDPlayer(GDPlayer p) {
		membersName.remove(p.getName());
		p.setSidebarType(SidebarType.party);
		p.setPartyChat(false);
		
		// Обновляем скроборд party, если party ещё есть.
		p.getTeamDaemon().unregisterObjective(DisplaySlot.SIDEBAR);
		if (p.hasParty()) {
			p.party.updateSidebar(p);
			p.party.updateTeam(p);
		}
	}
	
	// deleteGDPlayer используется в двух местах, в одном из которых нельзя удалять игрока
	// из members. Это случай с окончанием времени.
	public void deleteGDPlayerByWayback(GDPlayer p) {
		members.remove(p);
		deleteGDPlayer(p);
	}
	
	public void saveRunTime() {
		dungeon.addRunTimeToTop(ownerName, System.currentTimeMillis() - createTime, level.number());
	}
	
	public void tempDeleteGDPlayer(GDPlayer p) {
		members.remove(p);
	}
	
	public void restorePlayer(GDPlayer p) {
		members.add(p);
		p.setSidebarType(SidebarType.dungeon);
		p.setPartyChat(true);
	}
	
	public boolean isEnded() {
		return ended;
	}
	
	public Layer getLayer() {
		return layer;
	}
	
	public Level getLevel() {
		return level;
	}
	
	public Dungeon getDungeon() {
		return dungeon;
	}
	
	public Party getParty() {
		return party;
	}
	
	public List<GDPlayer> getMembers() {
		return members;
	}
	
	public boolean addPlayerToSession(GDPlayer p) {
		if (full || !members.contains(p)) return false;
		confirms++;
		
		// с этого момента данж запускается
		if(confirms == level.number()) {
			
//			// Если кто-то из участников покинул party.
//			if (party.getSize() != members.size()) {
//				Message.partyMismatch.replace("{player}", ).send(owner);
//				sendMessageToSession(Message.cancelledAlert.replace("{name}", dungeon.getName()).replace("{size}", level.number()).get());
//				returnKeys();
//				ended = true;
//				return true;
//			}
			
			// Телепортация всех игроков в локацию данжа.
			for (GDPlayer member : members) {
				layer.teleportToSpawn(member);
				p.setSidebarType(SidebarType.dungeon);
				member.setPartyChat(true);
				member.setDungeonCooldown(dungeon.getId(), dungeon.getJoinCooldown());
			}
			
			// Закрываем слот данжа.
			layer.setBusy(true);
			
			// Обновляем голограмму данжа.
			dungeon.updateHologram();
			
			// Отрабатываем команды для спавна существ.
			layer.loadChunks();
			level.runCommands(layer.number());
			
			// Маркируем эту сессию, как полную.
			return (full = true);
		}
		
		// Оповещаем всех участников группы о статусе заполенния сессии.
		sendMessageToSession(Message.playerReady.replace("{player}", p.getName()).replace("{now}", confirms)
				.replace("{max}", level.number()).replace("{name}", dungeon.getName()).get());
		
		return true;
	}
	
	public void sendMessageToSession(String msg) {
		members.forEach(member -> member.sendMessage(msg));
	}
	
	public void returnKeys() {
		for (GDPlayer member : members) {
			member.giveItem(dungeon.getKey());
			Message.returnKey.send(member);
			confirms--;
		}
		
		if (confirms > 0) {
			owner.giveItem(dungeon.getKey(), confirms);
			Message.returnKeyFromOfflines.replace("{amount}", confirms).send(owner);
		}
	}
	
	
	/**
	 * Запрос на подтверждение участия в сессии всем участника, кроме лидера.
	 */
	public void sendRequestToMembers() {
		for (GDPlayer member : members) {
			// Если лидер группы, это единственный игрок в группе, то отправляем ему запрос запуска сессии.
			if(member == owner) {
				if (confirms == level.number()-1) {
					sendRequestToOwner();
					return;
				}
				Message.joinRequestSendedToMembers.replace("{name}", dungeon.getName()).replace("{size}", level.number()).send(owner);
				continue;
			}
			
			WaitingSystem.createRequest(ActionObject.DungeonPortal, member, () -> {
				if (ended) return;
				
				// Предварительно проверяем наличие ключа и расположение игрока по отношению к данжу.
				if (!dungeon.hasKey(member) || !dungeon.hasDistance(member)) {
					Message.confirmRequirement.send(member);
					return;
				}
				
				// Проверяем combat участника.
				if (SAAPI.inCombat(member.getBukkitPlayer())) {
					Message.cannotJoinCombat.send(member);
					return;
				}
				
				// Пробуем забрать ключ и добавить в сессию.
				if (member.takeItem(dungeon.getKey()) && addPlayerToSession(member)) {
					Message.confirmed.send(member);
					if(confirms == level.number()-1) {
						// Отправляем запрос лидеру, если все участники группы уже подтвердили участие.
						sendRequestToOwner();
					}
				} else {
					// В ином случае - отменяем сессию и возвращаем ключи.
					sendMessageToSession(Message.cancelledAlert.get());
					returnKeys();
					ended = true;
				}
				
				// Деактивируем кнопку запроса в чате.
				WaitingSystem.removeRequest(member.getName(), ActionObject.DungeonPortal);
			}, "Нажмите, если хотите подтвердить\\nсвоё участие в данже.", DungeonEngine.sessionTimeout, false);
		}
	}
	
	/**
	 * Запрос на подтверждение участия в сессии лидеру группы.
	 */
	public void sendRequestToOwner() {
		WaitingSystem.createRequest(ActionObject.DungeonPortal, owner, () -> {
			if (ended) return;
			// Проверяем наличие ключа и дистанции у лидера.
			if (!dungeon.hasKey(owner) || !dungeon.hasDistance(owner)) {
				Message.confirmRequirement.send(owner);
				return;
			}
			
			// Проверка на онлайн всех игроков в party.
			if (members.size() != level.number()) {
				Message.requirement_online.send(owner);
				return;
			}
			
			// проверка размера группу, если Создал сессию на 1 игрока, пригласил второго - сессия отменяется.
			if (party.getSize() != level.number()) {
				Message.requirement_completeness.send(owner);
				returnKeys();
				ended = true;
				return;
			}
			
			// Ещё проверки...
			for (GDPlayer mem : members) {
				if (!party.contains(mem.getName())) {
					Message.partyMismatch.replace("{player}", mem.getName()).send(owner);
					sendMessageToSession(Message.cancelledAlert.replace("{name}", dungeon.getName()).replace("{size}", level.number()).get());
					returnKeys();
					ended = true;
					// Деактивируем кнопку запроса в чате.
					WaitingSystem.removeRequest(owner.getName(), ActionObject.DungeonPortal);
					return;
				}
				
				// Проверяем расстояние всех участников к воротам данжа.
				if (!dungeon.hasDistance(mem)) {
					Message.requirement_distance.replace("{name}", dungeon.getName()).replace("{size}", level.number()).send(owner);
					return;
				}
				
				// Проверяем кулдаун участников к этому данжу.
				long remCooldown = mem.getDungeonRemCooldown(dungeon.getId());
				if (remCooldown < 0) {
					owner.sendMessage(MathOperation.makeTimeToString(Message.joinCooldownOther.replace("{player}", mem.getName()).get(), -remCooldown*1000));
					return;
				}
				
				// Проверяем наличие combat'а у участников.
				if (SAAPI.inCombat(mem.getBukkitPlayer())) {
					Message.requirement_combat.send(owner);
					return;
				}
			}
			
			// Получаем свободный слот данжа для подключения, если такой слот есть, то выделяем его.
			Layer layer = dungeon.getFreeLayer();
			if (layer == null || !layer.isEnabled()) {
				Message.busy.replace("{name}", dungeon.getName()).replace("{size}", level.number()).send(owner);
				return;
			}
			this.layer = layer;
			
			// Забираем последний ключ у владельца и добавляем его в сессию, если всё ок, то слот помечается как busy.
			if (owner.takeItem(dungeon.getKey()) && addPlayerToSession(owner)) {
				Message.confirmed.send(owner);
			} else {
				// В другом случае сессия закрывается, ключи возвращаются.
				sendMessageToSession(Message.cancelledAlert.get());
				returnKeys();
				ended = true;
			}
			
			// Деактивируем кнопку запроса в чате.
			WaitingSystem.removeRequest(owner.getName(), ActionObject.DungeonPortal);
		}, "Все игроки подтвердили своё участие,\\nнажмите, чтобы перенестись в данж.", DungeonEngine.sessionTimeout, false);
	}
}
