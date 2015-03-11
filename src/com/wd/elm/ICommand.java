package com.wd.elm;

import org.bukkit.command.CommandSender;

public interface ICommand {
	public String GetLongHelp();
	public String GetShortHelp();
	public String GetCommand();
	public boolean Execute(CommandSender sender, String[] args);
	public String GetRequiredPermission();
}
