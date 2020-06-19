package ru.Baalberith.GameDaemon.Menu.Builder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.NumberUtils;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.MemorySection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.google.common.collect.Lists;

import java.util.Map.Entry;

import ru.Baalberith.GameDaemon.ConfigsDaemon;
import ru.Baalberith.GameDaemon.GD;
import ru.Baalberith.GameDaemon.Menu.Container;
import ru.Baalberith.GameDaemon.Menu.Message;
import ru.Baalberith.GameDaemon.Menu.Builder.Commands.MenubuilderCMD;
import ru.Baalberith.GameDaemon.Menu.Elements.Boss;
import ru.Baalberith.GameDaemon.Menu.Elements.Element;
import ru.Baalberith.GameDaemon.Menu.Elements.Loot;
import ru.Baalberith.GameDaemon.Menu.Elements.Mob;
import ru.Baalberith.GameDaemon.Menu.Elements.NextWay;
import ru.Baalberith.GameDaemon.Menu.Elements.Element.ElementType;
import ru.Baalberith.GameDaemon.Utils.ItemDaemon;

public class Builder implements TabCompleter {
	
	public Add add;
	public Edit edit;
	public Clear clear;
	
	private static Builder inst;
	
	public Builder() {
		inst = this;
		
		GD.inst.getCommand("menubuilder").setExecutor(new MenubuilderCMD());
		GD.inst.getCommand("menubuilder").setTabCompleter(this);
		
		add = new Add();
		edit = new Edit();
		clear = new Clear();
	}
	
	// mb add container <container> <title> <isIndividual>
	// mb add element <container> <element> <type> <position> <previousWay> *держим предмет в руке*
	// mb edit container <container> <property> add/set/remove/clear <arg>
	// mb edit element <container> <element> <property> add/set/remove/clear <arg>
	// mb clear container <container>
	// mb clear element <container> <element>
	
	public static Builder getBuilder() {
		return inst;
	}

	public class Add {
		public String addContainer(String container, String title, boolean isIndividual) {
			if (ConfigsDaemon.menuConfig.get().contains(container)) return "§cНельзя добавь существующий контейнер.";
			ConfigsDaemon.menuConfig.get().set(container+"."+Container.TITLE_PATH, title);
			ConfigsDaemon.menuConfig.get().set(container+"."+Container.IS_INDIVIDUAL_PATH, isIndividual);
			ConfigsDaemon.menuConfig.get().set(container+"."+Container.ELEMENTS_PATH, new ArrayList<String>());
			ConfigsDaemon.menuConfig.get().set(container+"."+Container.ENABLED_PATH, true);
			ConfigsDaemon.menuConfig.save();
			return Message.add_container.get().replace("{container}", container);
		}
		public String addElement(String container, String element, String type, int position, String previousWay, ItemStack i, String args[]) {
			if (!ConfigsDaemon.menuConfig.get().contains(container))
				return Message.notExists_container.get().replace("{container}", container);
			if (ConfigsDaemon.menuConfig.get().contains(container+"."+Container.ELEMENTS_PATH+"."+element)) return "§cНельзя добавь существующий элемент.";
			String path = container+"."+Container.ELEMENTS_PATH+"."+element+".";
			if (i.getType() == Material.AIR) return Message.noItemInHand.get();
			
			List<String> lore = i.getItemMeta().getLore();
			
			List<String> enchs = ItemDaemon.enchantsToList(i.getEnchantments());
			i.getItemMeta().setLore(new ArrayList<String>());
			i.addEnchantments(new HashMap<Enchantment, Integer>());
			String item = null;
			int startInd = 7; // Начальный индекс для получения осташихся аргументов элемента
			
			switch (ElementType.valueOf(type)) {
				case cmds:
					if (args.length != startInd+1) return Message.help_type_button_cmds.get();
					item = ItemDaemon.serializeItem(i, true);
					if (lore != null) item = item.replace(ItemDaemon.listToString(lore, ItemDaemon.SEPARATOR), "{lore}");
					if (enchs != null) item = item.replace(ItemDaemon.listToString(enchs, ItemDaemon.SEPARATOR), "{enchantments}");
					ConfigsDaemon.menuConfig.get().set(path+Element.ITEM_PATH, item);
					ConfigsDaemon.menuConfig.get().set(path+Element.LORE_PATH, lore);
					ConfigsDaemon.menuConfig.get().set(path+Element.ENCHANTMENTS_PATH, enchs);
					ConfigsDaemon.menuConfig.get().set(path+NextWay.COMMANDS_PATH, args[startInd]);
					break;
				case nextWay:
					if (args.length != startInd+2) return Message.help_type_button_nextWay.get();
					item = ItemDaemon.serializeItem(i, true);
					if (lore != null) item = item.replace(ItemDaemon.listToString(lore, ItemDaemon.SEPARATOR), "{lore}");
					if (enchs != null) item = item.replace(ItemDaemon.listToString(enchs, ItemDaemon.SEPARATOR), "{enchantments}");
					ConfigsDaemon.menuConfig.get().set(path+Element.ITEM_PATH, item);
					ConfigsDaemon.menuConfig.get().set(path+Element.LORE_PATH, lore);
					ConfigsDaemon.menuConfig.get().set(path+Element.ENCHANTMENTS_PATH, enchs);
					ConfigsDaemon.menuConfig.get().set(path+NextWay.NEXT_WAY_LC_PATH, args[startInd]);
					ConfigsDaemon.menuConfig.get().set(path+NextWay.NEXT_WAY_LSC_PATH, args[startInd+1]);
					break;
				case skull:
					item = ItemDaemon.serializeItem(i, true);
					if (lore != null) item = item.replace(ItemDaemon.listToString(lore, ItemDaemon.SEPARATOR), "{lore}");
					if (enchs != null) item = item.replace(ItemDaemon.listToString(enchs, ItemDaemon.SEPARATOR), "{enchantments}");
					ConfigsDaemon.menuConfig.get().set(path+Element.ITEM_PATH, item);
					ConfigsDaemon.menuConfig.get().set(path+Element.LORE_PATH, lore);
					ConfigsDaemon.menuConfig.get().set(path+Element.ENCHANTMENTS_PATH, enchs);
					break;
				case icon:
					item = ItemDaemon.serializeItem(i, true);
					if (lore != null) item = item.replace(ItemDaemon.listToString(lore, ItemDaemon.SEPARATOR), "{lore}");
					if (enchs != null) item = item.replace(ItemDaemon.listToString(enchs, ItemDaemon.SEPARATOR), "{enchantments}");
					ConfigsDaemon.menuConfig.get().set(path+Element.ITEM_PATH, item);
					ConfigsDaemon.menuConfig.get().set(path+Element.LORE_PATH, lore);
					ConfigsDaemon.menuConfig.get().set(path+Element.ENCHANTMENTS_PATH, enchs);
					break;
				case boss:
					item = ItemDaemon.serializeItem(i, true);
					if (args.length != startInd+5) return Message.help_type_button_boss.get();
					if (i.getItemMeta().getDisplayName() == null) return Message.noDisplayname.get();
					ItemMeta meta1 = i.getItemMeta();
					meta1.setDisplayName(Message.bossDisplayname.get().replace("{title}", meta1.getDisplayName()));
					meta1.setLore(Arrays.asList(new String[] {"{lore}"}));
					i.setItemMeta(meta1);
					item = ItemDaemon.serializeItem(i, true);
					if (enchs != null) item = item.replace(ItemDaemon.listToString(enchs, ItemDaemon.SEPARATOR), "{enchantments}");
					ConfigsDaemon.menuConfig.get().set(path+Element.ITEM_PATH, item);
					ConfigsDaemon.menuConfig.get().set(path+Element.ENCHANTMENTS_PATH, enchs);
					ConfigsDaemon.menuConfig.get().set(path+Element.LORE_PATH, Arrays.asList(Message.bossLore.gets()));
					ConfigsDaemon.menuConfig.get().set(path+Boss.LEVEL_PATH, Integer.parseInt(args[startInd]));
					ConfigsDaemon.menuConfig.get().set(path+Boss.MIN_PLAYERS_PATH, Integer.parseInt(args[startInd+1]));
					ConfigsDaemon.menuConfig.get().set(path+Boss.NEXT_WAY_LC_PATH, args[startInd+2]);
					ConfigsDaemon.menuConfig.get().set(path+Boss.NEXT_WAY_LSC_PATH, args[startInd+3]);
					ConfigsDaemon.menuConfig.get().set(path+Boss.IS_WORLD_BOSS_PATH, Boolean.parseBoolean(args[startInd+4]));
					break;
				case mob:
					if (args.length != startInd+4) return Message.help_type_button_mob.get();
					if (i.getItemMeta().getDisplayName() == null) return Message.noDisplayname.get();
					ItemMeta meta2 = i.getItemMeta();
					meta2.setDisplayName(Message.mobDisplayname.get().replace("{title}", meta2.getDisplayName()));
					meta2.setLore(Arrays.asList(new String[] {"{lore}"}));
					i.setItemMeta(meta2);
					item = ItemDaemon.serializeItem(i, true);
					if (enchs != null) item = item.replace(ItemDaemon.listToString(enchs, ItemDaemon.SEPARATOR), "{enchantments}");
					ConfigsDaemon.menuConfig.get().set(path+Element.ITEM_PATH, item);
					ConfigsDaemon.menuConfig.get().set(path+Element.ENCHANTMENTS_PATH, enchs);
					ConfigsDaemon.menuConfig.get().set(path+Element.LORE_PATH, Arrays.asList(Message.mobLore.gets()));
					ConfigsDaemon.menuConfig.get().set(path+Mob.LEVEL_PATH, Integer.parseInt(args[startInd]));
					ConfigsDaemon.menuConfig.get().set(path+Mob.MIN_PLAYERS_PATH, Integer.parseInt(args[startInd+1]));
					ConfigsDaemon.menuConfig.get().set(path+Mob.NEXT_WAY_LC_PATH, args[startInd+2]);
					ConfigsDaemon.menuConfig.get().set(path+Mob.NEXT_WAY_LSC_PATH, args[startInd+3]);
					break;
				case loot:
					if (args.length != startInd+1) return Message.help_type_loot.get();
					ItemMeta meta3 = i.getItemMeta();
					List<String> lore3;
					if (meta3.hasLore()) {
						lore3 = meta3.getLore();
					} else {
						lore3 = new ArrayList<String>();
					}
					lore3.add("");
					lore3.add(Message.dropLoot_dropChance.get());
					meta3.setLore(lore3);
					i.setItemMeta(meta3);
					item = ItemDaemon.serializeItem(i, true);
					if (lore != null) item = item.replace(ItemDaemon.listToString(lore, ItemDaemon.SEPARATOR), "{lore}");
					if (enchs != null) item = item.replace(ItemDaemon.listToString(enchs, ItemDaemon.SEPARATOR), "{enchantments}");
					ConfigsDaemon.menuConfig.get().set(path+Element.ITEM_PATH, item);
					ConfigsDaemon.menuConfig.get().set(path+Element.LORE_PATH, lore);
					ConfigsDaemon.menuConfig.get().set(path+Element.ENCHANTMENTS_PATH, enchs);
					ConfigsDaemon.menuConfig.get().set(path+Loot.DROP_CHANCE_PATH, Integer.parseInt(args[startInd]));
					break;
			default:
				break;
			}
			ConfigsDaemon.menuConfig.get().set(path+Element.TYPE_PATH, type);
			ConfigsDaemon.menuConfig.get().set(path+Element.POSITION_PATH, position);
			ConfigsDaemon.menuConfig.get().set(path+Element.PREVIOUS_WAY_PATH, previousWay);
			ConfigsDaemon.menuConfig.get().set(path+Element.ENABLED_PATH, true);
			ConfigsDaemon.menuConfig.save();
			return Message.add_element.get().replace("{container}", container).replace("{element}", element).replace("{type}", type);
		}
		
	}
	
	public class Edit {
		// mb edit container <name> <arg> add/set/remove/clear <args>
		// mb edit element <container> <element> <arg> add/set/remove/clear <args>
		public String editContainerProperty(String container, String property, String action, String object) {
			if (!ConfigsDaemon.menuConfig.get().contains(container))
				return Message.notExists_container.get();
			String path = container+"."+property; // путь к нужному свойству
			if (!ConfigsDaemon.menuConfig.get().contains(path))
				return Message.notExists_property_container.get();
			if (action.equalsIgnoreCase("add")) {
				List<String> list = ConfigsDaemon.menuConfig.get().getStringList(path);
				String[] args = object.split(" ");
				if (args.length == 2) {
					int index = Integer.parseInt(args[0]);
					list.add(index, args[1]);
				} else list.add(object);
				ConfigsDaemon.menuConfig.get().set(path, list);
			}
			if (action.equalsIgnoreCase("set")) {
				if (object.equalsIgnoreCase("false") || object.equalsIgnoreCase("true"))
					ConfigsDaemon.menuConfig.get().set(path, Boolean.parseBoolean(object));
				else if (NumberUtils.isNumber(object))
					ConfigsDaemon.menuConfig.get().set(path, Integer.parseInt(object));
				else
					ConfigsDaemon.menuConfig.get().set(path, object);
			}
			if (action.equalsIgnoreCase("remove")) {
				List<String> list = ConfigsDaemon.menuConfig.get().getStringList(path);
				if (object.equalsIgnoreCase("end"))
					list.remove(list.size()-1);
				else list.remove(Integer.parseInt(object));
				ConfigsDaemon.menuConfig.get().set(path, list);
			}
			if (action.equalsIgnoreCase("clear")) {
				ConfigsDaemon.menuConfig.get().set(path, null);
				ConfigsDaemon.menuConfig.save();
				return Message.clear_property_container.get().replace("{container}", container).replace("{property}", property);
			}

			ConfigsDaemon.menuConfig.save();
			return Message.edit_container.get().replace("{container}", container).replace("{property}", property)
					.replace("{value}", object);
		}
		
		public String editElementProperty(String container, String element, String property, String action, String object) {
			if (!ConfigsDaemon.menuConfig.get().contains(container))
				return Message.notExists_container.get();
			String path = container+"."+Container.ELEMENTS_PATH+"."+element;
			if (!ConfigsDaemon.menuConfig.get().contains(path))
				return Message.notExists_element.get();
			path = container+"."+Container.ELEMENTS_PATH+"."+element+"."+property; // путь к нужному свойству
			if (!ConfigsDaemon.menuConfig.get().contains(path))
				return Message.notExists_property_element.get();
			
			if (action.equalsIgnoreCase("add")) {
				List<String> list = ConfigsDaemon.menuConfig.get().getStringList(path);
				list.add(object);
				ConfigsDaemon.menuConfig.get().set(path, list);
			}
			if (action.equalsIgnoreCase("set")) {
				if (object.equalsIgnoreCase("false") || object.equalsIgnoreCase("true"))
					ConfigsDaemon.menuConfig.get().set(path, Boolean.parseBoolean(object));
				else if (NumberUtils.isNumber(object))
					ConfigsDaemon.menuConfig.get().set(path, Integer.parseInt(object));
				else
					ConfigsDaemon.menuConfig.get().set(path, object);
			}
			if (action.equalsIgnoreCase("remove")) {
				List<String> list = ConfigsDaemon.menuConfig.get().getStringList(path);
				if (object.equalsIgnoreCase("end"))
					list.remove(list.size()-1);
				else list.remove(Integer.parseInt(object));
				
				ConfigsDaemon.menuConfig.get().set(path, list);
			}
			if (action.equalsIgnoreCase("clear")) {
				ConfigsDaemon.menuConfig.get().set(path, null);
				ConfigsDaemon.menuConfig.save();
				return Message.clear_property_element.get().replace("{container}", container).replace("{element}", element)
						.replace("{property}", property);
			}
			
			ConfigsDaemon.menuConfig.save();
			return Message.edit_element.get().replace("{container}", container).replace("{element}", element)
					.replace("{property}", property).replace("{value}", object);
		}
	}
	
	public class Clear {
		// mb remove element <container> <name>
		//    0      1       2           3
		public String removeContainer(String container) {
			if (!ConfigsDaemon.menuConfig.get().contains(container))
				return Message.notExists_container.get();
			ConfigsDaemon.menuConfig.get().set(container, null);
			ConfigsDaemon.menuConfig.save();
			return Message.clear_container.get().replace("{container}", container).replace("&", "\u00a7");
		}
		public String removeElement(String container, String element) {
			if (!ConfigsDaemon.menuConfig.get().contains(container))
				return Message.notExists_container.get();
			String path = container+"."+Container.ELEMENTS_PATH+"."+element; // путь к нужному свойству
			if (!ConfigsDaemon.menuConfig.get().contains(path))
				return Message.notExists_element.get();
			ConfigsDaemon.menuConfig.get().set(path, null);
			ConfigsDaemon.menuConfig.save();
			return Message.clear_element.get().replace("{container}", container).replace("{element}", element).replace("&", "\u00a7");
		}

	}
	
	
	public List<String> getHierarchy(String container, String element) {
		List<String> h = new ArrayList<String>();
		
		if (container == null) {
			Set<String> set = ConfigsDaemon.menuConfig.get().getKeys(false);
			if (set == null || set.isEmpty()) return null;
			for (String s : set) {
				h.add("§9§l> §4"+s+"§8: §0...");
			}
			return h;
		}
		if (container != null && element == null) {
			if (!ConfigsDaemon.menuConfig.get().contains(container)) return null;
			Map<String, Object> map = ConfigsDaemon.menuConfig.get().getConfigurationSection(container).getValues(false);
			if (map == null || map.isEmpty()) return null;
			h.add("§9§l> §4"+container+"§8:");
			for (Entry<String, Object> e : map.entrySet()) {
				if (e.getValue() instanceof MemorySection) {
					h.add("§9§l-> §7"+e.getKey()+"§8:");
					MemorySection values = (MemorySection) e.getValue();
					for (String s : values.getKeys(false)) h.add("§9§l--> §7"+s+"§8: §0...");
				} else {
					h.add("§9§l-> §7"+e.getKey()+"§8: §b"+e.getValue());
				}
			}
			return h;
		}
		if (container != null && element != null) {
			if (!ConfigsDaemon.menuConfig.get().contains(container)) return null;
			if (!ConfigsDaemon.menuConfig.get().contains(container+".elements."+element)) return null;
			Map<String, Object> map = ConfigsDaemon.menuConfig.get().getConfigurationSection(container+".elements."+element).getValues(false);
			if (map == null || map.isEmpty()) return null;
			h.add("§9§l> §4"+container+"§8:");
			h.add("§9§l-> §7elements§8:");
			h.add("§9§l--> §7"+element+"§8:");
			for (Entry<String, Object> e : map.entrySet()) {
				if (e.getValue() instanceof MemorySection) {
					h.add("§9§l---> §7"+e.getKey()+"§8: §0...");
					MemorySection values = (MemorySection) e.getValue();
					for (String s : values.getKeys(false)) h.add("§9§l----> §7"+s);
				} else
					h.add("§9§l---> §7"+e.getKey()+"§8: §b"+e.getValue());
			}
			return h;
		}
		
		return h;
	}

	private static final List<String> ARGS_0 =  Arrays.asList(new String[] {"add","edit","clear","show"});
	private static final List<String> ARGS_1 =  Arrays.asList(new String[] {"element","container"});
	private static final List<String> ARGS_2 =  Arrays.asList(new String[] {"add","set","remove","clear"});
	private static final List<String> BTN_TYPES =  Arrays.asList(new String[] {
			ElementType.boss.name(), 
			ElementType.cmds.name(),
			ElementType.mob.name(),
			ElementType.nextWay.name(),
			ElementType.loot.name(),
			ElementType.icon.name(),
			ElementType.skull.name()});
	
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		if (!sender.hasPermission("gsm.admin")) return null;

		if (args.length == 1) {
			return ARGS_0;
		}
		if (args.length == 2) {
			if (args[0].equalsIgnoreCase("show") || args[0].equalsIgnoreCase("s")) {
				return Lists.newArrayList(ConfigsDaemon.menuConfig.get().getKeys(false));
			}
			return ARGS_1;
		}
		// mb edit element <container> <name> 
		if (args.length == 3) {
			if (args[0].equalsIgnoreCase("show") || args[0].equalsIgnoreCase("s")) {
				if (Lists.newArrayList(ConfigsDaemon.menuConfig.get().getKeys(false)).contains(args[1])) {
					return Lists.newArrayList(ConfigsDaemon.menuConfig.get().getConfigurationSection(args[1]+".elements").getKeys(false));
				}
			}
			if (args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("a")) {
				if (args[1].equalsIgnoreCase("container") || args[1].equalsIgnoreCase("c")) {
					return new ArrayList<String>();
				}
			}
			return Lists.newArrayList(ConfigsDaemon.menuConfig.get().getKeys(false));
		}
		if (args.length == 4) {
			if (args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("a")) {
				if (args[1].equalsIgnoreCase("element") || args[1].equalsIgnoreCase("e")) {
					return new ArrayList<String>();
				}
			}
			if (args[0].equalsIgnoreCase("edit") || args[0].equalsIgnoreCase("e")) {
				if (args[1].equalsIgnoreCase("container") || args[1].equalsIgnoreCase("c")) {
					List<String> list = new ArrayList<String>();
					list.addAll(ConfigsDaemon.menuConfig.get().getConfigurationSection(args[2]).getKeys(false));
					list.remove("elements");
					return list;
				}
				if (args[1].equalsIgnoreCase("element") || args[1].equalsIgnoreCase("e")) {
					List<String> list = new ArrayList<String>();
					list.addAll(ConfigsDaemon.menuConfig.get().getConfigurationSection(args[2]).getKeys(false));
					return list;
				}
			}
			if (args[0].equalsIgnoreCase("clear") || args[0].equalsIgnoreCase("c")) {
				if (args[1].equalsIgnoreCase("container") || args[1].equalsIgnoreCase("c")) {
					List<String> list = new ArrayList<String>();
					list.addAll(ConfigsDaemon.menuConfig.get().getConfigurationSection(args[2]).getKeys(false));
					return list;
				}
				if (args[1].equalsIgnoreCase("element") || args[1].equalsIgnoreCase("e")) {
					List<String> list = new ArrayList<String>();
					list.addAll(ConfigsDaemon.menuConfig.get().getConfigurationSection(args[2]).getKeys(false));
					return list;
				}
			}
		}

		// mb edit container <container> <property> add/set/remove/clear <arg>
		// mb edit element <container> <element> <property> add/set/remove/clear <arg>
		
		if (args.length == 5) {
			if (args[0].equalsIgnoreCase("edit") || args[0].equalsIgnoreCase("e")) {
				if (args[1].equalsIgnoreCase("container") || args[1].equalsIgnoreCase("c")) {
					return ARGS_2; // add/set/remove/clear
				}
				if (args[1].equalsIgnoreCase("element") || args[1].equalsIgnoreCase("e")) {
					return BTN_TYPES;
				}
			}
			if (args[0].equalsIgnoreCase("clear") || args[0].equalsIgnoreCase("c")) {
				if (args[1].equalsIgnoreCase("container") || args[1].equalsIgnoreCase("c")) {
					List<String> list = new ArrayList<String>();
					list.addAll(ConfigsDaemon.menuConfig.get().getConfigurationSection(args[2]+"."+args[3]).getKeys(false));
					return list;
				}
				if (args[1].equalsIgnoreCase("element") || args[1].equalsIgnoreCase("e")) {
					List<String> list = new ArrayList<String>();
					list.addAll(ConfigsDaemon.menuConfig.get().getConfigurationSection(args[2]+"."+args[3]).getKeys(false));
					return list;
				}
				
			}
			
		}
		
		if (args.length == 6) {
			if (args[0].equalsIgnoreCase("edit") || args[0].equalsIgnoreCase("e")) {
				if (args[1].equalsIgnoreCase("container") || args[1].equalsIgnoreCase("c")) {
					return ARGS_2; // add/set/remove/clear
				}
				if (args[1].equalsIgnoreCase("element") || args[1].equalsIgnoreCase("e")) {
					List<String> list = new ArrayList<String>();
					list.addAll(ConfigsDaemon.menuConfig.get().getConfigurationSection(args[2]+"."+args[3]).getKeys(false));
					return list;
				}
			}
		}
		
		return new ArrayList<String>();
	}
	
	
}
