package ru.Baalberith.GameDaemon.Clans.Groups.Party;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.scoreboard.DisplaySlot;

import ru.Baalberith.GameDaemon.ConfigsDaemon;
import ru.Baalberith.GameDaemon.GD;
import ru.Baalberith.GameDaemon.GDPlayer;
import ru.Baalberith.GameDaemon.Storage;
import ru.Baalberith.GameDaemon.Clans.Groups.Party.Commands.PartyCMD;
import ru.Baalberith.GameDaemon.Extra.ActionObject;
import ru.Baalberith.GameDaemon.Extra.WaitingSystem;
import ru.Baalberith.GameDaemon.Extra.TeamDaemon.SidebarType;

public class PartyEngine {
	
	public static PartyEngine inst;
	public static Storage partyStorage = new Storage("partiesData.json", GD.inst.getDataFolder().getPath(), false);
	public static List<CommandSender> spySenders = new ArrayList<CommandSender>();
	
	public PartyEngine() {
		inst = this;
		Bukkit.getPluginManager().registerEvents(new PartyListener(), GD.inst);
		new PartyCMD();
	}
	
	static int cleanAfterDays;
	static int maxPlayers;
	static int maxNameLength;
	static int minNameLength;
	static Pattern namePattern;
	static int requestTimeout;
	static String randomCharset;
	
	static String sidebarDisplayName;
	static String sidebarColorOnlineOwner;
	static String sidebarColorOfflineOwner;
	static String sidebarColorOnlineMember;
	static String sidebarColorOfflineMember;
	static String sidebarHealthDisplay;
	
	public void reload() {
		ConfigurationSection m = ConfigsDaemon.messagesConfig.getConfigurationSection("party");
		Message.load(m, m.getString("label"));
		ConfigurationSection c = ConfigsDaemon.mainConfig.getConfigurationSection("party");
		
		cleanAfterDays = c.getInt("cleanAfterDays", 1);
		maxPlayers = c.getInt("maxPlayers", 5);
		maxNameLength = c.getInt("maxNameLength", 5);
		minNameLength = c.getInt("minNameLength", 2);
		namePattern = Pattern.compile(c.getString("namePattern", ".*"));
		requestTimeout = c.getInt("requestTimeout", 60);
		randomCharset = c.getString("random.charset", "QWERTYUIOPLKJHGFDSAZXCVBNMqwertyuioplkjhgfdsazxcvbnm1234567890");
		
		sidebarDisplayName = c.getString("scoreboard.name", "{party}").replace("&", "§");
		sidebarHealthDisplay = c.getString("scoreboard.healthDisplay", "&c❤").replace("&", "§");
		sidebarColorOnlineOwner = c.getString("scoreboard.colors.onlineOwner", "&f").replace("&", "§");
		sidebarColorOfflineOwner = c.getString("scoreboard.colors.offlineOwner", "&f").replace("&", "§");
		sidebarColorOnlineMember = c.getString("scoreboard.colors.onlineMember", "&f").replace("&", "§");
		sidebarColorOfflineMember = c.getString("scoreboard.colors.offlineMember", "&f").replace("&", "§");
		
		spySenders.clear();
		spySenders.add(Bukkit.getConsoleSender());
		
		Party.parties.clear();
		for (String k : partyStorage.keySet()) {
			long updateDate = partyStorage.getLong(k+".updateDate", System.currentTimeMillis());
			if ((System.currentTimeMillis()-updateDate)/1000/60/60/24 > cleanAfterDays) {
				partyStorage.set(k, null);
				continue;
			}
			
			String owner = partyStorage.getString(k+".owner", null);
			if (owner == null) continue;
			List<String> members = partyStorage.getStringList(k+".members");
			
			
			Party.parties.add(new Party(k, owner, members, maxPlayers, updateDate));
		}
		
		GD.log("[TRPGParty] Loaded "+Party.parties.size()+" parties.");
		partyStorage.save();
	}
	
	public static void saveAllToDisk() {
		Party.parties.stream().forEach(p -> p.saveToStorage());
		partyStorage.save();
	}
	
	public void help(CommandSender sender) {
		Message.help_title.send(sender);
		Message.help_default.send(sender);
		if (sender.hasPermission("gsm.party.smoder")) {
			Message.help_smoder.send(sender);
		}
	}
	
	public void leave(GDPlayer p) {
		if (p.inDungeon()) {
			Message.cannotChangeInDungeon.send(p);
			return;
		}
		
		if (p.party == null) {
			Message.noParty.send(p);
			return;
		}
		
		if (p.party.getOwner().equals(p.getName())) {
			Message.disbandAlert.replace("{name}", p.party.getName()).send(p);
			p.party.disband();
		} else {
			Message.left.replace("{name}", p.party.getName()).send(p);
			p.party.removeMember(p.getName());
			String alert = Message.leaveAlert.replace("{member}", p.getName()).replace("{name}", p.party.getName()).get();
			p.party.sendMessage(alert);
			p.party.updateSidebar();
			p.party.updateTeam();
			p.party = null;
		}
	}
	
	public void toggleChat(GDPlayer p) {
		if (p.partyChat()) {
			p.setPartyChat(false);
			Message.chatDeactivated.send(p);
		} else {
			p.setPartyChat(true);
			Message.chatActivated.send(p);
		}
	}
	
	public void socialspy(CommandSender sender) {
		if (!sender.hasPermission("gsm.party.smoder")) return;
		if (spySenders.contains(sender)) {
			Message.socialspy_disable.send(sender);
			spySenders.remove(sender);
		} else {
			Message.socialspy_enable.send(sender);
			spySenders.add(sender);
		}
	}
	
	
	public void create(GDPlayer p, String name) {
		if (p.party != null) {
			Message.alreadyHas.send(p);
			return;
		}
		
		if (!namePattern.matcher(name).matches()) {
			Message.namePattern.send(p);
			return;
		}
		
		if (name.length() > maxNameLength || name.length() < minNameLength) {
			Message.nameLength.replace("{min}", ""+minNameLength).replace("{max}", ""+maxNameLength).send(p);
			return;
		}
		
		if (Party.exists(name)) {
			Message.alreadyExists.replace("{name}", name).send(p);
			return;
		}
		
		Party party = new Party(name, p.getName());
		p.party = party;
		partyStorage.set(name.concat(".owner"), p.getName());
		party.updateSidebar();
		party.updateTeam();
		Message.created.replace("{name}", name).send(p);
	}
	
	public void invite(GDPlayer p, String target) {
		if (p.inDungeon()) {
			Message.cannotChangeInDungeon.send(p);
			return;
		}
		
		if (p.party == null) {
			Message.noParty.send(p);
			return;
		}
		
		if (!p.party.getOwner().equals(p.getName())) {
			Message.notOwner.send(p);
			return;
		}
		
		if (p.party.getSize() >= maxPlayers) {
			Message.full.replace("{name}", p.party.getName()).send(p);
			return;
		}
		
		GDPlayer tp = GD.getGDPlayer(target);
		if (tp == null || tp.p == null) {
			Message.playerNotOnline.replace("{player}", target).send(p);
			return;
		}
		
		if (tp.party != null) {
			Message.playerHasParty.replace("{player}", target).send(p);
			return;
		}
		
		WaitingSystem.createRequest(ActionObject.inviteRequest, tp, () -> {
			String alert = Message.joinAlert.replace("{member}", target).replace("{name}", p.party.getName()).get();
			p.party.sendMessage(alert);
			p.party.addMember(target);
			p.party.updateSidebar();
			p.party.updateTeam();
			tp.party = p.party;
			WaitingSystem.removeRequest(tp.getName(), ActionObject.inviteRequest); // fix почему-то сам не удаляется при touchClear
			Message.inviteAccept.replace("{name}", p.party.getName()).send(tp);
			Message.inviteAccepted.replace("{player}", target).send(p);
		}, "§bНажми сюда", Message.inviteRequest.replace("{player}", p.getName()).get(), true);
		Message.inviteRequested.replace("{player}", target).send(p);
	}
	
	public void kick(GDPlayer p, String target) {
		if (p.inDungeon()) {
			Message.cannotChangeInDungeon.send(p);
			return;
		}
		
		if (p.party == null) {
			Message.noParty.send(p);
			return;
		}
		
		if (!p.party.getOwner().equals(p.getName())) {
			Message.notOwner.send(p);
			return;
		}
		
		if (!p.party.contains(target)) {
			Message.noMember.replace("{member}", target).send(p);
			return;
		}
		
		if (p.getName().equalsIgnoreCase(target)) {
			Message.kickYourself.send(p);
			return;
		}
		
		// TODO не удаляется запись в скорборде от игрока
		p.party.removeMember(target);
		p.party.updateSidebar();
		p.party.updateTeam();
		
		GDPlayer tp = GD.getGDPlayer(target);
		if (tp != null && tp.p != null) {
			tp.party = null;
			tp.getTeamDaemon().unregisterObjective(DisplaySlot.SIDEBAR);
			Message.kicked.replace("{name}", p.party.getName()).send(tp);
		}
		
		p.party.sendMessage(Message.kickAlert.replace("{member}", target).replace("{name}", p.party.getName()).get());
	}
	
	public void join(GDPlayer p, String name) {
		if (p.party != null) {
			Message.alreadyHas.send(p);
			return;
		}
		
		Party party = Party.getParty(name);
		if (party == null) {
			Message.notFound.replace("{name}", name).send(p);
			return;
		}
		
		if (party.getSize() >= maxPlayers) {
			Message.full.replace("{name}", name).send(p);
			return;
		}
		
		// TODO в будущем с уровневой системой добавить уровневый порог вхождения.
		GDPlayer owner = GD.getGDPlayer(party.getOwner());
		if (owner == null || owner.p == null) {
			Message.ownerNotOnline.replace("{owner}", party.getOwner()).send(p);
			return;
		}
		
		if (owner.inDungeon()) {
			Message.ownerInDungeon.replace("{name}", name).send(p);
		}
		
		WaitingSystem.createRequest(ActionObject.joinRequest, owner, () -> {
			party.addMember(p.getName());
			party.updateSidebar();
			party.updateTeam();
			p.party = party;
			party.sendMessage(Message.joinAlert.replace("{member}", p.getName()).replace("{name}", party.getName()).get());
			Message.joinAccept.replace("{player}", p.getName()).send(owner);
			Message.joinAccepted.replace("{name}", party.getName()).send(p);
		}, "§bНажми сюда", Message.joinRequest.replace("{player}", p.getName()).get(), true); // TODO добавить description в json строку
		
		Message.joinRequested.replace("{name}", name).send(p);
	}
	
	public void scoreboard(GDPlayer p, String mode) {
		if (p.party == null) {
			Message.noParty.send(p);
			return;
		}
		
		boolean sidebar = mode.equalsIgnoreCase("show") ? true : false;
		
		if (p.hasPartySidebar() && sidebar) {
			Message.sidebar_alreadyShown.send(p);
			return;
		}
		
		if (!p.hasPartySidebar() && !sidebar) {
			Message.sidebar_alreadyHidden.send(p);
			return;
		}
		
		if (sidebar) {
			p.setSidebarType(SidebarType.party);
			p.party.updateSidebar(p);
			Message.sidebar_shown.send(p);
		} else {
			p.setSidebarType(null);
			p.getTeamDaemon().unregisterObjective(DisplaySlot.SIDEBAR);
			Message.sidebar_hidden.send(p);
		}
	}
	
	
	public void createRandom(GDPlayer p) {
		create(p, randomName());
	}
	
	public static String randomName() {
		String[] names = Message.randomGroupNames.gets();
		int rand = (int) Math.round(Math.random()*Math.random()*(names.length-1));
		return Party.exists(names[rand]) ? generateName() : names[rand];
	}
	
	private static String generateName() {
		Random r = new Random();
		int len = maxNameLength;
		String src = Message.charset.get();
		int srclen = src.length();
		
		StringBuilder b = new StringBuilder();
		for (int i = 0; i < len; i++) {
			int ch = r.nextInt(srclen);
			b.append(src.charAt(ch));
		}
		
		String name = b.toString();
		if(Party.exists(name)) return generateName();
		
		return name;
	}
}
