package com.wd.elm;

import java.util.HashMap;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PlayerLocationCache {
	private HashMap<String, MobGroup[]> playerLagLookups = new HashMap<String, MobGroup[]>();
	
	public void SaveLookupResults(CommandSender cs, MobGroup[] results) {
		if (!(cs instanceof Player))
			return;
		Player p = (Player)cs;
		MobGroup[] capturedResults = new MobGroup[5];
		for (int i = 0; i < 5 && i < results.length; i++) {
			capturedResults[i] = results[i];
		}
		playerLagLookups.put(p.getUniqueId().toString(), capturedResults);
	}
	
	public boolean TeleportPlayer(Player p, int locationId) {
		MobGroup[] locations = playerLagLookups.get(p.getUniqueId().toString());
		if (locations == null)
			return false;
		if (locationId >= locations.length)
			return false;
		if (locations[locationId] == null)
			return false;
		
		p.teleport(locations[locationId].At);
		
		return true;
	}
}

