package ru.Baalberith.GameDaemon.Menu.Elements;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import ru.Baalberith.GameDaemon.ConfigsDaemon;
import ru.Baalberith.GameDaemon.Menu.MenuEngine;
import ru.Baalberith.GameDaemon.Menu.Elements.Actions.Action;
import ru.Baalberith.GameDaemon.Menu.Elements.Actions.ActionNextWay;
import ru.Baalberith.GameDaemon.Menu.Elements.Actions.Action.ActionType;
import ru.Baalberith.GameDaemon.Utils.MathOperation;

public class Mob extends Element {
	private Action action;
	private int level;
	private int minPlayers;
	private String stage;
	
	public final static String LEVEL_PATH = "level";
	public final static String MIN_PLAYERS_PATH = "minPlayers";
	public final static String NEXT_WAY_LC_PATH = "nextWayLC";
	public final static String NEXT_WAY_LSC_PATH = "nextWayLSC";

	// Конструктор для кнопки с предустановленным шаблоном информации.
	// displayName, lore
	public Mob(int position, ItemStack i, String nextWayLC, String nextWayLSC, String previousWay, 
			int level, int minPlayers) {
		init(position, i);
		this.level = level;
		this.minPlayers = minPlayers;
		action = new ActionNextWay(nextWayLC, nextWayLSC, previousWay);
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

	@Override
	public ItemStack getItem(Player p) {
		ItemStack i = new ItemStack(getMaterial(), getAmount(), (short)getDurability());
		i.addUnsafeEnchantments(getEnchantments());
		ItemMeta meta = i.getItemMeta();
		stage = getStage();
		List<String> list = new ArrayList<String>();
		for (String l : getLore()) {
			l = l.replace("{health}", ""+getHealth()) .replace("{strength}", ""+getStrength())
			.replace("{regen}", ""+getRegen()).replace("{stage}", ""+stage);
			list.add(l);
		}
//		setLore(list);
		meta.setLore(list);
		meta.setDisplayName(getDisplayName().replace("{level}", ""+level));
		i.setItemMeta(meta);
		return i;
	}
	
	private int getRegen() {
		String formula = ConfigsDaemon.mainConfig.get().getString("menu.formules.mob."+stage+".Regen");
		return (int) MathOperation.calcValueFromString(formula.replace("level", ""+level).replace("minPlayers", ""+minPlayers));
	}
	
	private int getStrength() {
		String formula = ConfigsDaemon.mainConfig.get().getString("menu.formules.mob."+stage+".Strength");
		return (int) MathOperation.calcValueFromString(formula.replace("level", ""+level).replace("minPlayers", ""+minPlayers));
	}
	
	private int getHealth() {
		String formula = ConfigsDaemon.mainConfig.get().getString("menu.formules.mob."+stage+".Health");
		return (int) MathOperation.calcValueFromString(formula.replace("level", ""+level).replace("minPlayers", ""+minPlayers));
	}
	
	private String getStage() {
		String[] stagesRange = MenuEngine.inst.containers.stagesRange.split(";");
		for (String stage : stagesRange) {
			if (level > Integer.parseInt(stage.split("-")[1])) continue;
			return stage.split(":")[0];
		}
 		return null;
	}
	
}
