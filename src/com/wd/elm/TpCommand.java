package com.wd.elm;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TpCommand implements ICommand {

	private PlayerLocationCache cache;
	
	public TpCommand(PlayerLocationCache cache) {
		this.cache = cache;
	}
	
	@Override
	public String GetLongHelp() {
		return "/elm tp <mob group #> \n\tTeleport to a group of entities from a previous /elm locate";
	}

	@Override
	public String GetShortHelp() {
		return "tp <mob group #> : Teleport to one of the groups from /elm locate";
	}

	@Override
	public String GetCommand() {
		return "tp";
	}
	
	
	@Override
	public boolean Execute(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(Main.Header + "If you're not a player you can't teleport!");
			return true;
		}
		
		if (args.length != 2) {
			sender.sendMessage(Main.Header + ChatColor.RED + "You either provided too many or not enough arguments. Do /elm help tp");
			return true;
		}
		
		int value;
		try {
			value = Integer.parseInt(args[1]);
		}
		catch(Exception ex) {
			sender.sendMessage(Main.Header + ChatColor.RED + "Unable to read integer value '" + args[1] + "'");
			return true;
		}
		
		value--;
		if (value < 0) {
			sender.sendMessage(Main.Header + ChatColor.RED + "tp value should be a group number 1 to 5");
			return true;
		}
		if (!cache.TeleportPlayer((Player)sender, value)) {
			sender.sendMessage(Main.Header + ChatColor.RED + "The value you specified either wasn't 1 to 5, or you need to rerun /elm locate");
			return true;
		}
		
		sender.sendMessage(Main.Header + "Teleporting player to mobgroup #" + (value + 1));
		return true;
	}

	@Override
	public String GetRequiredPermission() {
		return "entitylagmeasure.tp";
	}

}
