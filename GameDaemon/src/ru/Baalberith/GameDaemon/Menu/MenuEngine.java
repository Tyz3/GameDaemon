package ru.Baalberith.GameDaemon.Menu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import ru.Baalberith.GameDaemon.ConfigsDaemon;
import ru.Baalberith.GameDaemon.GD;
import ru.Baalberith.GameDaemon.GDPlayer;
import ru.Baalberith.GameDaemon.Menu.Builder.Builder;
import ru.Baalberith.GameDaemon.Menu.Commands.MenuCMD;
import ru.Baalberith.GameDaemon.Menu.Elements.Element;

public class MenuEngine {

	public static MenuEngine inst;
	public final static String MAIN_MENU_NAME = "main";
	public final static String NO_ACTION = "noAction";
	
	private MenuHandler menuHandler = new MenuHandler();
	
	public Map<String, String> menuHolders = new HashMap<String, String>();
	public Map<String, List<Container>> playersIndContainers = new HashMap<String, List<Container>>();
	public Containers containers = new Containers();
	public List<String> blockP = new ArrayList<String>();
	public Builder builder = new Builder();
	
	public MenuEngine() {
		inst = this;
		
		// Регистрация команды меню.
		GD.inst.getCommand("menu").setExecutor(new MenuCMD());
	}
	
	public void reload() {
		try {
			ConfigurationSection m = ConfigsDaemon.messagesConfig.getConfigurationSection("menu.builder");
			Message.load(m, m.getString("label"));
			
			// Закрытие инвентарей у игроков и отчистка списка холдеров.
			for (Entry<String, String> e : menuHolders.entrySet()) {
				GDPlayer p = GD.getGDPlayer(e.getKey());
				p.p.closeInventory();
				menuHolders.remove(e.getKey());
			}
			blockP.clear();
			playersIndContainers.clear();
			
			// Сначала релоад контейнеров.
			containers.reload();
			for (GDPlayer p : GD.online) {
				menuHandler._onJoin(p.getBukkitPlayer());
			}
		} catch (Exception e) {e.printStackTrace();}
	}
	
	// Нужен для получения контейнера из общего списка и с
	// последующей его уникализацией для конкретного игрока.
	public Container getIndividualContainer(Player p, String name) {
		for (Container c : containers.containers) {
			if (c.getName().equalsIgnoreCase(name)) {
				Container new_c = new Container(c.getName(), c.getElements(), c.getTitle(), c.isIndividual());
				return new_c.proceedIndividual(p); // Уникализация
			}
		}
		return null;
	}
	
	public Element getElementByPosition(Container container, int position) {
    	List<Element> list = container.getElements();
    	for (Element e : list)
			if (e.getPosition() == position) return e;
		return null;
	}
}
