package ru.Baalberith.GameDaemon.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import ru.Baalberith.GameDaemon.Sharpening.SharpEngine;

public class ItemDaemon {
	
	public final static String SEPARATOR = "@";
	
	public static HashMap<Enchantment, Integer> combineEnchantments(Map<Enchantment, Integer> enchs1, Map<Enchantment, Integer> enchs2) {
		HashMap<Enchantment, Integer> hm = new HashMap<Enchantment, Integer>();
		hm.putAll(enchs1);
		for (Entry<Enchantment, Integer> e2 : enchs2.entrySet()) {
			if (hm.containsKey(e2.getKey()))
				hm.put(e2.getKey(), e2.getValue()+hm.get(e2.getKey()));
			else hm.put(e2.getKey(), e2.getValue());
		}
		
		return hm;
	}
	
	public static List<String> setPlaceHolders(List<String> list, String... strings) {
		List<String> newList = new ArrayList<String>();
		if (list == null) return newList;
		for (String msg : list) {
			if (strings.length % 2 == 0 && strings.length > 1) {
				for (int i = 0; i < strings.length; i+=2) {
					msg = msg.replace(strings[i], strings[i+1]);
				}
			}
			newList.add(msg);
		}
		return newList;
	}
	
	/**
	 * @param src строка в формате 'Material:durability' или 'Material'
	 * @return Возвращает ItemStack.
	 */
	public static ItemStack fromString(String src) {
		if(src == null) return null;
		Material t;
		short d;
		
		if(src.contains(":")) {
			String[] arr = src.split("\\:");
			t = Material.matchMaterial(arr[0]);
			d = Short.parseShort(arr[1]);
		} else {
			t = Material.matchMaterial(src);
			d = 0;
		}
		
		if(t == null) return null;
		return new ItemStack(t, 1, d);
	}
	
	public static String toString(ItemStack item) {
		if (item == null) return null;
		Material t = item.getType();
		short d = item.getDurability();
		
		StringBuilder result = new StringBuilder();
		result.append(t.name());
		if (d != 0) {
			result.append(":").append(d);
		}
		return result.toString();
	}
	
	public static String listToString(List<String> list, String separator) {
		if (list == null) return "null";
		if (list.isEmpty()) return "null";
		String string = "";
		
		for (int j = 0; j < list.size(); j++)
			string += list.get(j) + separator;
		return string.substring(0, string.length()-1);
	}
	
	public static List<String> stringToList(String string, String separator) {
		if (string.equalsIgnoreCase("null")) return new ArrayList<String>();
		return Arrays.asList(string.split(separator));
	}
	
	public static List<String> enchantsToList(Map<Enchantment, Integer> enchants) {
		if (enchants == null) return null;
		if (!enchants.isEmpty()) {
			List<String> list = new ArrayList<String>();
			Enchantment[] enchList = Enchantment.values();
			
			for (int i = 0; i < enchList.length; i++) {
				int power = enchants.getOrDefault(enchList[i], 0);
				if (power > 0) list.add(enchList[i].getName()+"="+power);
			}
			return list;
		} else return null;
	}
	
	public static Map<Enchantment, Integer> listToEnchanments(List<String> list) {
		if (list == null || list.isEmpty()) return null;
		Map<Enchantment, Integer> map = new HashMap<Enchantment, Integer>();
		for (String l : list) {
			try {
				String[] args = l.split("=");
				if (args.length != 2) continue;
				Enchantment e = Enchantment.getByName(args[0]);
				if (e == null) continue;
				Integer i = Integer.parseInt(args[1]);
				map.put(e, i);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return map.isEmpty() ? null : map;
	}

	// Example: IRON_HELMET;0;&bЖелезный шлем;{lore};{enchs};1
	
	// type;damage;displayname;lore1@lore2;enchant1=1@enchant2=3;amount
	
	public static String serializeItem(ItemStack item, boolean saveAir) {
		
		StringBuilder string = new StringBuilder();
		
		if (item == null && saveAir)
			item = new ItemStack(Material.AIR);
		else if (item == null && !saveAir)
			return null;
		
		// type
		string.append(item.getType().toString());
		
		// damage
		string.append(";");
		string.append(item.getDurability());
		
		if (item.hasItemMeta()) {
			ItemMeta meta = item.getItemMeta();
			
			// displayname
			string.append(";");
			if (meta.hasDisplayName())
				string.append(meta.getDisplayName());
			else
				string.append("null");
			
			// lore
			string.append(";");
			if (meta.hasLore()) {
				List<String> lore = meta.getLore();
				for (int i = 0; i < lore.size(); i++) {
					if (i > 0) string.append(SEPARATOR);
					string.append(lore.get(i));
				}
			} else {
				string.append("null");
			}
			
			// enchants
			string.append(";");
			if (item.getType() == Material.POTION) {
				PotionMeta pmeta = (PotionMeta) item.getItemMeta();
				List<PotionEffect> pEff = pmeta.getCustomEffects();
				if (pEff != null && !pEff.isEmpty()) {
					for (int i = 0; i < pEff.size(); i++) {
						int power = pEff.get(i).getAmplifier();
						int duration = pEff.get(i).getDuration();
						String name = pEff.get(i).getType().getName();
						string.append(name);
						string.append("=");
						string.append(duration);
						string.append("=");
						string.append(power);
						if (i < pEff.size()-1) string.append(SEPARATOR);
					}
				} else string.append("null");
			} else {
				Map<Enchantment, Integer> enchants = meta.getEnchants();
				
				if (!enchants.isEmpty()) {
					Enchantment[] enchList = Enchantment.values();
					
					for (int i = 0; i < enchList.length; i++) {
						int power = enchants.getOrDefault(enchList[i], 0);
						if (power > 0) {
							string.append(enchList[i].getName());
							string.append("=");
							string.append(power);
							if (enchants.size() > 1) string.append(SEPARATOR);
						}
					}
				} else string.append("null");
			}
		} else {
			string.append(";null;null;null");
		}
		
		// amount
		string.append(";");
		string.append(item.getAmount());
		
		return string.toString().replace("\u00a7", "&");
	}

	// type;damage;displayname;lore1@lore2;enchant1=1@enchant2=3;amount
	// 0   :1     :2          :3          :4                    :5
	
	public static ItemStack deSerializeItem(String string) {
		if (string == null) return new ItemStack(Material.AIR);
		
		String[] elements = string.split(";");
		
		Material m = Material.matchMaterial(elements[0]);
		if (m == null) return new ItemStack(Material.AIR);
		
		ItemStack item = new ItemStack(m, Integer.parseInt(elements[5]), Short.parseShort(elements[1]));
		
		ItemMeta iMeta = item.getItemMeta();
		
		if (!elements[2].equalsIgnoreCase("null")) {
			iMeta.setDisplayName(elements[2].replace("&", "\u00a7"));
		}
		
		if (!elements[3].equalsIgnoreCase("null")) {
			String[] lore = elements[3].split(SEPARATOR);
			List<String> iLore = new ArrayList<String>();
			for (int i = 0; i < lore.length; i++) {
				iLore.add(lore[i].replace("&", "\u00a7"));
			}
			iMeta.setLore(iLore);
		}

		item.setItemMeta(iMeta);
		
		if (!elements[4].equalsIgnoreCase("null")) {
			String[] enchantsNames = elements[4].split(SEPARATOR);
			if (m == Material.POTION) {
				PotionMeta pmeta = (PotionMeta) item.getItemMeta();
				for (int i = 0; i < enchantsNames.length; i++) {
					String[] vs = enchantsNames[i].split("=");
					pmeta.addCustomEffect(new PotionEffect(PotionEffectType.getByName(vs[0]), Integer.parseInt(vs[1]), Integer.parseInt(vs[2]), true), true);
					
				}
				item.setItemMeta(pmeta);
			} else {
				for (int i = 0; i < enchantsNames.length; i++) {
					try {
						item.addUnsafeEnchantment(Enchantment.getByName(enchantsNames[i].split("=")[0]), Integer.parseInt(enchantsNames[i].split("=")[1]));
					} catch (Exception e) { System.out.println(m.name()); }
				}
			}
		}
		
		return item;
	}
	
	public static List<PotionEffect> deSerializePotionEffect(String potion) {
		
		String[] potions = potion.split(";");
		List<PotionEffect> list = new ArrayList<PotionEffect>();
		for (String str : potions) {
			try {
				String[] args = str.split(":");
				if (args.length != 3) continue;
				list.add(new PotionEffect(PotionEffectType.getByName(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2])));
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
		}
		return list;
	}
	
	public static boolean containsLore(ItemStack item, List<String> lore) {
		if (lore == null) return false;
		if (!item.hasItemMeta() && !lore.isEmpty()) return false;
		ItemMeta meta = item.getItemMeta();
		if (!meta.hasLore() && !lore.isEmpty()) return false;
		List<String> mainLore = meta.getLore();
		int size = lore.size();
		for (String ml : mainLore) {
			for (String l : lore) {
				if (ml.equalsIgnoreCase(l)) size--;
				if (size <= 0) break;
			}
			if (size <= 0) break;
		}
		return size <= 0 ? true : false;
	}
	
	public static boolean containsLoreExact(ItemStack item, List<String> lore) {
		if (lore == null) return false;
		if (!item.hasItemMeta() && !lore.isEmpty()) return false;
		ItemMeta meta = item.getItemMeta();
		if (!meta.hasLore() && !lore.isEmpty()) return false;
		List<String> mainLore = meta.getLore();
		int size = mainLore.size();
		if (size == 0 && lore.size() == 0) return true;
		if (size != lore.size()) return false;
		for (String ml : mainLore) {
			for (String l : lore) {
				if (SharpEngine.checkEnhancedString(ml)) size --;
				else if (ml.equalsIgnoreCase(l)) size--;
				if (size <= 0) break;
			}
			if (size <= 0) break;
		}
		return size <= 0 ? true : false;
	}
	
	public static boolean containsEnchants(ItemStack item, Map<Enchantment, Integer> enchants) {
		if (enchants == null) return false;
		Map<Enchantment, Integer> encs = item.getEnchantments();
		if (encs == null) return false;
		int size = enchants.size();
		if (encs.size() == 0 && size == 0) return true;
		for (Entry<Enchantment, Integer> e1 : encs.entrySet()) {
			for (Entry<Enchantment, Integer> e2 : enchants.entrySet()) {
				if (!e1.getKey().getName().equalsIgnoreCase(e2.getKey().getName())) continue;
				if (e1.getValue() != e2.getValue()) continue;
				size--;
				if (size <= 0) break;
			}
			if (size <= 0) break;
		}
		return size <= 0 ? true : false;
	}
	
	public static boolean containsEnchantsExact(ItemStack item, Map<Enchantment, Integer> enchants) {
		if (enchants == null) return false;
		Map<Enchantment, Integer> encs = item.getEnchantments();
		if (encs == null) return false;
		int size = encs.size();
		if (size == 0 && enchants.size() == 0) return true;
		if (size != enchants.size()) return false;
		for (Entry<Enchantment, Integer> e1 : encs.entrySet()) {
			for (Entry<Enchantment, Integer> e2 : enchants.entrySet()) {
				if (!e1.getKey().getName().equalsIgnoreCase(e2.getKey().getName())) continue;
				if (e1.getValue() != e2.getValue()) continue;
				size--;
				if (size <= 0) break;
			}
			if (size <= 0) break;
		}
		return size <= 0 ? true : false;
	}
	
	public static void addLore(ItemStack item, List<String> lore) {
		ItemMeta meta = item.getItemMeta();
		if (meta.hasLore()) {
			List<String> mainLore = meta.getLore();
			mainLore.addAll(lore);
			meta.setLore(mainLore);
		} else meta.setLore(lore);
		item.setItemMeta(meta);
	}
	
	public static void setEnchantments(ItemStack item, Map<Enchantment, Integer> enchantments) {
		Map<Enchantment, Integer> es = item.getEnchantments();
		if (es == null || es.isEmpty()) {
			item.addUnsafeEnchantments(enchantments);
		} else {
			es = new HashMap<Enchantment, Integer>();
			es.putAll(enchantments);
		}
		item.addUnsafeEnchantments(es);
	}
	
	public static void setColorCodes(List<String> list) {
		if (list == null || list.isEmpty()) return;
		for (int i = 0; i < list.size(); i++) {
			list.set(i, list.get(i).replace("&", "§"));
		}
	}
}
