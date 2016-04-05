package com.wd.elm;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;

public class Commands implements Listener, CommandExecutor {
	private ArrayList<ICommand> commands = new ArrayList<ICommand>();
	
	// This command is special since its called to show help
	private HelpCommand help;
	
	
	public Commands() {
		help = new HelpCommand(commands);
		
		commands.add(help);
		commands.add(new SummarizeCommand());
		
		// Need the playerlocationcache to be shared
		PlayerLocationCache locations = new PlayerLocationCache();
		commands.add(new LocateCommand(locations));
		commands.add(new TpCommand(locations));
		commands.add(new DampenAI());
		commands.add(new FreezeHoppers());
	}
	
	public boolean onCommand(CommandSender sender, Command cmd,
			String commandLabel, String[] args) {
		// Verify entitylagmeasure set
		if (!commandLabel.equalsIgnoreCase("entitylagmeasure") && 
				!commandLabel.equalsIgnoreCase("elm")) {
			return false;
		}
				
		if (args.length == 0) {
			sender.sendMessage(Main.Header + ChatColor.RED + "You must provide a command.");
			help.ShowAllHelp(sender);
			return true;
		}
		
		// Search for a command which uses this command
		for (ICommand command : commands) {
			if (args[0].equalsIgnoreCase(command.GetCommand())) {
				String requiredPermission = command.GetRequiredPermission();
				if (requiredPermission.length() > 0 && !sender.hasPermission(requiredPermission))
				{
					sender.sendMessage(Main.Header + ChatColor.RED + "You don't have the necessary permission for that! You need " + requiredPermission);
					return true;
				}
				try {
					command.Execute(sender, args);
				}
				catch (Exception ex ) {
					sender.sendMessage(Main.Header + ChatColor.RED + "Something went very wrong inside ELM. Sorry =( printed stacktrace to console.");
					ex.printStackTrace();
				}
				return true;
			}
		}
		
		sender.sendMessage(Main.Header + ChatColor.RED + "Unable to recognize '" + args[0] + "'");
		help.ShowAllHelp(sender);
		
		return true;
	}
}
