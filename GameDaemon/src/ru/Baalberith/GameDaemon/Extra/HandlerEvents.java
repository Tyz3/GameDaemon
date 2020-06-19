package ru.Baalberith.GameDaemon.Extra;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import ru.Baalberith.GameDaemon.GD;
import ru.Baalberith.GameDaemon.GDPlayer;
import ru.Baalberith.GameDaemon.Utils.ThreadDaemon;

public class HandlerEvents implements Listener {
	
//	private List<String> inWrite = new ArrayList<String>();
//	private boolean hasMeta = false;
	
	public HandlerEvents() {
		Bukkit.getPluginManager().registerEvents(this, GD.inst);
//		GD.inst.getCommand("list-writer").setExecutor(this);
		
		ThreadDaemon.asyncTimer(() -> dungeonBanshiMove(), 100, 10);
//		ThreadDaemon.asyncTimer(() -> playerFlyCheck(), 100, 100);
	}
	
	@EventHandler
	public void onEnderPearl(PlayerInteractEvent e) {
		if (e.getPlayer().isOp()) return;
		if (e.getItem() == null) return;
		if (e.getItem().getType() == Material.ENDER_PEARL) e.setCancelled(true);
	}
	
//	@EventHandler
//	public void onJoinTest(PlayerJoinEvent e) {
//		Player p = e.getPlayer();
//		ThreadDaemon.async(() -> {
//			for (Sound sound : Sound.values()) {
//				p.playSound(p.getLocation(), sound, 5, 5);
//				p.sendMessage("> "+sound.name());
//				try {
//					Thread.sleep(1000);
//				} catch (InterruptedException e1) {
//					// TODO Auto-generated catch block
//					e1.printStackTrace();
//				}
//			}
//		});
//	}

//	@EventHandler
//	public void onInvOpen(InventoryOpenEvent e) {
//		if (!(e.getPlayer() instanceof Player)) return;
//		Player p = (Player) e.getPlayer();
//		if (!inWrite.contains(p.getName())) return;
//		
//		Inventory i = e.getInventory();
//		List<String> list = ConfigsDaemon.tempDataConfig.get().getStringList("market-list");
//		list = (list == null || list.isEmpty()) ? new ArrayList<String>() : list;
//
//		for (ItemStack itemStack : i) {
//			if (itemStack == null) continue;
//			String add = (hasMeta) ? itemStack.getType().toString()+":"+itemStack.getDurability() : itemStack.getType().toString();
//			if (list.contains(add)) continue;
//			list.add(add);
//		}
//		ConfigsDaemon.tempDataConfig.get().set("market-list", list);
//		ConfigsDaemon.tempDataConfig.save();
//	}
	
//	public void playerFlyCheck() {
//		for (GDPlayer p : GD.online) {
//			if (!p.p.getAllowFlight()) return;
//			if (p.isOp() || p.hasPermission("gsm.fly.bypass")) return;
//			String world = p.getWorld().getName();
//			if (world.equalsIgnoreCase("world")) return;
//			p.p.setFlying(false);
//		}
//	}
	
//	@Override
//	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
//		if (!sender.isOp()) return false;
//		
//		if (cmd.getName().equalsIgnoreCase("list-writer")) {
//			if (inWrite.contains(sender.getName())) {
//				inWrite.remove(sender.getName());
//				sender.sendMessage(label + " is off ("+hasMeta+")");
//			} else {
//				inWrite.add(sender.getName());
//				if (args.length == 1) hasMeta = Boolean.parseBoolean(args[0]);
//				sender.sendMessage(label + " is on ("+hasMeta+")");
//			}
//			return true;
//		}
//		return false;
//	}
	
//	@EventHandler
//	public void repairProgressAdd(PlayerJoinEvent e) {
//		Player p = e.getPlayer();
//		if (!p.hasPermission("arrep.ratio.progress2")) {
//			long play = e.getPlayer().getStatistic(Statistic.PLAY_ONE_TICK)*50;
//			if (play < 200*60*60*1000) return;
//			Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "pex user "+p.getName()+" add arrep.ratio.progress2");
//		}
//		if (!p.hasPermission("arrep.ratio.progress3")) {
//			long play = e.getPlayer().getStatistic(Statistic.PLAY_ONE_TICK)*50;
//			if (play < 300*60*60*1000) return;
//			Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "pex user "+p.getName()+" add arrep.ratio.progress3");
//		}
//	}

	
	// Телепорт по наступлению на блок в данже у Банши.
	// С рандомной выборкой.
	Material blockTp = Material.matchMaterial("Tutorial_technicSlabBlock");
	byte dataOuter = 4;
	byte dataInner = 6;
	int[][] coordsInner = {{486,32,628},{483,32,626},{480,32,629},{478,32,632},{481,32,634},{482,32,638},{486,32,637},{488,32,639},{490,32,637},{493,32,635},
			{491,32,632},{492,32,629},{489,32,629},{488,32,626}};
	int[][] coordsOuter = {{485,34,606},{488,34,605},{491,34,606},{494,34,605},{497,34,607},{501,34,611},{503,34,614},{506,34,615},{508,34,618},{509,34,621},{511,34,623},
			{509,34,626},{512,34,628},{510,34,631},{512,34,635},{510,34,638},{511,34,641},{508,34,643},{509,34,647},{505,34,649},{505,34,652},{501,34,653},{500,34,656},
			{495,34,655},{494,34,658},{490,34,656},{488,34,658},{485,34,655},{482,34,658},{479,34,657},{476,34,655},{473,34,657},{471,34,654},{468,34,652},{465,34,652},
			{463,34,648},{464,34,646},{460,34,644},{461,34,640},{458,34,638},{459,34,635},{460,34,632},{458,34,629},{458,34,626},{459,34,623},{459,34,619},{462,34,618},
			{462,34,615},{465,34,613},{466,34,611},{470,34,609},{473,34,607},{476,34,607},{477,34,605},{480,34,607},{482,34,605}};
	World world = Bukkit.getWorld("dungeonworld");
	public void dungeonBanshiMove() {
		for (GDPlayer p : GD.online) {
			if (world == null) return;
			if (blockTp == null) return;
			Location loc = p.getLocation();
			if (loc.getWorld() != world) return;
			Block b = loc.getBlock();
			if (b.getType() != blockTp) return;
			if (b.getData() == dataOuter) {
				int min = 0, max = coordsOuter.length-1;
				int random = (int) Math.floor(min + Math.random()*(max-min));
				p.teleportSync(new Location(world, coordsOuter[random][0], coordsOuter[random][1], coordsOuter[random][2]));
			} else if (b.getData() == dataInner) {
				int min = 0, max = coordsOuter.length-1;
				int random = (int) Math.floor(min + Math.random()*(max-min));
				p.teleportSync(new Location(world, coordsInner[random][0], coordsInner[random][1], coordsInner[random][2]));
			}
		}
	}
	
	
	
}
