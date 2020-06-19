package ru.Baalberith.GameDaemon;

import java.io.File;

import ru.Baalberith.GameDaemon.AutoStopServer.AutoStop;
import ru.Baalberith.GameDaemon.CNPSsFactions.FactionItems;
import ru.Baalberith.GameDaemon.CargoDelivery.CargoEngine;
import ru.Baalberith.GameDaemon.Clans.ClansEngine;
import ru.Baalberith.GameDaemon.Clans.Dungeons.DungeonEngine;
import ru.Baalberith.GameDaemon.Clans.Groups.Party.PartyEngine;
import ru.Baalberith.GameDaemon.CloudMail.MailEngine;
import ru.Baalberith.GameDaemon.Commands.BasicCommands;
import ru.Baalberith.GameDaemon.Commands.CoinsConverter;
import ru.Baalberith.GameDaemon.Commands.CommandAI;
import ru.Baalberith.GameDaemon.Commands.Introduction;
import ru.Baalberith.GameDaemon.Commands.PremiumHeal;
import ru.Baalberith.GameDaemon.DropBags.DropBags;
import ru.Baalberith.GameDaemon.ExperienceExchange.BottledExpEngine;
import ru.Baalberith.GameDaemon.Extra.CooldownSystem;
import ru.Baalberith.GameDaemon.Extra.HandlerEvents;
import ru.Baalberith.GameDaemon.Extra.WaitingSystem;
import ru.Baalberith.GameDaemon.Extra.WarmUpSystem;
import ru.Baalberith.GameDaemon.Jail.ImprisonedPlayers;
import ru.Baalberith.GameDaemon.MCMMOBoost.BoostEngine;
import ru.Baalberith.GameDaemon.Menu.MenuEngine;
import ru.Baalberith.GameDaemon.MuteDaemon.MuteEngine;
import ru.Baalberith.GameDaemon.PVPRating.RatingEngine;
import ru.Baalberith.GameDaemon.RPGSpawn.SpawnEngine;
import ru.Baalberith.GameDaemon.ServerLimits.BlocksLimit;
import ru.Baalberith.GameDaemon.Sharpening.SharpEngine;
import ru.Baalberith.GameDaemon.Statistics.StatEngine;
import ru.Baalberith.GameDaemon.StuffSynchronizer.SynchronizeEngine;
import ru.Baalberith.GameDaemon.Summoning.SummonItems;
import ru.Baalberith.GameDaemon.Warps.WarpEngine;
import ru.Baalberith.GameDaemon.WorldAnchor.DolmenEngine;
import ru.Baalberith.GameDaemon.WorldQuests.WorldQuestsEngine;

public class DaemonsRecruiter {
	
	StatEngine statEngine = new StatEngine();
	
	HandlerEvents handlerEvents = new HandlerEvents();
	CoinsConverter coinsConverter = new CoinsConverter();
	
	private ConfigsDaemon configs = new ConfigsDaemon();
	private WarmUpSystem warmUpSystem = new WarmUpSystem();
	private WaitingSystem waitingSystem = new WaitingSystem();
	private BasicCommands basicCommands = new BasicCommands();
	private CooldownSystem cooldownSystem = new CooldownSystem();
	
	private AutoStop autoStop = new AutoStop();
	private BoostEngine boostEngine = new BoostEngine();
	private CargoEngine cargoEngine = new CargoEngine();
	private SummonItems summonItems = new SummonItems();
	private FactionItems factionItems = new FactionItems();
	private ImprisonedPlayers imprisonedPlayers = new ImprisonedPlayers();
	
	private BottledExpEngine expToBottleEvent = new BottledExpEngine();
	private RatingEngine ratingEngine = new RatingEngine();
	
	private DolmenEngine dolmenEngine = new DolmenEngine();
	private SpawnEngine spawnEngine = new SpawnEngine();
	private BlocksLimit blocksLimit = new BlocksLimit();
	private WarpEngine warpEngine = new WarpEngine();
	private DropBags dropBags = new DropBags();
//	private Newbies newbies = new Newbies();
	
	private Introduction introduction = new Introduction();
	private MenuEngine menuEngine = new MenuEngine();
	private CommandAI commandAI = new CommandAI();
	
	private SharpEngine sharpEngine = new SharpEngine();
	private SynchronizeEngine synchronizeEngine = new SynchronizeEngine();
	private MuteEngine muteEngine = new MuteEngine();
	
	private ClansEngine clansEngine = new ClansEngine();
//	private LiLSEngine liLSEngine = new LiLSEngine();
	
	PremiumHeal premiumHeal = new PremiumHeal();
	
	public DaemonsRecruiter() {
		load();
		clearOldPlayers();
	}
	
	private void load() {
		// Что-то что выполняется только при запуске (1 раз).
		reload();
		
		DungeonEngine.reload();
		WorldQuestsEngine.reload();
		
		GD.sendServerMessage(ConfigsDaemon.messagesConfig.get().getString("other.enable"));
	}
	
	public void reload() {
		configs.reload();
		warmUpSystem.reload();
		waitingSystem.reload();
		basicCommands.reload();
		cooldownSystem.reload();
		autoStop.reload();
		
		boostEngine.reload();
		cargoEngine.reload();
		summonItems.reload();
		factionItems.reload();
		imprisonedPlayers.reload();
		expToBottleEvent.reload();
		
		ratingEngine.reload();
		
		dolmenEngine.reload();
		spawnEngine.reload();
		blocksLimit.reload();
		warpEngine.reload();
		dropBags.reload();
		
		
//		newbies.reload();
		introduction.reload();
		menuEngine.reload();
		commandAI.reload();
		sharpEngine.reload();
		synchronizeEngine.reload();
		muteEngine.reload();
		
		clansEngine.reload();
//		liLSEngine.reload();
		
		MailEngine.reload();
	}
	
	public void unload() {
		WarpEngine.inst.saveAllToDisk();
		PartyEngine.saveAllToDisk();
		DungeonEngine.saveAllToDisk();
		WorldQuestsEngine.saveAllToDisk();
		MailEngine.saveAllToDisk();
		
		
		GD.inst.getServer().getConsoleSender().sendMessage(ConfigsDaemon.messagesConfig.get().getString("other.disable").replace("&", "\u00a7"));
	}
	
	private void clearOldPlayers() {
		String[] fs = GD.getPlayerFiles();
		long unixDate = System.currentTimeMillis();
		int amount = 0;
		for (String string : fs) {
			GDData dc = new GDData(string.replace(".json", ""), ConfigsDaemon.STATS_FOLDER);
			if (dc.getQuitDate(0) + (long) 1000*60*60*24*90 <= unixDate) {
				File f = new File("." + File.separator + ConfigsDaemon.STATS_FOLDER + File.separator + string);
				if (f.delete()) amount++;
			}
		}
		GD.log("[GameDaemon] Deleted "+amount+" files from players statistic.");
	}
}
