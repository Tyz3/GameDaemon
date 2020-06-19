package ru.Baalberith.GameDaemon.Menu;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import ru.Baalberith.GameDaemon.Menu.Elements.Element;

public class Container {
	
	private Inventory container;
	private boolean individual;
	private List<Element> elements;
	private String title;
	private int containerSize;
	private String name;

	public final static String ELEMENTS_PATH = "elements";
	public final static String ENABLED_PATH = "enabled";
	public final static String IS_INDIVIDUAL_PATH = "isIndividual";
	public final static String TITLE_PATH = "title";
	
	public Container(String name, List<Element> elements, String title, boolean individual) {
		this.name = name;
		this.elements = elements;
		this.individual = individual;
		this.title = title.replace("&", "\u00a7");
		this.containerSize = calcContainerSize(elements.size());
		container = Bukkit.createInventory(null, this.containerSize, this.title);
		
		// Добавляем елементы контейнера в него.
		for (Element e : elements) {
			container.setItem(e.getPosition(), e.getItem(null));
		}
	}
	
	// Метод индивидуализации контейнера, если тот является индивидуальным.
	public Container proceedIndividual(Player p) {
		for (int i = 0; i < elements.size(); i++) {
			Element e = elements.get(i);
			
			e.setItem(e.getChangedInstance());
			elements.set(i, e);
			container.setItem(e.getPosition(), e.getItem(p).clone());
		}
		return this;
	}
	
	public boolean equals(Container c) {
		return c.getName().equalsIgnoreCase(name) ? true : false;
	}
	
	public String getName() {
		return name;
	}

	public List<Element> getElements() {
		return elements;
	}

	public void setElements(List<Element> elements) {
		this.elements = elements;
	}

	public boolean isIndividual() {
		return individual;
	}
	
	private int calcContainerSize(int items) {
		return (int) Math.ceil((float)items/9) * 9;
	}

	public Inventory getContainer() {
		return container;
	}
	
	public int getSize() {
		return this.containerSize;
	}
	
	public String getTitle() {
		return this.title;
	}
	
}
