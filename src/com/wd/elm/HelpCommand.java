package com.wd.elm;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class HelpCommand implements ICommand {
	private ArrayList<ICommand> commandList;
	
	public HelpCommand(ArrayList<ICommand> commands) {
		commandList = commands;
	}
	
	public void ShowAllHelp(CommandSender toSender)
	{
		toSender.sendMessage(Main.Header + "EntityLagMeasure toolset " + Main.Version);
		toSender.sendMessage(Main.Header + "Commands:");
		for (ICommand command : commandList) {
			toSender.sendMessage(Main.Header + "  /elm " + command.GetShortHelp());
		}
		toSender.sendMessage(Main.Header + "Do /elm help <commandname> for help on a specific command.");
	}
	
	@Override
	public String GetLongHelp() {
		return "/elm help [command]\n\tShows how to use a command (like this one)\n\tSpecify a command to get specific help about that command";
	}

	@Override
	public String GetShortHelp() {
		return "help [command]     : provides basic help.";
	}

	@Override
	public String GetCommand() {
		return "help";
	}

	@Override
	public boolean Execute(CommandSender sender, String[] args) {
		if (args.length == 2){
			for (ICommand command : commandList) {
				if (args[1].equalsIgnoreCase(command.GetCommand())) {
					// Split the separate lines into separate messages
					String[] lines = command.GetLongHelp().split("\n");
					for (String line : lines) {
						sender.sendMessage(Main.Header + line);
					}
					return true;
				}
			}
			sender.sendMessage(Main.Header + ChatColor.RED + "No command found with name '" + args[1] + "'");
		}
		else if (args.length > 2) {
			sender.sendMessage(Main.Header + ChatColor.RED + "Specify only one command at a time. Do " + 
					ChatColor.ITALIC + "/elm help" + ChatColor.RESET + ChatColor.RED + "for details.");
			return false;
		}
		
		ShowAllHelp(sender);
		return true;
	}

	@Override
	public String GetRequiredPermission() {
		return "";
	}

}
