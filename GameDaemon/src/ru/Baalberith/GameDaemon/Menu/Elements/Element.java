package ru.Baalberith.GameDaemon.Menu.Elements;

import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public abstract class Element {
	
	public enum ElementType {
		cmds, nextWay, skull, icon, boss, mob, loot, dungeon;
	}
	
//	public enum ElementPath {
//	}
	
//	protected String displayName;
//	protected List<String> lore;
//	protected Map<Enchantment, Integer> enchantments;
//	protected Material material;
//	protected int durability;
//	protected int amount;
	
	protected ItemStack item;
	
	protected int position;
	
	public final static String ENABLED_PATH = "enabled";
	public final static String TYPE_PATH = "type";
	public final static String ITEM_PATH = "item";
	public final static String LORE_PATH = "lore";
	public final static String ENCHANTMENTS_PATH = "enchantments";
	public final static String POSITION_PATH = "position";
	public final static String PREVIOUS_WAY_PATH = "previousWay";
	
	
	public abstract void action(Player p, ClickType clickType);
	public abstract ItemStack getItem(Player p);

	public void init(int position, ItemStack i) {
		item = i;
		this.position = position;
	}
	
	public String getDisplayName() {
		return item.getItemMeta().getDisplayName();
	}
	
	public List<String> getLore() {
		return item.getItemMeta().getLore();
	}
	
	public Map<Enchantment, Integer> getEnchantments() {
		return item.getEnchantments();
	}
	
	public void setItem(ItemStack i) {
		item = i;
	}
	
	public Material getMaterial() {
		return item.getType();
	}
	
	public int getAmount() {
		return item.getAmount();
	}
	
	public int getDurability() {
		return (int) item.getDurability();
	}
	
	public int getPosition() {
		return position;
	}
	
	public boolean hasLore() {
		return item.getItemMeta().hasLore();
	}
	
	public ItemStack getChangedInstance() {
		Material m = Material.matchMaterial(item.getType().name());
		
		ItemStack result = new ItemStack(m, item.getAmount(), item.getDurability());
		if (item.hasItemMeta()) {
			ItemMeta meta = item.getItemMeta().clone();
			result.setItemMeta(meta);
		}
		
		return result;
	}
	

//	public void setLore(List<String> lore) {
//		item.getItemMeta().setLore(lore);
//	}
//	
//	public void setEnchantments(Map<Enchantment, Integer> enchantments) {
//		item.addEnchantments(enchantments);
//	}
}
