package ru.Baalberith.GameDaemon.Clans.Groups.Party;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.bukkit.scoreboard.DisplaySlot;

import ru.Baalberith.GameDaemon.GD;
import ru.Baalberith.GameDaemon.GDPlayer;
import ru.Baalberith.GameDaemon.Extra.TeamDaemon;
import ru.Baalberith.GameDaemon.Extra.TeamDaemon.Objective;
import ru.Baalberith.GameDaemon.Extra.TeamDaemon.Trigger;

public class Party {
	
	public static CopyOnWriteArrayList<Party> parties = new CopyOnWriteArrayList<Party>();
	
	private String name;
	private String[] members;
	private int pointer = 1;
	
	private long updateDate;
	
	public Party(String name, String owner, List<String> members, int size, long updateDate) {
		this.name = name;
		this.members = new String[size];
		this.members[0] = owner;
		for (int i = 1; i <= members.size(); i++) {
			this.members[i] = members.get(i-1);
			pointer++;
		}
		this.updateDate = updateDate;
		parties.add(this);
	}
	
	public Party(String name, String owner) {
		this.name = name;
		this.members = new String[PartyEngine.maxPlayers];
		this.members[0] = owner;
		this.updateDate = System.currentTimeMillis();
		parties.add(this);
	}
	
	public void saveToStorage() {
		PartyEngine.partyStorage.set(name.concat(".owner"), members[0]);
		PartyEngine.partyStorage.set(name.concat(".members"), null);
		for (int i = 1; i < members.length; i++) {
			if (members[i] == null) continue;
			PartyEngine.partyStorage.addObjectToList(name.concat(".members"), members[i]);
		}
		PartyEngine.partyStorage.set(name.concat(".updateDate"), updateDate);
	}
	
	public List<GDPlayer> asPlayers() {
		List<GDPlayer> players = new ArrayList<GDPlayer>();
		for (String member : members) {
			if (member == null) continue;
			GDPlayer p = GD.getGDPlayer(member);
			players.add(p);
		}
		return players;
	}
	
	public void disband() {
		PartyEngine.partyStorage.set(name, null);
		for (GDPlayer p : asPlayers()) {
			if (p == null || p.p == null) continue;
			p.getTeamDaemon().getOrRegisterTeam(p.getName(), true, false).removeEntries();
			p.getTeamDaemon().unregisterObjective(DisplaySlot.SIDEBAR);
			p.setParty(null);
		}
		parties.remove(this);
	}
	
	public void sendMessage(String message) {
		for (GDPlayer p : asPlayers()) {
			p.sendMessage(message);
		}
	}
	
	public boolean contains(String name) {
		for (int i = 0; i < members.length; i++) {
			if (members[i] == null) break;
			if (members[i].equals(name)) return true;
		}
		return false;
	}
	
	public int getSize() {
		return pointer;
	}
	
	public long getUpdateDate() {
		return updateDate;
	}
	
	public String getName() {
		return name;
	}
	
	public String getOwner() {
		return members[0];
	}
	
	public List<String> getMembers() {
		List<String> players = new ArrayList<String>();
		for (int i = 0; i < members.length; i++) {
			if (members[i] == null) continue;
			players.add(members[i]);
		}
		return players;
	}
	
	// pointer всегда показывает на пустую ячейку после заполненных.
	// При удалении мембера участники должны сдивнуться вверх, если удаление было
	// из середины списка.
	public void removeMember(String name) {
		for (int i = 1; i < members.length; i++) {
			if (!members[i].equals(name)) continue;
			GDPlayer p = GD.getGDPlayer(name);
			if (p.p != null) {
				p.getTeamDaemon().getOrRegisterTeam(members[i], true, false).removeEntries();
				p.getTeamDaemon().unregisterObjective(DisplaySlot.SIDEBAR);
			}
			members[i] = null;
			if (pointer-i-1 == 0) break;
			for (int j = i; j < pointer; j++) {
				members[j] = members[j+1];
			}
			break;
 		}
		removeEntryFromSideBar(name);
		pointer--;
	}
	
	public void addMember(String name) {
		if (pointer == members.length) return;
		members[pointer] = name;
		pointer++;
	}
	
	// Нужно для удаления игрока и скорборда, когда его кикнули или он вышел из группы, а его ник остался висеть.
	public void removeEntryFromSideBar(String entry) {
		for (int j = 0; j < members.length; j++) {
			if (members[j] == null) break;
			GDPlayer m = GD.getGDPlayer(members[j]);
			Objective o = m.getTeamDaemon().getOrRegisterObjective(DisplaySlot.SIDEBAR, "", Trigger.dummy);
			o.resetScores(PartyEngine.sidebarColorOfflineMember.concat(entry));
			o.resetScores(PartyEngine.sidebarColorOnlineMember.concat(entry));
		}
	}
	
	public void updateTeam(GDPlayer p) {
		p.getTeamDaemon().getOrRegisterTeam(p.getName(), true, false).removeEntries().addEntries(members);
	}
	
	public void updateTeam() {
		updateDate = System.currentTimeMillis();
		for (GDPlayer p : asPlayers()) {
			if (p == null || p.p == null) continue;
			updateTeam(p);
		}
	}
	
	public void updateSidebar() {
		for (int i = 0; i < members.length; i++) {
			if (members[i] == null) break;
			GDPlayer p = GD.getGDPlayer(members[i]);
			if (p == null || p.p == null) continue;
			updateSidebar(p);
		}
	}
	
	public void updateSidebar(GDPlayer p) {
		if (!p.hasPartySidebar()) return;
		TeamDaemon team = p.getTeamDaemon();
		Objective o = team.getOrRegisterObjective(DisplaySlot.SIDEBAR, PartyEngine.sidebarDisplayName.replace("{party}", name), Trigger.dummy);
		
		for (int j = 0; j < members.length; j++) {
			if (members[j] == null) break;
			GDPlayer m = GD.getGDPlayer(members[j]);
			double health = (m == null || m.p == null) ? -1 : Math.round(m.getHealth());
			if (health == 0) health = -1;
			
			if (j == 0) {
				o.resetScores(PartyEngine.sidebarColorOfflineOwner.concat(members[j]));
				o.resetScores(PartyEngine.sidebarColorOnlineOwner.concat(members[j]));
				o.setScore(health == -1 ? PartyEngine.sidebarColorOfflineOwner.concat(members[j]) : PartyEngine.sidebarColorOnlineOwner.concat(members[j]), (int) health);
			} else {
				o.resetScores(PartyEngine.sidebarColorOfflineMember.concat(members[j]));
				o.resetScores(PartyEngine.sidebarColorOnlineMember.concat(members[j]));
				o.setScore(health == -1 ? PartyEngine.sidebarColorOfflineMember.concat(members[j]) : PartyEngine.sidebarColorOnlineMember.concat(members[j]), (int) health);
			}
		}
	}
	
	public static Party getParty(String name) {
		for (Party party : parties) {
			if (party.name.equalsIgnoreCase(name)) return party;
		}
		return null;
	}
	
	public static Party getByPlayer(String name) {
		for (Party party : parties) {
			List<String> members = party.getMembers();
			if (members.contains(name)) return party;
		}
		return null;
	}
	
	public static boolean exists(String name) {
		return getParty(name) != null;
	}
	
}
