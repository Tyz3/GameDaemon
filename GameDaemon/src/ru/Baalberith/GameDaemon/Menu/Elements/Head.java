package ru.Baalberith.GameDaemon.Menu.Elements;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import ru.Baalberith.GameDaemon.GD;
import ru.Baalberith.GameDaemon.GDPlayer;
import ru.Baalberith.GameDaemon.Menu.MenuEngine;
import ru.Baalberith.GameDaemon.Menu.Elements.Actions.Action;
import ru.Baalberith.GameDaemon.Menu.Elements.Actions.ActionNoting;
import ru.Baalberith.GameDaemon.Menu.Elements.Actions.Action.ActionType;
import ru.Baalberith.GameDaemon.Utils.ItemDaemon;
import ru.Baalberith.GameDaemon.Utils.MathOperation;

public class Head extends Element {
	private Action action;
	
	public Head(int position, ItemStack i) {
		init(position, i);
		action = new ActionNoting();
	}
	
	@Override
	public void action(Player p, ClickType clickType) {
		if (action.getActionType() == ActionType.CMDS) {
			action.action(p, clickType);
		} else {
	    	MenuEngine.inst.blockP.add(p.getName());
			action.action(p, clickType);
	    	MenuEngine.inst.blockP.remove(p.getName());
		}
	}

	
	SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
	
	@Override
	public ItemStack getItem(Player player) {
		ItemStack i = getChangedInstance();
		
		SkullMeta meta = (SkullMeta) i.getItemMeta().clone();
		if (player != null) {
			meta.setOwner(player.getName());
			List<String> newLore = new ArrayList<String>();
			GDPlayer p = GD.getGDPlayer(player);
			String time = MathOperation.makeTimeToString("{H} ч. {M} мин.", p.getPlayTime());
			String[] strs = {"{player}", player.getName(), "{dateFirstJoin}", ""+sdf.format(p.getDateFirstJoin()),
					"{dateJoin1}", sdf.format(p.getJoinDate(0)), "{dateJoin2}", sdf.format(p.getJoinDate(1)),
					"{dateJoin3}", sdf.format(p.getJoinDate(2)), "{dateQuit1}", sdf.format(p.getQuitDate(0)),
					"{dateQuit2}", sdf.format(p.getQuitDate(1)), "{dateQuit3}", sdf.format(p.getQuitDate(2)),
					"{ipJoin1}", p.getJoinIp(0), "{ipJoin2}", p.getJoinIp(1),
					"{ipJoin3}", p.getJoinIp(2), "{ipQuit1}", p.getQuitIp(0), "{ipQuit2}", p.getQuitIp(1),
					"{ipQuit3}", p.getQuitIp(2), "{joinsAmount}", ""+p.getJoinsAmount(), "{muteAmount}", ""+p.getMuteAmount(""),
					"{imprisonedAmount}", ""+p.getImprisonedAmount(), "{imprisoned}", p.isImprisoned()?"да":"нет",
					"{time}", time, "{mute}", p.checkMute()?"да":"нет", "&", "§"};
			newLore.addAll(ItemDaemon.setPlaceHolders(meta.getLore(), strs));
			meta.setLore(newLore);
		}
		i.setItemMeta(meta);
		
		return i;
	}

}
