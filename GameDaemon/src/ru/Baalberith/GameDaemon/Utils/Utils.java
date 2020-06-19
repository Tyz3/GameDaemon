package ru.Baalberith.GameDaemon.Utils;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.bukkit.enchantments.Enchantment;

public class Utils {
	
	
	/**
	 * Если reverseOrder = true - сортировка по убыванию, иначе по возрастанию.
	 */
	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map, boolean reverseOrder) {
		if (reverseOrder) {
			Map<K,V> topTen =
				    map.entrySet().stream()
				       .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
				       .collect(Collectors.toMap(
				          Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
			return topTen;
		} else {
			Map<K,V> topTen =
				    map.entrySet().stream()
				       .sorted(Map.Entry.comparingByValue())
				       .collect(Collectors.toMap(
				          Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
			return topTen;
		}
	}
	
	/**
	 * Если reverseOrder = true - сортировка по убыванию, иначе по возрастанию.
	 * @limit максимальное число элементов после сортировки.
	 */
	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map, boolean reverseOrder, int limit) {
		if (reverseOrder) {
			Map<K,V> topTen =
				    map.entrySet().stream()
				       .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
					   .limit(limit)
				       .collect(Collectors.toMap(
				          Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
			return topTen;
		} else {
			Map<K,V> topTen =
				    map.entrySet().stream()
				       .sorted(Map.Entry.comparingByValue())
					   .limit(limit)
				       .collect(Collectors.toMap(
				          Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
			return topTen;
		}
	}
	
	/**
	 * @param enchants чары предмета в виде Map.
	 * @return Возвращает чары в формате json вида: {id:0,lvl:10},{...}, ...
	 */
	public static String rawEnchants(Map<Enchantment, Integer> enchants) {
		if (enchants == null || enchants.isEmpty()) return "[]";
		StringBuilder sb = new StringBuilder();
		// {id:0,lvl:10},{...}
		sb.append("[");
		int i = 0;
		for (Entry<Enchantment, Integer> e : enchants.entrySet()) {
			sb.append("{id:");
			sb.append(e.getKey().getId());
			sb.append(",lvl:");
			sb.append(e.getValue());
			sb.append("}");
			if (i < enchants.size()-1) sb.append(",");
			i++;
		}
		sb.append("]");
		return sb.toString();
	}
	
	/**
	 * @param lore лор предмета в виде списка.
	 * @return Возвращает лор в формате json вида: [\"string1\",\"string2\", ...]
	 */
	public static String rawLore(List<String> lore) {
		if (lore == null || lore.isEmpty()) return "[]";
		StringBuilder sb = new StringBuilder();
		// "lore1","lore2"
		sb.append("[");
		for (int i = 0; i < lore.size(); i++) {
			sb.append("\\\"");
			sb.append(lore.get(i).equals("") ? " " : lore.get(i));
			sb.append("\\\"");
			if (i < lore.size()-1) sb.append(",");
		}
		sb.append("]");
		return sb.toString().replace(":", "");
	}
	
	/**
	 * @param array список строк для предмета.
	 * @return Возвращает лор в формате json вида: ["",{"text":"string1 \n string2 \n ..."}]
	 */
	public static String rawArray(List<String> array) {
		if (array == null || array.isEmpty()) return "[\"\",{\"text\":\"\"}]";
		StringBuilder sb = new StringBuilder();
		// "lore1","lore2"
		sb.append("[\"\",{\"text\":\"");
		for (int i = 0; i < array.size(); i++) {
			sb.append("§b");
			sb.append(array.get(i));
			if (i < array.size()-1) sb.append("\n");
		}
		sb.append("\"}]");
		return sb.toString();
	}

	/**
	 * @param array массив строк для предмета.
	 * @return Возвращает лор в формате json вида: ["",{"text":"string1 \n string2 \n ..."}]
	 */
	public static String rawArray(String[] array) {
		if (array == null || array.length == 0) return "[\"\",{\"text\":\"\"}]";
		StringBuilder sb = new StringBuilder();
		// "lore1","lore2"
		sb.append("[\"\",{\"text\":\"");
		for (int i = 0; i < array.length; i++) {
			sb.append("§b");
			sb.append(array[i]);
			if (i < array.length-1) sb.append("\n");
		}
		sb.append("\"}]");
		return sb.toString();
	}
}
