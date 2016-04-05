package com.wd.elm;


import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SummarizeCommand implements ICommand {

	@Override
	public String GetLongHelp() {
		return "/elm summary [all|here|worldname|-p]\n\tSummarizes the type and quantity of entities across specified world(s)\n\tSpecify 'here' for the world you're in\n\tSpecify 'all' for all worlds (this is the default)\n\tSpecify a worldname to check a specific world.\nSpecify -p to sort by player\nUse -t for tile entities.";
	}

	@Override
	public String GetShortHelp() {
		return "summary [all|here|worldname|-p]  : Summarizes entity count/type";
	}

	@Override
	public String GetCommand() {
		return "summary";
	}

	private String padding(int length) {
		String str = "";
		for (int i = 0; i < length; i++)
			str += " ";
		return str;
	}

	private class TrackedPlayer {
		public String PlayerName;
		public int TotalEntities = 0;
		public HashMap<String,Integer> EntitiesByName = new HashMap<String,Integer>();
		public HashMap<String,Integer> DampenedEntities = new HashMap<String,Integer>();
	}
	
	private void AddOneToHashmap(HashMap<String,Integer> map, String key) {
		Integer count = map.get(key);
		if (count == null) {
			map.put(key, 1);
		}
		else {
			map.put(key, count + 1);
		}	
	}
	
	// Attempts to summarize the list of entities on a world
	// returns false if nothing was interesting about this world, else true
	private boolean SummarizeByPlayer(CommandSender sender, boolean tileModeENabled) {
		HashMap<String,TrackedPlayer> entityCounts = new HashMap<String,TrackedPlayer>();
		
		sender.sendMessage(Main.Header + "Summary by player");
		
		List<World> worlds = Bukkit.getServer().getWorlds();
		for (World w : worlds)
		{
			// Guard against madness
			if (w == null)
				continue;
			
			List<Countable> entities = Main.GetEntities(w, tileModeENabled);
			// Count how many of all entities we have on this world
			for (Countable e : entities) {				
				String playerName = GriefPreventionConnector.LookupPlayerFromLocation(e.Pos);
				if (playerName.length() == 0) {
					continue;
				}
					
				
				String entityName = e.Name;
				TrackedPlayer count = entityCounts.get(playerName);
				if (count == null) {
					count = new TrackedPlayer();
					count.PlayerName = playerName;
					entityCounts.put(playerName, count);
				}
				
				count.TotalEntities++;
				
				AddOneToHashmap(count.EntitiesByName, entityName);
				if (!e.HasAi)
					AddOneToHashmap(count.DampenedEntities, entityName);
			}
		}
		
		// Now build a sorted list of players
		List<SimpleEntry<Integer,TrackedPlayer>> playerByCount = new ArrayList<SimpleEntry<Integer,TrackedPlayer>>();
		for (Entry<String,TrackedPlayer> e : entityCounts.entrySet()) {
			playerByCount.add(new SimpleEntry<Integer,TrackedPlayer>(e.getValue().TotalEntities, e.getValue()));
		}
		Collections.sort(playerByCount, new Comparator<SimpleEntry<Integer, TrackedPlayer>>() {
			public int compare(SimpleEntry<Integer, TrackedPlayer> arg0,
					SimpleEntry<Integer, TrackedPlayer> arg1) {
				return arg1.getKey().compareTo(arg0.getKey());
			}
		});
		
		// Now print out the useful information
		boolean onePlayerBad = false;
		for (int i = 0; i < playerByCount.size() && i < 5; i++) {
			SimpleEntry<Integer,TrackedPlayer> entityPair = playerByCount.get(i);
			// Skip low counts of entities
			if (entityPair.getKey() < 50)
				break;
			
			TrackedPlayer player = entityPair.getValue();
			onePlayerBad = true;
			sender.sendMessage(Main.Header + "Summary for " + ChatColor.GREEN + player.PlayerName);
			
			List<SimpleEntry<Integer,String>> entityByCount = new ArrayList<SimpleEntry<Integer,String>>();
			for (Entry<String,Integer> e : player.EntitiesByName.entrySet()) {
				entityByCount.add(new SimpleEntry<Integer,String>(e.getValue(), e.getKey()));
			}
			Collections.sort(entityByCount, new Comparator<SimpleEntry<Integer, String>>() {
				public int compare(SimpleEntry<Integer, String> arg0,
						SimpleEntry<Integer, String> arg1) {
					return arg1.getKey().compareTo(arg0.getKey());
				}
			});
			
			for (int x = 0; x < entityByCount.size() && x < 5; x++) {
				SimpleEntry<Integer,String> entity = entityByCount.get(x);
				if (entity.getKey() < 20 && x > 1)
					break;
				
				String messageHeader = "   " + entity.getKey().toString();
				
				Integer dampenedCount = player.DampenedEntities.get(entity.getValue());
				if (dampenedCount == null || dampenedCount == 0) {
					sender.sendMessage(Main.Header + ChatColor.YELLOW + messageHeader + " " + padding(16-messageHeader.length()) + entity.getValue());
				}
				else {
					sender.sendMessage(Main.Header + ChatColor.YELLOW + messageHeader + " " + padding(16-messageHeader.length()) + entity.getValue() + " (" + dampenedCount + " dampened)");
				}				
			}	
		}
		
		if (!onePlayerBad) {
			sender.sendMessage(Main.Header + "Didn't find any players owning more than 50 entities.");
		}
		
		return true;
	}
	
	// Attempts to summarize the list of entities on a world
	// returns false if nothing was interesting about this world, else true
	private boolean SummarizeWorld(CommandSender sender, boolean tileModeEnabled, World w) {
		HashMap<String,Integer> entityCounts = new HashMap<String,Integer>();
		HashMap<String,Integer> dampenedEntityCounts = new HashMap<String,Integer>();
		List<Countable> entities = Main.GetEntities(w, tileModeEnabled);
		// Count how many of all entities we have on this world
		for (Countable e : entities) {			
			String entityName = e.Name;
			AddOneToHashmap(entityCounts, entityName);
			
			if (!e.HasAi)
			{
				AddOneToHashmap(dampenedEntityCounts, entityName);			
			}
		}
		
		// Now build a sorted list of entities
		List<SimpleEntry<Integer,String>> entityByCount = new ArrayList<SimpleEntry<Integer,String>>();
		for (Entry<String,Integer> e : entityCounts.entrySet()) {
			entityByCount.add(new SimpleEntry<Integer,String>(e.getValue(), e.getKey()));
		}
		Collections.sort(entityByCount, new Comparator<SimpleEntry<Integer, String>>() {
			public int compare(SimpleEntry<Integer, String> arg0,
					SimpleEntry<Integer, String> arg1) {
				return arg1.getKey().compareTo(arg0.getKey());
			}
			
		});
		
		// Now print out the useful information
		boolean printedHeader = false;
		for (int i = 0; i < entityByCount.size() && i < 5; i++) {
			SimpleEntry<Integer,String> entityPair = entityByCount.get(i);
			// Skip low counts of entities
			if (entityPair.getKey() < 50)
				break;
			
			// Only print headers for worlds with at least one interesting thing
			if (!printedHeader) {
				printedHeader = true;
				sender.sendMessage(Main.Header + "Summary for " + ChatColor.GREEN + w.getName());
			}
			
			String messageHeader = "   " + entityPair.getKey().toString();
			
			Integer dampenedCount = dampenedEntityCounts.get(entityPair.getValue());
			if (dampenedCount == null || dampenedCount == 0) {
				sender.sendMessage(Main.Header + ChatColor.YELLOW + messageHeader + " " + padding(16-messageHeader.length()) + entityPair.getValue());
			}
			else {
				sender.sendMessage(Main.Header + ChatColor.YELLOW + messageHeader + " " + padding(16-messageHeader.length()) + entityPair.getValue() + " (" + dampenedCount + " dampened)");
			}
			
		}
		return printedHeader;
	}
	
	@Override
	public boolean Execute(CommandSender sender, String[] args) {
		boolean tileModeEnabled = Main.EnableTileMode(args);
		args = Main.RemoveTileMode(args);
		
		if (args.length > 2) {
			sender.sendMessage(Main.Header + ChatColor.RED + "too many arguments! Usage: " + GetShortHelp());
			return false;
		}
		
		if (args.length == 2) {
			if (args[1].equalsIgnoreCase("here")) {
				if (sender instanceof Player) {
					Player p = (Player)sender;
					if (!SummarizeWorld(sender, tileModeEnabled, p.getLocation().getWorld()))
						sender.sendMessage(Main.Header + "No large amounts of entities on the this world.");
					return true;
				}
				else {
					sender.sendMessage(Main.Header + ChatColor.RED + "You can't use 'here' from the console, that doesn't even make sense.");
					return false;
				}
			}
			else if (args[1].equalsIgnoreCase("all")) {
				// Do nothing, fall through to the all case
			}
			else if (args[1].equalsIgnoreCase("-p")) {
				// Special players case
				SummarizeByPlayer(sender, tileModeEnabled);
				return true;
			}
			else {
				List<World> worlds = Bukkit.getServer().getWorlds();
				for (World w : worlds)
				{
					// Guard against madness
					if (w == null)
						continue;
					
					// Check if the world name equals what they entered
					if (w.getName().equalsIgnoreCase(args[1])) {
						if (!SummarizeWorld(sender, tileModeEnabled, w))
							sender.sendMessage(Main.Header + "No large amounts of entities on the specified world.");
						return true;
					}
				}	
				sender.sendMessage(Main.Header + ChatColor.RED + "Unable to find world with that name. Double check spelling?");
				return false;
			}
		}
		
		List<World> worlds = Bukkit.getServer().getWorlds();
		boolean summarizedAtLeastOne = false;
		for (World w : worlds)
		{
			// Guard against madness
			if (w == null)
				continue;
			
			if (SummarizeWorld(sender, tileModeEnabled, w))
				summarizedAtLeastOne = true;
		}
		
		if (!summarizedAtLeastOne)
			sender.sendMessage(Main.Header + "No large amounts of entities on any world.");
		return true;
	}

	@Override
	public String GetRequiredPermission() {
		return "entitylagmeasure.summary";
	}

}
