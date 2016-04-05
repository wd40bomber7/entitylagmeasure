package com.wd.elm;

import org.bukkit.Location;

public class MobGroup {
	public Location At;
	public int Count;
	public double Area;
	public String Owner;
	public MobGroup(Location at, int count, double area) {
		At = at;
		Count = count;
		Area = area;
		Owner = GriefPreventionConnector.LookupPlayerFromLocation(at);
	}
}
