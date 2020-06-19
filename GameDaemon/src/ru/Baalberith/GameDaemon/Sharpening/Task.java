package ru.Baalberith.GameDaemon.Sharpening;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import ru.Baalberith.GameDaemon.ConfigsDaemon;
import ru.Baalberith.GameDaemon.GD;
import ru.Baalberith.GameDaemon.GDPlayer;
import ru.Baalberith.GameDaemon.Utils.ItemDaemon;
import ru.Baalberith.GameDaemon.Utils.MathOperation;
import ru.Baalberith.GameDaemon.Utils.Utils;

public class Task implements Runnable {
	
	private Inventory inv;
	private GDPlayer p;
	
	public Task(GDPlayer p) {
		this.p = p;
		this.inv = Bukkit.createInventory(p.getBukkitPlayer(), 9, SharpEngine.sharpTitle);
		for (int i = 1; i < 8; i++) this.inv.setItem(i, SharpEngine.DEFAULT_SLOT);
		if (p.hasSharpeningInv()) {
			ItemStack[] contents = p.takeSharpeningInv();
			inv.setItem(0, contents[0]);
			inv.setItem(4, contents[1]);
			inv.setItem(8, contents[2]);
		}
	}
	
	public void openInventory() {
		p.p.openInventory(inv);
	}
	
	public void saveInventory() {
		ItemStack[] contents = new ItemStack[3];
		ItemStack left = inv.getItem(0);
		ItemStack res = inv.getItem(4);
		ItemStack right = inv.getItem(8);
		if (left == null && right == null && (res == null || res.getType() == SharpEngine.DEFAULT_SLOT.getType())) return;
		contents[0] = inv.getItem(0);
		contents[1] = inv.getItem(4);
		contents[2] = inv.getItem(8);
		p.saveSharpeningInv(contents);
	}
	
	int stage = 1;
	
	@Override
	public void run() {
		if (!canProcess()) {
			if (stage != 1) {
				for (int i = 1; i < 4; i++) this.inv.setItem(i, SharpEngine.DEFAULT_SLOT);
				for (int i = 5; i < 8; i++) this.inv.setItem(i, SharpEngine.DEFAULT_SLOT);
			}
			if (inv.getItem(4) == null || inv.getItem(4).equals(SharpEngine.DEFAULT_SLOT)) {
				inv.setItem(4, SharpEngine.DEFAULT_SLOT);
			}
			stage = 1;
			return;
		}
		
		
		// i=+++++=i
		if (stage == 1) {
			inv.setItem(1, SharpEngine.PROGRESS_SLOT);
			inv.setItem(7, SharpEngine.PROGRESS_SLOT);
			stage = 2;
			return;
		}
		
		// i==+++==i
		if (stage == 2) {
			inv.setItem(2, SharpEngine.PROGRESS_SLOT);
			inv.setItem(6, SharpEngine.PROGRESS_SLOT);
			stage = 3;
			return;
		}
		
		// i===+===i
		if (stage == 3) {
			inv.setItem(3, SharpEngine.PROGRESS_SLOT);
			inv.setItem(5, SharpEngine.PROGRESS_SLOT);
			stage = 4;
			return;
		}
		
		// i===O===i
		if (stage == 4) {
			ItemStack i1 = inv.getItem(0);
			ItemStack i2 = inv.getItem(8);

			p.takeLevel(levelsPay);
			if (isMaterial(i1)) {
				reduceAmount(i1, 0);
				inv.setItem(8, ConfigsDaemon.EMPTY_ITEM);
				if (!tryChance(chance)) {
					damageItem(i2);
					p.addSharpFailureAmount(1);
					failure();
				} else {
					replaceLore(i2);
					i2.addUnsafeEnchantments(ItemDaemon.combineEnchantments(i1.getEnchantments(), i2.getEnchantments()));
					p.addSharpSuccessAmount(1);
					success();
					broadcast(i2);
				}
				
				inv.setItem(4, i2);
			} else {
				reduceAmount(i2, 8);
				inv.setItem(0, ConfigsDaemon.EMPTY_ITEM);
				if (!tryChance(chance)) {
					damageItem(i1);
					p.addSharpFailureAmount(1);
					failure();
				} else {
					replaceLore(i1);
					i1.addUnsafeEnchantments(ItemDaemon.combineEnchantments(i1.getEnchantments(), i2.getEnchantments()));
					p.addSharpSuccessAmount(1);
					success();
					broadcast(i1);
				}
				
				inv.setItem(4, i1);
			}
			for (int i = 1; i < 4; i++) this.inv.setItem(i, SharpEngine.DEFAULT_SLOT);
			for (int i = 5; i < 8; i++) this.inv.setItem(i, SharpEngine.DEFAULT_SLOT);
			stage = 1;
			return;
		}
	}
	
	private boolean isMaterial(ItemStack i) {
		return SharpEngine.materials.contains(i.getType());
	}
	
	private int levelsPay = 0;
	private double chance = 0.0;
	private String targetString;
	
	private void broadcast(ItemStack i) {
		if (chance <= SharpEngine.minChanceToBroadcast) GD.sendMessageForAll(SharpEngine.msgBroadcast.replace("{player}", p.getName())
				.replace("{chance}", String.valueOf(chance))
				.replace("{name}", String.valueOf(i.getTypeId()))
				.replace("{lore}", Utils.rawLore(i.getItemMeta().getLore()))
				.replace("{enchantments}", Utils.rawEnchants(i.getEnchantments())));
	}
	
	private void success() {
		p.sendMessage(SharpEngine.msgSuccess);
		p.playSound(Sound.ANVIL_USE, 2, 2);
	}
	
	private void failure() {
		p.sendMessage(SharpEngine.msgFailure);
		p.playSound(Sound.ITEM_BREAK, 2, 2);
	}
	
	private void damageItem(ItemStack i) {
		short damage = (short) (SharpEngine.MIN_DUR_LOST + Math.random()*(SharpEngine.MAX_DUR_LOST - SharpEngine.MIN_DUR_LOST));
		i.setDurability((short) (i.getDurability()+damage));
	}
	
	private boolean tryChance(double chance) {
		double effort = 1+Math.random()*99;
		if (effort <= chance) {
			return true;
		} else return false;
	}
	
	private void reduceAmount(ItemStack i, int pos) {
		if (i.getAmount() != 1)
			i.setAmount(i.getAmount()-1);
		else inv.setItem(pos, ConfigsDaemon.EMPTY_ITEM);
	}
	
	private void replaceLore(ItemStack i) {
		ItemMeta meta = i.getItemMeta();
		List<String> lore = meta.getLore();
		for (int j = 0; j < lore.size(); j++) {
			if (lore.get(j).equalsIgnoreCase(targetString)) {
				lore.set(j, SharpEngine.loreToAdd.replace("{percentage}", String.valueOf(MathOperation.roundAvoid(chance/2, 2))));
				break;
			}
		}
		meta.setLore(lore);
		i.setItemMeta(meta);
	}
	
	private boolean canProcess() {
		if (inv.getItem(4) == null) return false;
		if (inv.getItem(4).getType() != SharpEngine.DEFAULT_SLOT.getType() && inv.getItem(4).getDurability() != SharpEngine.DEFAULT_SLOT.getDurability()) return false;
		ItemStack i1 = inv.getItem(0);
		ItemStack i2 = inv.getItem(8);
		if (i1 == null || i2 == null) return false;
		
		targetString = SharpEngine.getEnhancedString(i2);
		if (SharpEngine.materials.contains(i1.getType()) && targetString != null) {
			chance = SharpEngine.inst.getChanceFromString(targetString);
			levelsPay = SharpEngine.inst.getLevelsPay(chance);
			if (p.hasLevel(levelsPay)) {
				return true;
			} else
				p.sendMessage(SharpEngine.msgRequiresLvl.replace("{lvl}", String.valueOf(levelsPay)));
		}
		
		targetString = SharpEngine.getEnhancedString(i1);
		if (SharpEngine.materials.contains(i2.getType()) && targetString != null) {
			chance = SharpEngine.inst.getChanceFromString(targetString);
			levelsPay = SharpEngine.inst.getLevelsPay(chance);
			if (p.hasLevel(levelsPay)) {
				return true;
			} else
				p.sendMessage(SharpEngine.msgRequiresLvl.replace("{lvl}", String.valueOf(levelsPay)));
		}
		
		return false;
	}
}
