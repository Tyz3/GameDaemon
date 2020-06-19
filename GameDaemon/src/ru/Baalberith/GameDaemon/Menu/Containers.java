package ru.Baalberith.GameDaemon.Menu;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import ru.Baalberith.GameDaemon.ConfigsDaemon;
import ru.Baalberith.GameDaemon.Menu.Elements.Boss;
import ru.Baalberith.GameDaemon.Menu.Elements.Element;
import ru.Baalberith.GameDaemon.Menu.Elements.Head;
import ru.Baalberith.GameDaemon.Menu.Elements.Icon;
import ru.Baalberith.GameDaemon.Menu.Elements.Loot;
import ru.Baalberith.GameDaemon.Menu.Elements.Mob;
import ru.Baalberith.GameDaemon.Menu.Elements.NextWay;
import ru.Baalberith.GameDaemon.Menu.Elements.Element.ElementType;
import ru.Baalberith.GameDaemon.Utils.ItemDaemon;

public class Containers {
	
	public static String dropLoot;
	
	public static List<String> menuTitles = new ArrayList<String>();
	
	public String stagesRange;
	
	private ConfigurationSection mc;
	private ConfigurationSection c;
	public List<Container> containers = new ArrayList<Container>();
	// Названия контейнеров, которые уникальны для каждого игрока.
	public List<String> listOfIndContainer = new ArrayList<String>();
	
	public void reload() {
		c = ConfigsDaemon.mainConfig.getConfigurationSection("menu");
		mc = ConfigsDaemon.menuConfig.get();
		
		dropLoot = c.getString("dropLoot.dropChance");
		listOfIndContainer.clear();
		containers.clear();
		
		stagesRange = c.getString("stages-range", "I:1-9;II:10-24;III:25-35;IV:36-49;V:50-74;VI:75-89;VII:90-100");
		
		// Инициализация контейнеров.
		Set<String> keys = mc.getKeys(false);
		for (String k : keys) {
			try {
				// Инициализация элементов контейнера.
				List<Element> elements = new ArrayList<Element>();
				ConfigurationSection s = mc.getConfigurationSection(k+"."+Container.ELEMENTS_PATH);
				if (s == null) continue;
				Set<String> elems = mc.getConfigurationSection(k+"."+Container.ELEMENTS_PATH).getKeys(false);
				if (!mc.getBoolean(k+"."+Container.ENABLED_PATH, true)) continue;
				for (String e : elems) {
					try {
						ConfigurationSection mc_e = mc.getConfigurationSection(k+"."+Container.ELEMENTS_PATH+"."+e);
						if (mc_e == null) continue;
						if (!mc_e.getBoolean(Element.ENABLED_PATH, true)) continue;
						
						ElementType type = ElementType.valueOf(mc_e.getString(Element.TYPE_PATH, "icon"));
						if (type == null) continue;
						int position = mc_e.getInt(Element.POSITION_PATH);
						String item = mc_e.getString(Element.ITEM_PATH);
						if (item == null) continue;
						
						// Добавляем лор предмета, если он был в виде отдельного списка.
						List<String> lore = mc_e.getStringList(Element.LORE_PATH);
						if (lore != null && lore.size() > 0)
							item = item.replace("{lore}", ItemDaemon.listToString(lore, ItemDaemon.SEPARATOR));
						
						
						// Добавляем зачарования предмета, если они был в виде отдельного списка.
						List<String> enchantments = mc_e.getStringList(Element.ENCHANTMENTS_PATH);
						if (enchantments != null && enchantments.size() > 0)
							item = item.replace("{enchantments}", ItemDaemon.listToString(enchantments, ItemDaemon.SEPARATOR));
						
						ItemStack i = ItemDaemon.deSerializeItem(item);
						if (i == null) continue;
						String previousWay = mc_e.getString(Element.PREVIOUS_WAY_PATH, MenuEngine.NO_ACTION);
						
						// Определяем типы кнопок в контейнере.
						switch (type) {
							case cmds:
								elements.add(new NextWay(position, i, mc_e.getStringList(NextWay.COMMANDS_PATH), previousWay));
							break;
							case nextWay:
								elements.add(new NextWay(position, i, mc_e.getString(NextWay.NEXT_WAY_LC_PATH), mc_e.getString(NextWay.NEXT_WAY_LSC_PATH), previousWay));
							break;
							case skull:
								elements.add(new Head(position, i));
							break;
							case icon:
								elements.add(new Icon(position, i, previousWay));
							break;
							case boss:
								elements.add(new Boss(position, i, mc_e.getString(Boss.NEXT_WAY_LC_PATH), mc_e.getString(Boss.NEXT_WAY_LSC_PATH),
										previousWay, mc_e.getBoolean(Boss.IS_WORLD_BOSS_PATH), mc_e.getInt(Boss.LEVEL_PATH), mc_e.getInt(Boss.MIN_PLAYERS_PATH)));
							break;
							case mob:
								elements.add(new Mob(position, i, mc_e.getString(Mob.NEXT_WAY_LC_PATH), mc_e.getString(Mob.NEXT_WAY_LSC_PATH), previousWay, mc_e.getInt(Mob.LEVEL_PATH), mc_e.getInt(Mob.MIN_PLAYERS_PATH)));
							break;
							case loot:
								elements.add(new Loot(position, i, previousWay, mc_e.getInt(Loot.DROP_CHANCE_PATH)));
							break;
							default:
								elements.add(new Icon(position, i, previousWay));
							break;
						}
					} catch (Exception e2) {e2.printStackTrace();}
				}
				String title = mc.getString(k+"."+Container.TITLE_PATH);
				if (title == null) return;
				menuTitles.add(title.replace("&", "§"));
				boolean individual = mc.getBoolean(k+"."+Container.IS_INDIVIDUAL_PATH, false);
				
				// Добавление контейнера в общий список.
				containers.add(new Container(k, elements, title, individual));
				// Добавляем название индивидуального контейнера в отдельное место.
				if (individual) listOfIndContainer.add(k);
			} catch (Exception e1) {e1.printStackTrace();}
		}
	}
	
	// Получаем контейнер по названию. Если контейнер индивидуальный,
	// то берём его для определённого игрока.
	public Container getContainer(String player, String containerName) {
		if (containerName == null) return null;
		for (Container container : containers) {
			if (!container.getName().equalsIgnoreCase(containerName)) continue;
			if (container.isIndividual()) {
				List<Container> list = MenuEngine.inst.playersIndContainers.get(player);
				for (Container iContainer : list) {
					if (iContainer.getName().equalsIgnoreCase(containerName)) return iContainer;
				}
			} else return container;
		}
		return null;
	}
}
