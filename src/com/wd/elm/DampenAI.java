package com.wd.elm;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class DampenAI implements ICommand {

	@Override
	public String GetLongHelp() {
		return "/elm dampen <off|on> [radius] Disables AI in the given radius. Defaults to 5 radius if non specified.";
	}

	@Override
	public String GetShortHelp() {
		return "dampen <off|on> [radius]   : disables AI in the given radius";
	}

	@Override
	public String GetCommand() {
		return "dampen";
	}

	@Override
	public boolean Execute(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(Main.Header + "You must be a player to run this command");
			return true;
		}
		
		boolean enable = false;
		if (args.length < 2) {
			sender.sendMessage(Main.Header + "You must provide <on|off> argument.");
			return true;
		}
		
		if (args[1].equalsIgnoreCase("on")) {
			enable = false;
		}
		else if (args[1].equalsIgnoreCase("off")) {
			enable = true;
		}
		else {
			sender.sendMessage(Main.Header + "Valid arguments are 'on' or 'off'");
			return true;
		}
		
		double radius = 5;
		if (args.length >= 3) {
			try {
				radius = Double.parseDouble(args[2]);
			}
			catch (Exception ex) {
				sender.sendMessage(Main.Header + ChatColor.RED + "Radius of '" + args[2] + "' not valid!");
				return true;
			}
			if (/*radius > 100 || */radius <= 0) {
				sender.sendMessage(Main.Header + ChatColor.RED + "Radius of '" + args[2] + "' too large! (or too small)");
				return true;
			}
		}
		
		Player issuer = (Player)sender;
		List<Entity> entities = issuer.getWorld().getEntities();
		// Count how many of all entities we have on this world
		int dampenedAi = 0;
		int customEntities = 0;
		for (Entity e : entities) {
			if (!(e instanceof LivingEntity)) 
				continue;
			
			if (e instanceof Player)
				continue;
			
			/// WTF?
			if (!issuer.getWorld().getName().equals(e.getWorld().getName()))
				continue;
			
			if (e.getLocation().distance(issuer.getLocation()) > radius)
				continue;
						
			if (e.getCustomName() != null && e.getCustomName() != "") {
				customEntities++;
				continue;
			}
			
			dampenedAi++;
			Reflection.setAiEnabled(e, enable);
		}
		
		if (customEntities > 0) {
			sender.sendMessage(Main.Header + "Skipped " + customEntities + " named entities which can't be dampened.");
		}
		
		if (enable)
			sender.sendMessage(Main.Header + "Successfully undamped " + dampenedAi + " entities.");
		else
			sender.sendMessage(Main.Header + "Successfully damped " + dampenedAi + " entities.");
		
		return true;
	}

	@Override
	public String GetRequiredPermission() {
		return "entitylagmeasure.dampen";
	}

}
