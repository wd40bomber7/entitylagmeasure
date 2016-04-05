package com.wd.elm;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class FrozenHopperProtection implements Listener {
	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
	public void onBlockBreak(BlockBreakEvent breakEvent)
	{
		Player player = breakEvent.getPlayer();
		Block block = breakEvent.getBlock();
		
		if (!block.getType().equals(Material.PACKED_ICE))
			return;
		if (!block.hasMetadata("frozen"))
			return;
		player.sendMessage(Main.Header + ChatColor.RED + " This block is a frozen hopper. You must use /elm freeze off <wg region> to unfreeze it before it can be broken.");
		breakEvent.setCancelled(true);
	}
}
