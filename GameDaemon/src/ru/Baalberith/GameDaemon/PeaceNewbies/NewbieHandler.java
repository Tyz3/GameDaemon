package ru.Baalberith.GameDaemon.PeaceNewbies;

import java.util.Date;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import ru.Baalberith.GameDaemon.Utils.MathOperation;
import ru.Baalberith.GameDaemon.Utils.ThreadDaemon;


public class NewbieHandler implements Listener {
	
	private static final short[] potionData = {
			16385,16417,16449,16386,16418,
			16450,16419,16451,16388,16420,
			16452,16453,16421,16422,16454,
			16424,16456,16393,16425,16457,
			16426,16458,16460,16428,16429,
			16461,16430,16462};
	
	// Добавление игроку режима новичка происходит в классе Statistics.StatEngine
	
    @EventHandler
    public void PlayerJoin(PlayerJoinEvent e) {   
        ThreadDaemon.syncLater(() -> {
    		long time1 = new Date().getTime();
        	Player p = e.getPlayer();
    		Newbie newbie = Newbies.inst.getNewbie(p.getName());
        	if (newbie == null) return;
    		long time = new Date(newbie.getResidual()).getTime();
			String msg = MathOperation.makeTimeToString(Newbies.MSG_JOIN, time)
				.replace("{player}", newbie.getName());
    		p.sendMessage(msg);
    		long time2 = new Date().getTime() - time1;
    		Bukkit.getLogger().info("PeaceNewbies.NewbieHandler.PlayerJoin-later("+p.getName()+"), ["+time2+" ms]");
        }, Task.TASK_TIMER);
    }
    
	@EventHandler(priority = EventPriority.HIGH)
	public void onDamage(EntityDamageByEntityEvent e) {
		if (!(e.getEntity() instanceof Player)) return;
		long time1 = new Date().getTime();
		if (e.getDamager() instanceof Arrow) {
			Arrow a = (Arrow) e.getDamager();
			if (a.getShooter() instanceof Player)  {
				Player s = (Player) a.getShooter();
				Player p = (Player) e.getEntity();
				if (Newbies.inst.getNewbie(p.getName()) != null || Newbies.inst.getNewbie(s.getName()) != null) {
					e.setCancelled(true);
				}
				long time2 = new Date().getTime() - time1;
				Bukkit.getLogger().info("PeaceNewbies.NewbieHandler.onDamage("+p.getName()+"), ["+time2+" ms]");
			}
		}
		if (e.getDamager() instanceof Player) {
			Player d = (Player) e.getDamager();
			Player p = (Player) e.getEntity();
			if (Newbies.inst.getNewbie(d.getName()) != null || Newbies.inst.getNewbie(p.getName()) != null) {
				d.sendMessage(Newbies.MSG_DAMAGE_BLOCK);
				e.setCancelled(true);
				long time2 = new Date().getTime() - time1;
				Bukkit.getLogger().info("PeaceNewbies.NewbieHandler.onDamage("+p.getName()+"), ["+time2+" ms]");
			}
		}
		
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onPotionInteract(PlayerInteractEvent e) {
		Action a = e.getAction();
		if (!(a == Action.RIGHT_CLICK_AIR || a == Action.RIGHT_CLICK_BLOCK)) return;
		Player p = e.getPlayer();
		if (e.getItem() == null) return;
		long time1 = new Date().getTime();
		if (!e.getItem().getType().equals(Material.POTION)) return;
		if (Newbies.inst.getNewbie(p.getName()) == null) return;
		if (p.getWorld().getName().equalsIgnoreCase("dungeonworld")) return;
		if (!isExplosive(e.getItem().getDurability())) return;
		e.setCancelled(true);
		p.updateInventory();
		p.sendMessage(Newbies.MSG_POTION_BLOCK);
		long time2 = new Date().getTime() - time1;
		Bukkit.getLogger().info("PeaceNewbies.NewbieHandler.onPotionInteract("+p.getName()+"), ["+time2+" ms]");
	}

	@EventHandler(priority = EventPriority.NORMAL)
    public void onEntityDamage(EntityDamageEvent e) {
    	if (!(e.getEntity() instanceof Player)) return;
		long time1 = new Date().getTime();
    	Player p = (Player) e.getEntity();
    	if (Newbies.inst.getNewbie(p.getName()) == null) return;
    	if (e.getCause().equals(DamageCause.POISON) || 
    		e.getCause().equals(DamageCause.MAGIC) || 
    		e.getCause().equals(DamageCause.WITHER)) {
    		e.setCancelled(true);
			long time2 = new Date().getTime() - time1;
			Bukkit.getLogger().info("PeaceNewbies.NewbieHandler.onEntityDamage("+p.getName()+"), ["+time2+" ms]");
    	}
    }
	
	private boolean isExplosive(short dur) {
		for (short s : potionData)
			if (s == dur) return true;
		return false;
	}
}
