package com.wd.elm;

import java.util.List;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Hopper;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class FreezeHoppers implements ICommand {

	@Override
	public String GetLongHelp() {
		return "/elm freeze <off|on> <worldguard region> Disables hoppers in the given region. Only works in loaded chunks.";
	}

	@Override
	public String GetShortHelp() {
		return "freeze <off|on> <worldguard region>   : freezes hoppers in region";
	}

	@Override
	public String GetCommand() {
		return "freeze";
	}

	private boolean HopperEmpty(Hopper hopper) {
		Inventory inv = hopper.getInventory();
		if (inv == null)
			return true;
		ItemStack[] items = inv.getContents();
		if (items == null || items.length == 0)
			return true;
		for (int i = 0; i < items.length; i++) {
			ItemStack stack = items[i];
			if (stack != null && stack.getAmount() > 0)
				return false;
		}
		return true;
	}
	
	public boolean LocationInRegion(Location l, Vector min, Vector max) {
		return ((l.getBlockX() >= min.getBlockX()) && (l.getBlockX() <= max.getBlockX()))
				&& ((l.getBlockY() >= min.getBlockY()) && (l.getBlockY() <= max.getBlockY()))
				&& ((l.getBlockZ() >= min.getBlockZ()) && (l.getBlockZ() <= max.getBlockZ()));
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public boolean Execute(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(Main.Header + "You must be a player to run this command");
			return true;
		}
		
		Player issuer = (Player)sender;
		boolean enable = false;
		if (args.length < 2) {
			sender.sendMessage(Main.Header + "You must provide <on|off> argument.");
			return true;
		}
		
		if (args[1].equalsIgnoreCase("on")) {
			enable = true;
		}
		else if (args[1].equalsIgnoreCase("off")) {
			enable = false;
		}
		else {
			sender.sendMessage(Main.Header + "Valid arguments are 'on' or 'off'");
			return true;
		}
		
		if (args.length < 3) {
			sender.sendMessage(Main.Header + "You must provide <worldguard region> argument.");
			return true;
		}
		
		WorldGuardPlugin wg = Main.WorldGuard;
		if (wg == null) {
			sender.sendMessage(Main.Header + "WG was not found. This feature requires worldguard.");
			return true;		
		}
		
		World w = issuer.getWorld();
		RegionManager rm = wg.getRegionManager(w);
		if (rm == null) {
			sender.sendMessage(Main.Header + "WG was found but is disabled in this world. This feature requires worldguard.");
			return true;		
		}
		
		ProtectedRegion region = rm.getRegion(args[2]);
		if (region == null) {
			sender.sendMessage(Main.Header + "No region called '" + args[2] + "' found.");
			return true;		
		}
		
		Vector measurements = region.getMaximumPoint().subtract(region.getMinimumPoint());
		int area = (int)(measurements.getX() * measurements.getY() * measurements.getZ());
		if (area > 50000) {
			sender.sendMessage(Main.Header + "This region has area of " + area + " and is too large. Regions must be smaller than 50k volume");
			return true;		
		}
		
		if (!region.isOwner(wg.wrapPlayer(issuer))) {
			sender.sendMessage(Main.Header + "You must be an owner of the region to toggle on/off frozen hoppers.");
			return true;				
		}
		
		// Build a list of valid hoppers
		int appliedTo = 0;
		if (enable) {
			for (Chunk chunk : w.getLoadedChunks())
			{
				for (BlockState bl : chunk.getTileEntities()) {
					Location loc = bl.getLocation();
					
					if (!LocationInRegion(loc, region.getMinimumPoint(), region.getMaximumPoint()))
						continue;
					
					if (!(bl instanceof Hopper))
						continue;
					
					Hopper hopper = (Hopper)bl;
					if (!HopperEmpty(hopper))
						continue;
					// Freeze the hopper! Or something...
					MaterialData material = hopper.getData();
					
					Block hopperBlock = hopper.getBlock();
					hopperBlock.setType(Material.PACKED_ICE);
					hopperBlock.setMetadata("frozen", new FixedMetadataValue(wg, new Integer(material.getData())));
					appliedTo++;
				}
			}	
			sender.sendMessage(Main.Header + "Froze " + appliedTo + " hoppers.");
		}
		else {
			// Check all blocks
			Vector min = region.getMinimumPoint();
			Vector max = region.getMaximumPoint();
			for (int x = min.getBlockX(); x <= max.getBlockX(); x++) {
				for (int y = min.getBlockY(); y <= max.getBlockY(); y++) {
					for (int z = min.getBlockZ(); z <= max.getBlockZ(); z++) {
						
						Block bl = w.getBlockAt(x, y, z);
						if (!bl.getType().equals(Material.PACKED_ICE))
							continue;
						if (!bl.hasMetadata("frozen"))
							continue;
						
						List<MetadataValue> values = bl.getMetadata("frozen");
						if (values.size() == 0)
							continue;
						
						Integer facing = (Integer)values.get(0).value();
						bl.setType(Material.HOPPER);
						MaterialData material = bl.getState().getData();
						material.setData(facing.byteValue());
						bl.getState().setRawData(facing.byteValue());
						bl.setData(facing.byteValue());
						appliedTo++;
					}		
				}		
			}
			sender.sendMessage(Main.Header + "Unfroze " + appliedTo + " hoppers.");
		}

		
		return true;
	}

	@Override
	public String GetRequiredPermission() {
		return "entitylagmeasure.freeze";
	}

}
