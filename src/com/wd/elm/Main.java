package com.wd.elm;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

public class Main extends JavaPlugin {
	public static final String Header = ChatColor.GREEN + "[ELM] " + ChatColor.GRAY;
	public static final String Version = "0.1.0";
	public static WorldGuardPlugin WorldGuard;
	public static JavaPlugin ThisPlugin;

	public void onEnable() {
		ThisPlugin = this;
		WorldGuard = getWorldGuard();
		
		Commands commandController = new Commands();
		
		getServer().getPluginManager().registerEvents(commandController, this);
		getServer().getPluginManager().registerEvents(new FrozenHopperProtection(), this);
		this.getCommand("elm").setExecutor(commandController);
		this.getCommand("entitylagmeasure").setExecutor(commandController);
	}

	public static boolean SkipEntity(Entity e) {
		if (e == null)
			return true;
		if (e.getName().equalsIgnoreCase("entity.ItemFrame.name"))
			return true;
		if (e.getName().equalsIgnoreCase("Painting"))
			return true;
		return false;
	}
	
	public static boolean SkipTileEntity(String name) {
		if (name == null)
			return true;
		if (name.equalsIgnoreCase("CHEST"))
			return true;
		if (name.equalsIgnoreCase("WALL_SIGN"))
			return true;
		if (name.equalsIgnoreCase("TRAPPED_CHEST"))
			return true;
		return false;
	}
	
	private WorldGuardPlugin getWorldGuard() {
	    Plugin plugin = getServer().getPluginManager().getPlugin("WorldGuard");
	 
	    // WorldGuard may not be loaded
	    if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
	        return null; // Maybe you want throw an exception instead
	    }
	 
	    return (WorldGuardPlugin) plugin;
	}
	
	// Most inefficient fuctions ever. I hate this design
	public static boolean EnableTileMode(String args[]) {
		for (String s : args) {
			if (s.equalsIgnoreCase("-t")) {
				return true;
			}
		}
		return false;
	}
	public static String[] RemoveTileMode(String args[]) {
		ArrayList<String> newFlags = new ArrayList<String>();
		for (String s : args) {
			if (!s.equalsIgnoreCase("-t")) {
				newFlags.add(s);
			}
		}
		return newFlags.toArray(new String[newFlags.size()]);
	}
	public static List<Countable> GetEntities(World w, boolean tile) {
		ArrayList<Countable> tileEntities = new ArrayList<Countable>();
		if (tile) {
			for (Chunk chunk : w.getLoadedChunks())
			{
				for (BlockState bl : chunk.getTileEntities()) {
					if (SkipTileEntity(bl.getType().name()))
						continue;
					tileEntities.add(new Countable(bl.getBlock()));
				}
			}		
		}
		else {
			for (Entity e : w.getEntities()) {
				if (SkipEntity(e))
					continue;
				tileEntities.add(new Countable(e));
			}
		}

		return tileEntities;
	}
	
	
}
