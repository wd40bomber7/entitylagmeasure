package com.wd.elm;


import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class SummarizeCommand implements ICommand {

	@Override
	public String GetLongHelp() {
		return "/elm summary [all|here|worldname]\n\tSummarizes the type and quantity of entities across specified world(s)\n\tSpecify 'here' for the world you're in\n\tSpecify 'all' for all worlds (this is the default)\n\tSpecify a worldname to check a specific world";
	}

	@Override
	public String GetShortHelp() {
		return "summary [all|here|worldname]   : Summarizes entity count/type";
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
	
	private void SummarizeWorld(CommandSender sender, World w) {
		HashMap<String,Integer> entityCounts = new HashMap<String,Integer>();
		List<Entity> entities = w.getEntities();
		// Count how many of all entities we have on this world
		for (Entity e : entities) {
			String entityName = e.getName();
			Integer count = entityCounts.get(entityName);
			if (count == null) {
				entityCounts.put(entityName, 1);
			}
			else {
				entityCounts.put(entityName, count + 1);
			}
		}
		
		// Now build a sorted list of entities
		List<SimpleEntry<Integer,String>> entityByCount = new ArrayList<SimpleEntry<Integer,String>>();
		for (Entry<String,Integer> e : entityCounts.entrySet()) {
			entityByCount.add(new SimpleEntry<Integer,String>(e.getValue(), e.getKey()));
		}
		entityByCount.sort(new Comparator<SimpleEntry<Integer, String>>() {

			public int compare(SimpleEntry<Integer, String> arg0,
					SimpleEntry<Integer, String> arg1) {
				return arg1.getKey().compareTo(arg0.getKey());
			}
			
		});
		
		// Now print out the useful information
		sender.sendMessage(Main.Header + "Summary for " + ChatColor.GREEN + w.getName());
		int printedEntries = 0;
		for (int i = 0; i < entityByCount.size() && i < 5; i++) {
			SimpleEntry<Integer,String> entityPair = entityByCount.get(i);
			// Skip low counts of entities
			//if (entityPair.getKey() < 50)
			//	break;
			String messageHeader = "   " + entityPair.getKey().toString();
			sender.sendMessage(Main.Header + messageHeader + " " + padding(16-messageHeader.length()) + entityPair.getValue());
			printedEntries++;
		}
		if (printedEntries <= 0) {
			sender.sendMessage(Main.Header + "   " + "No meaningful quantities of entities.");
		}
	}
	
	@Override
	public boolean Execute(CommandSender sender, String[] args) {
		// TODO Auto-generated method stub
		
		if (args.length > 2) {
			sender.sendMessage(Main.Header + ChatColor.RED + "too many arguments! Usage: " + GetShortHelp());
			return false;
		}
		
		if (args.length == 2) {
			if (args[1].equalsIgnoreCase("here")) {
				if (sender instanceof Player) {
					Player p = (Player)sender;
					SummarizeWorld(sender, p.getLocation().getWorld());
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
			else {
				List<World> worlds = Bukkit.getServer().getWorlds();
				for (World w : worlds)
				{
					// Guard against madness
					if (w == null)
						continue;
					
					// Check if the world name equals what they entered
					if (w.getName().equalsIgnoreCase(args[1])) {
						SummarizeWorld(sender, w);
						return true;
					}
				}	
				sender.sendMessage(Main.Header + ChatColor.RED + "Unable to find world with that name. Double check spelling?");
				return false;
			}
		}
		
		List<World> worlds = Bukkit.getServer().getWorlds();
		for (World w : worlds)
		{
			// Guard against madness
			if (w == null)
				continue;
			
			SummarizeWorld(sender, w);
		}
		return true;
	}

	@Override
	public String GetRequiredPermission() {
		return "entitylagmeasure.summary";
	}

}
