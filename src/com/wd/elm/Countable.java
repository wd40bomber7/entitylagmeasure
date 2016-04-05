package com.wd.elm;


import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;

public class Countable {
	public Countable(Entity e) {
		Name = e.getName();
		Pos = e.getLocation();
		HasAi = Reflection.getAiEnabled(e);
	}
	public Countable(Block b) {
		Name = b.getState().getType().name();
		Pos = b.getLocation();
		HasAi = true;
	}
	
	public String Name;
	public Location Pos;
	public boolean HasAi;
}
