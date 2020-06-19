package ru.Baalberith.GameDaemon.Extra;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Score;

public class TeamDaemon {
	
	public enum Trigger {
		dummy, health, level, xp, playerKillCount, air, food;
	}
	
	public enum SidebarType {
		party, dungeon;
	}
	
	private static ConcurrentHashMap<String, TeamDaemon> teamDaemons = new ConcurrentHashMap<String, TeamDaemon>();
	
	private org.bukkit.scoreboard.Scoreboard bukkitScoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
	
	private HashMap<String, Team> teams = new HashMap<String, TeamDaemon.Team>();
	private HashMap<DisplaySlot, Objective> objs = new HashMap<DisplaySlot, TeamDaemon.Objective>();
	private Scoreboard scoreboard = new Scoreboard();
	
	public final String daemonName;
	
	private TeamDaemon(String daemonName) {
		this.daemonName = daemonName;
		teamDaemons.put(daemonName, this);
	}
	
	public static TeamDaemon getOrCreate(String daemonName) {
		TeamDaemon teamDaemon = teamDaemons.get(daemonName);
		return teamDaemon == null ? new TeamDaemon(daemonName) : teamDaemon;
	}
	
	public static void unregisterAll() {
		for (Entry<String, TeamDaemon> team : teamDaemons.entrySet()) {
			team.getValue().unregisterAllTeams();
			team.getValue().unregisterAllObjectives();
		}
	}
	
	public void unregisterAllTeams() {
		teams.entrySet().stream().forEach(t -> t.getValue().bukkitTeam.unregister());
		teams.clear();
	}
	
	public void unregisterAllObjectives() {
		objs.entrySet().stream().forEach(o -> o.getValue().bukkitObjective.unregister());
		objs.clear();
	}
	
	public void unregisterTeam(String teamName) {
		if (teams.containsKey(teamName)) {
			teams.get(teamName).bukkitTeam.unregister();
			teams.remove(teamName);
		}
	}
	
	public void unregisterObjective(DisplaySlot displaySlot) {
		if (objs.containsKey(displaySlot)) {
			objs.get(displaySlot).bukkitObjective.unregister();
			objs.remove(displaySlot);
		}
	}
	
	public Team getOrRegisterTeam(String teamName, boolean canSeeFriendlyInvis, boolean friendlyFire) {
		Team team = teams.get(teamName);
		if (team == null) {
			team = new Team(teamName, canSeeFriendlyInvis, friendlyFire);
			teams.put(teamName, team);
		}
		return team;
	}
	
	// Создаёт 
	public Objective getOrRegisterObjective(DisplaySlot displaySlot, String displayName, Trigger trigger) {
		Objective objective = objs.get(displaySlot);
		if (objective == null) {
//			System.out.println("CREATING NEW");
			objective = new Objective(displaySlot, displayName, trigger);
			objs.put(displaySlot, objective);
		} else if (objective.getTrigger() != trigger) {
//			System.out.println("GETTING EXIST");
			objective.setNewTrigger(trigger);
		}
		return objective;
	}

	public Scoreboard getScoreboard() {
		return scoreboard;
	}
	
	/**
	 * Этот подкласс позволяет создать группу с игроками. Но это только список и набор условий
	 * взаимодействия игроков в команде. Настройки: prefix, suffix, friendlyFire, invisBypass.
	 * @author Kronos
	 */
	public class Team {
		private org.bukkit.scoreboard.Team bukkitTeam;
		
		public Team(org.bukkit.scoreboard.Team team) {
			bukkitTeam = team;
		}
		
		public Team(String teamName, boolean canSeeFriendlyInvis, boolean friendlyFire) {
//			ThreadDaemon.sync(() -> {
				bukkitTeam = bukkitScoreboard.registerNewTeam(teamName);
				bukkitTeam.setCanSeeFriendlyInvisibles(canSeeFriendlyInvis);
				bukkitTeam.setAllowFriendlyFire(friendlyFire);
//			});
		}
		
		public org.bukkit.scoreboard.Team getBukkitTeam() {
			return bukkitTeam;
		}
		
		public Team setPrefix(String prefix) {
			bukkitTeam.setPrefix(prefix);
			return this;
		}
		
		public Team setSuffix(String suffix) {
			bukkitTeam.setSuffix(suffix);
			return this;
		}
		
		public Team setDisplayname(String displayname) {
			bukkitTeam.setDisplayName(displayname);
			return this;
		}
		
		public Team addEntry(String name) {
			bukkitTeam.addPlayer(Bukkit.getOfflinePlayer(name));
//			bukkitTeam.addEntry(name);
			return this;
		}
		
		public Team removeEntry(String name) {
			bukkitTeam.removePlayer(Bukkit.getOfflinePlayer(name));
//			bukkitTeam.removeEntry(name);
			return this;
		}
		
		public Team addEntries(String... names) {
			for (int i = 0; i < names.length; i++) {
				if (names[i] != null) bukkitTeam.addPlayer(Bukkit.getOfflinePlayer(names[i])); //bukkitTeam.addEntry(names[i]);
			}
			return this;
		}
		
		public Team addEntries(List<String> names) {
			names.stream().forEach(n -> bukkitTeam.addPlayer(Bukkit.getOfflinePlayer(n)));
			return this;
		}
		
		public Team removeEntries() {
//			bukkitTeam.getEntries().stream().forEach(e -> removeEntry(e));
			bukkitTeam.getPlayers().stream().forEach(e -> removeEntry(e.getName()));
			return this;
		}
		
		public org.bukkit.scoreboard.Scoreboard getScoreboard() {
			return bukkitTeam.getScoreboard();
		}
		
		public Set<String> getEntries() {
			Set<String> set = new HashSet<String>();
			bukkitTeam.getPlayers().stream().forEach(e -> set.add(e.getName()));
			return set;
		}
	}
	
	/**
	 * Этот клас предназначен для быстрого размещения информации во все возможные DisplaySlots.
	 * Например: SIDEBAR, BELOW_NAME, PLAYER_LIST.
	 * Методы этого класса могут только установить новое значение в Score.
	 * @author Kronos
	 */
	public class Objective {
		
		private org.bukkit.scoreboard.Objective bukkitObjective;
		private Trigger trigger;
		
		private Objective(DisplaySlot displaySlot, String title, Trigger trigger) {
//			ThreadDaemon.sync(() -> {
				bukkitObjective = bukkitScoreboard.registerNewObjective(displaySlot.name(), trigger.name());
				bukkitObjective.setDisplaySlot(displaySlot);
				bukkitObjective.setDisplayName(title);
				this.trigger = trigger;
//			});
		}
		
		private void setNewTrigger(Trigger trigger) {
//			ThreadDaemon.sync(() -> {
				String title = bukkitObjective.getDisplayName();
				DisplaySlot displaySlot = bukkitObjective.getDisplaySlot();
				bukkitObjective.unregister();
				bukkitObjective = bukkitScoreboard.registerNewObjective(displaySlot.name(), trigger.name());
				bukkitObjective.setDisplaySlot(displaySlot);
				bukkitObjective.setDisplayName(title);
//			});
		}
		
		public Trigger getTrigger() {
			return trigger;
		}
		
		public org.bukkit.scoreboard.Objective getBukkitObjective() {
			return bukkitObjective;
		}
		
		public String getDisplayName() {
			return bukkitObjective.getDisplayName();
		}
		
		public Score getScore(String name) {
			return bukkitObjective.getScore(name);
		}
		
		public void resetScores(String name) {
			bukkitObjective.getScoreboard().resetScores(name);
		}
		
		public Scoreboard getScoreboard() {
			return scoreboard;
		}
		
		public Objective setScore(String name, int value) {
			if (name.length() > 48) name = name.substring(0, 48);
			Score s = getScore(name);
			s.setScore(value);
			return this;
		}
	}
	
	/**
	 * Продвинутый класс для удобного редактирования списка SIDEBAR, поддерживает пустые строки.
	 * @author Kronos
	 */
	public class Scoreboard {
		
		private Map<String, Integer> lines = new LinkedHashMap<String, Integer>();
		
		public void blankLine() {
			add("");
		}
		
		public void add(String text) {
			text = fixDuplicates(text);
			set(text);
		}
		
		public void set(String text) {
			set(text, null);
		}
		
		public void set(String text, Integer score) {
			if (text.length() > 48) text = text.substring(0, 48);
			lines.put(text, score);
		}
		
		private String fixDuplicates(String text) {
			while (lines.containsKey(text)) text = text + "§r";
			return text;
		}
		
		public Scoreboard build() throws Exception {
			Objective o = objs.get(DisplaySlot.SIDEBAR);
			if (o != null) {
				int size = lines.size();
				for (Entry<String, Integer> e : lines.entrySet()) {
					Score score = o.getScore(e.getKey());
					Integer value = e.getValue();
					score.setScore(value == null ? size : value);
					size--;
				}
			} else throw new Exception("Objective 'SIDEBAR' not registered.");
			return this;
		}
		
		public void send(Player... players) {
			for (Player p : players) p.setScoreboard(bukkitScoreboard);
		}
		
		public void send(List<Player> players) {
			players.stream().forEach(p -> p.setScoreboard(bukkitScoreboard));
		}
	}
}
