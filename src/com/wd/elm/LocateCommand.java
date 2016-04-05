package com.wd.elm;

import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LocateCommand implements ICommand {

	private PlayerLocationCache cache;
	
	public LocateCommand(PlayerLocationCache cache) {
		this.cache = cache;
	}
	
	@Override
	public String GetLongHelp() {
		return "/elm locate [resolution] \n\tLocate groups of entities in your current world\n\t which may or may not be causing lag.\n\tYou can optionally specify a resolution\n\tThe resolution is the largest detectable group size\n\tIf none is specified it defaults to 12\n\tHigher density mobs are more likely to cause lag.\nUse -t to check tile entities instead of regular entities.";
	}

	@Override
	public String GetShortHelp() {
		return "locate [resolution] : Locates possibly laggy groups of mobs";
	}

	@Override
	public String GetCommand() {
		return "locate";
	}
	
	
		
	private MobGroup[] locateOnWorld(World w, boolean tileModeEnabled, int radius) {
		List<Countable> entities = Main.GetEntities(w, tileModeEnabled);
		EntityImageMap map1;
		EntityImageMap map2;
		
		if (tileModeEnabled) {
			map1 = new EntityImageMap(radius, true, 0, 0, 0);
			map2 = new EntityImageMap(radius, true, radius/2, radius/2, radius/2);		
		}
		else {
			map1 = new EntityImageMap(radius, false, 0, 0, 0);
			map2 = new EntityImageMap(radius, false, radius/2, 0, radius/2);		
		}
		
		// Count how many of all entities we have on this world
		for (int i = 0; i < entities.size(); i++) {
			Countable e = entities.get(i);

			map1.AddEntityToBucket(e);
			map2.AddEntityToBucket(e);
		}
		
		// Sort the groups of entities by how large they are
		PriorityQueue<List<Countable>> queue = new PriorityQueue<List<Countable>>(10, new Comparator<List<Countable>>() {
			public int compare(List<Countable> arg0, List<Countable> arg1) {
				return -Integer.compare(arg0.size(), arg1.size());
			}
		});
		queue.addAll(map1.GetBuckets().values());
		queue.addAll(map2.GetBuckets().values());
		
		// Now expose the top 5 groups providing the player with their centers
		MobGroup[] locations = new MobGroup[5];
		for (int i = 0; i < 5; i++) {
			List<Countable> entitiesInLoc = queue.poll();
			if (entitiesInLoc == null)
				break;
			
			double totalX = 0;
			double totalY = 0;
			double totalZ = 0;
			double minX = Double.POSITIVE_INFINITY;
			double maxX = Double.NEGATIVE_INFINITY;
			double minZ = Double.POSITIVE_INFINITY;
			double maxZ = Double.NEGATIVE_INFINITY;
			for (int e = 0; e < entitiesInLoc.size(); e++) {
				Location eloc = entitiesInLoc.get(e).Pos;
				totalX += eloc.getX();
				totalY += eloc.getY();
				totalZ += eloc.getZ();
				minX = Math.min(minX, eloc.getX());
				maxX = Math.max(maxX, eloc.getX());
				minZ = Math.min(minZ, eloc.getZ());
				maxZ = Math.max(maxZ, eloc.getZ());
			}
			
			int size = entitiesInLoc.size();
			locations[i] = new MobGroup(new Location(w, totalX / size, totalY / size, totalZ / size), size, (maxX - minX)*(maxZ - minZ));
		}
		
		return locations;
	}


	private String padding(int length) {
		String str = "";
		for (int i = 0; i < length; i++)
			str += " ";
		return str;
	}
	
	@Override
	public boolean Execute(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(Main.Header + "Sorry this command can only be sent by players right now. Check back next version");
			return true;
		}
		
		boolean tileModeEnabled = Main.EnableTileMode(args);
		args = Main.RemoveTileMode(args);
		
		int radius = 12;
		if (tileModeEnabled) {
			radius = 20;
		}
		if (args.length > 1) {
			try {
				radius = Integer.parseInt(args[1]);
                
                if (radius < 4) {
                    sender.sendMessage(Main.Header + ChatColor.RED + "Resolution is too small. Try again with a larger resolution");
                    return true;          
                }
                else if (radius > 1000) {
                    sender.sendMessage(Main.Header + ChatColor.RED + "Resolution is too large. Try again with a smaller resolution.");
                    return true;    
                }
			}
			catch (Exception ex) {
				sender.sendMessage(Main.Header + ChatColor.RED + "Unable to read resolution number '" + args[1] + "'");
				return true;
			}
		}
		
		Player p = (Player)sender;
		MobGroup[] mobs = locateOnWorld(p.getWorld(), tileModeEnabled, radius);
		if (tileModeEnabled)
			sender.sendMessage(Main.Header + "Top five groups of tile entities on this world with radius " + radius + ":");
		else
			sender.sendMessage(Main.Header + "Top five groups of entities on this world with radius " + radius + ":");
		
		boolean stoppedEarly = false;
		for (int i = 0; i < mobs.length; i++) {
			double area = mobs[i].Area;
			if (area < 1)
				area = 1;
			
			double density = mobs[i].Count/area;
			
			if ((mobs[i].Count < 5) || (mobs[i].Count < 15 && density < 4)) {
				stoppedEarly = true;
				break;
			}
			
			String countStr = mobs[i].Count + " entities ";
			if (mobs[i].Owner != null && mobs[i].Owner.length() > 0) {
				sender.sendMessage(Main.Header + ChatColor.GREEN + "#" + (i+1) + ". "  + ChatColor.YELLOW + countStr + padding(20-countStr.length()) + (int)(density * 100)/100.0 + " density    <" + mobs[i].Owner + ">");	
			}
			else {
				sender.sendMessage(Main.Header + ChatColor.GREEN + "#" + (i+1) + ". "  + ChatColor.YELLOW + countStr + padding(20-countStr.length()) + (int)(density * 100)/100.0 + " density");
			}
		}
			
		cache.SaveLookupResults(sender, mobs);
		sender.sendMessage(Main.Header + "Use /elm tp <group number>   to teleport to one of the groups of entities");
		
		if (stoppedEarly) {
			sender.sendMessage(Main.Header + "(Maybe try /elm summary -p)");	
		}
		
		return true;
	}

	@Override
	public String GetRequiredPermission() {
		return "entitylagmeasure.locate";
	}

}
