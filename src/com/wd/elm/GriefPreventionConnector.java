package com.wd.elm;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;

import org.bukkit.Location;

public class GriefPreventionConnector {
	public static String LookupPlayerFromLocation(Location loc) {
		try {
			Claim claim = GriefPrevention.instance.dataStore.getClaimAt(loc, true, null);
			return claim.getOwnerName();
		}
		catch (Exception ex) {
			// No grief prevention
			return "";
		}
	}
}
