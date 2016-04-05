package com.wd.elm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Location;

// This uses a hashmap backing to map entities into 2dimensional buckets.
// Could be rendered into a bmp or something to get a hotmap
public class EntityImageMap {
	private HashMap<Long, List<Countable>> buckets = new HashMap<Long, List<Countable>>();
	private int radius;
	private int oX, oY, oZ;
	private boolean use3dBucket;
	public EntityImageMap(int bucketWidth, boolean use3dBucket, int offsetX, int offsetY, int offsetZ) {
		radius = bucketWidth;
		oX = offsetX;
		oY = offsetY;
		oZ = offsetZ;
	}
	// Place entities in 3d buckets based on their position
	public void AddEntityToBucket(Countable e) {
		Location a = e.Pos;
		
		int bx = ((int)(a.getX() + oX))/radius + 250000;
		int by = ((int)(a.getY() + oY))/(use3dBucket ? radius : 3);
		int bz = ((int)(a.getZ() + oZ))/radius + 250000;
		// Format of bucket entry is:
		// 24 bits X|8 bits Y|24 bits Z
		long bucketEntry = bz;
		bucketEntry |= (long)bx << 32;
		bucketEntry |= (long)by << 24;
		List<Countable> bucket = buckets.get(bucketEntry);
		if (bucket == null) {
			bucket = new ArrayList<Countable>();
			buckets.put(bucketEntry, bucket);
		}
		bucket.add(e);
	}
	public HashMap<Long, List<Countable>> GetBuckets() {
		return buckets;
	}
}
