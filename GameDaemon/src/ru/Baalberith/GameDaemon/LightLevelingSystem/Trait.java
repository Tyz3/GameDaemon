package ru.Baalberith.GameDaemon.LightLevelingSystem;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import ru.Baalberith.GameDaemon.GDPlayer;

public abstract class Trait {
	
	public enum TraitType {
		BRIEF, HEALTH, DEFENSE, ATTACK, INSPIRATION;
	}
	
	protected TraitType type;
	protected int cost;
	protected int availableLevel;
	protected int threshold;
	protected boolean immutable;
	
	protected boolean autoReward;
	protected int autoRewardAmount;
	protected int autoRewardEvery;
	
	protected int position;
	protected ItemStack icon;
	protected String displayName;
	
	public Trait(TraitType type, 
			int cost, 
			int availableLevel, 
			int threshold, 
			boolean immutable,
			boolean autoReward, 
			int autoRewardAmount, 
			int autoRewardEvery,
			int position,
			ItemStack icon,
			String displayName) {
		this.type = type;
		this.cost = cost;
		this.availableLevel = availableLevel;
		this.threshold = threshold;
		this.immutable = immutable;
		this.autoReward = autoReward;
		this.autoRewardAmount = autoRewardAmount;
		this.autoRewardEvery = autoRewardEvery;
		this.position = position;
		this.icon = icon;
		this.displayName = displayName;
	}
	
	public abstract void reload(ConfigurationSection c);
	public abstract ItemStack getIcon(GDPlayer p);
	
	public String getDisplayName() {
		return displayName;
	}
	
	public int getThreshold() {
		return threshold;
	}
}
