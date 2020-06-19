package ru.Baalberith.GameDaemon.ServerLimits;

import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

import ru.Baalberith.GameDaemon.ConfigsDaemon;
import ru.Baalberith.GameDaemon.GD;
import ru.Baalberith.GameDaemon.GDPlayer;

public class BlocksLimit implements Listener {
	
	private ConfigurationSection c;
	private ConfigurationSection m;
	private Set<String> blacklist;
	
	private static String msgLimitReached;
	
	public BlocksLimit() {
		Bukkit.getPluginManager().registerEvents(this, GD.inst);
	}
	
	public void reload() {
		try {
			c = ConfigsDaemon.mainConfig.getConfigurationSection("limitation-blocks");
			m = ConfigsDaemon.messagesConfig.getConfigurationSection("limitation-blocks");
			blacklist = c.getKeys(false);
			msgLimitReached = m.getString("limit-reached").replace("&", "\u00a7");
		} catch (Exception e) {e.printStackTrace();}
	}
	
	@EventHandler
	public void onPlayerBlockPlace(BlockPlaceEvent e) {
		GDPlayer p = GD.getGDPlayer(e.getPlayer());
		if (p.isOp() || p.hasPermission("gsm.blockslimit.bypass")) return;
		
		Block b = e.getBlock();
		Material targetBlock = b.getType();
		for (String blackBlockName : blacklist) {
			
			if (blackBlockName.equalsIgnoreCase(targetBlock.toString())) {
				if (hasChunkMaxCount(b.getLocation().getChunk(), targetBlock, c.getInt(blackBlockName))) {
					e.setCancelled(true);
					p.sendMessage(msgLimitReached);
					break;
				}
			}
		}
		
		return;
	}
	
	private boolean hasChunkMaxCount(Chunk chunk, Material targetBlock, int maxCount) {
		for (int x = 0; x < 16; x++) {
			for (int y = 0; y < 256; y++) {
				for (int z = 0; z < 16; z++) {
					if (chunk.getBlock(x, y, z).getType() == targetBlock)  {
						maxCount--;
						if (maxCount == -1) return true;
					}
				}
			}
		}
		return false;
	}
}
