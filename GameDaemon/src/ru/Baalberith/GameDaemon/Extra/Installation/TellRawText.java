package ru.Baalberith.GameDaemon.Extra.Installation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import ru.Baalberith.GameDaemon.GDSender;
import ru.Baalberith.GameDaemon.Utils.Utils;

public class TellRawText {

	public enum Color {
		black, dark_blue, dark_green, dark_aqua,
		dark_red, dark_purple, gold, gray,
		dark_gray, blue, green, aqua, 
		red, light_purple, yellow, white;
	}
	public enum Style { bold, italic, underlined, strikethrough, obfuscated; }
	public enum ClickEvent { open_url, run_command, suggest_command; }
	
	private static final Line blankLine = new TellRawText().new Line(
			new TellRawText().new Element().setText(""));
	private static final Line separatorLine1 = new TellRawText().new Line(
			new TellRawText().new Element().setText("=====================================================").setColor(Color.black));
	private static final Line separatorLine2 = new TellRawText().new Line(
			new TellRawText().new Element().setText("-----------------------------------------------------").setColor(Color.dark_gray));
	
	
	private List<Line> lines = new ArrayList<>();
	
	public void clear() {
		lines.stream().forEach(l -> l.clear());
		lines.clear();
	}
	
	public void addLine(Line textLine) {
		lines.add(textLine);
	}
	
	public void addBlankLine() {
		lines.add(blankLine);
	}
	
	public void addSeparator1() {
		lines.add(separatorLine1);
	}
	
	public void addSeparator2() {
		lines.add(separatorLine2);
	}
	
	public List<Line> getLines() {
		return lines;
	}
	
	public void send(GDSender s, String... args) {
		getLines().stream().forEach(l -> s.sendMessage("json: "+l.toString(), args));
	}
	
	public Line createLine(Element... elements) {
		return new Line(elements);
	}
	
	public Element createElement() {
		return new Element();
	}
	
	public class Line {
		private List<Element> elements = new ArrayList<>();
		
		public Line(Element... elements) {
			for (Element element : elements) {
				this.elements.add(element);
			}
		}
		
		public void clear() {
			elements.stream().forEach(e -> e.clear());
		}
		
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("[\"\"");
			for (Element e : elements) {
				sb.append(",");
				sb.append(e.toString());
			}
			return sb.append("]").toString();
		}
	}
	
	public class Element {
		private HashMap<String, String> parts = new HashMap<String, String>();
		
		public void clear() {
			parts.clear();
		}
		
		public Element setText(String text) {
			parts.put("\"text\"", "\"".concat(text.replace("&", "§")).concat("\""));
			return this;
		}
		
		public Element setColor(Color color) {
			parts.put("\"color\"", "\"".concat(color.name()).concat("\""));
			return this;
		}
		
		public Element setStyle(Style style) {
			parts.put("\"".concat(style.name()).concat("\""), "\"true\"");
			return this;
		}
		
		public Element setClickEvent(ClickEvent clickEvent, String text) {
			parts.put("\"clickEvent\"", "{\"action\":\"".concat(clickEvent.name()).concat("\",\"value\":\"").concat(text).concat("\"}"));
			return this;
		}
		
		public Element setTip(String text) {
			String[] args = text.split("\\n");
			if (args.length > 1) text = Utils.rawArray(Arrays.asList(args));
			parts.put("\"hoverEvent\"", "{\"action\":\"show_text\",\"value\":\"".concat(text.replace("&", "§")).concat("\"}"));
			return this;
		}
		
		public Element setTip(ItemStack item, boolean useDisplayName) {
			// "action":"show_item","value":"{id:{name},Damage:{data},tag:{display:{Name:\"{displayName}\",Lore:{lore}}, ench:{enchantments}}}"
			String value = null;
			ItemMeta meta = item.getItemMeta();
			if (useDisplayName && meta.hasDisplayName()) {
				value = "{id:{id},Damage:{data},tag:{display:{Name:\\\"{displayName}\\\",Lore:{lore}},ench:{enchantments}}}";
			} else value = "{id:{id},Damage:{data},tag:{display:{Lore:{lore}},ench:{enchantments}}}";
			
			if (useDisplayName && meta.hasDisplayName()) value = value.replace("{displayName}", meta.getDisplayName());
			value = value.replace("{id}", String.valueOf(item.getTypeId()))
					.replace("{data}", String.valueOf(item.getDurability()))
					.replace("{lore}", Utils.rawLore(meta.getLore()))
					.replace("{enchantments}", Utils.rawEnchants(meta.getEnchants()));
			parts.put("\"hoverEvent\"", "{\"action\":\"show_item\",\"value\":\"".concat(value).concat("\"}"));
			return this;
		}
		
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("{");
			int size = parts.entrySet().size();
			for (Entry<String, String> part : parts.entrySet()) {
				sb.append(part.getKey()).append(":").append(part.getValue());
				if (size > 1) sb.append(",");
				size--;
			}
			return sb.append("}").toString();
		}
	}
}
