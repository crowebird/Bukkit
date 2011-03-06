package com.crowebird.bukkit.AntiGrief.ZoneProtection;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.crowebird.bukkit.AntiGrief.AntiGrief;

public class AntiGriefZoneProtectionWorld {

	
	@SuppressWarnings("unused")
	private final AntiGrief plugin;
	
	private ArrayList<AntiGriefZoneProtectionZone> zones;
	
	private final String world;
	
	public AntiGriefZoneProtectionWorld(AntiGrief plugin_, String world_) {
		plugin = plugin_;
		world = world_;
		zones = new ArrayList<AntiGriefZoneProtectionZone>();
	}
	
	public boolean access(Player player_, String node_, Location location_, int item_, boolean suppress_) throws AntiGriefZoneProtectionException {
		for(AntiGriefZoneProtectionZone zone : this.zones) {
			if (zone.inside(location_.getBlockX(), location_.getBlockY(), location_.getBlockZ())) {
				return zone.access(player_, node_, item_, suppress_);
			}
		}
		throw new AntiGriefZoneProtectionException();
	}
	
	public String zoneInside(Player player_) {
		Location l = player_.getLocation();
		
		for(AntiGriefZoneProtectionZone zone : this.zones) {
			if (zone.inside(l.getBlockX(), l.getBlockY(), l.getBlockZ())) {
				return zone.getName();
			}
		}
		return null;		
	}
	
	public boolean zoneExists(String zone_) {
		for (AntiGriefZoneProtectionZone zone : zones) {
			if (zone.getName().equals(zone_)) return true;
		}
		return false;
	}
	
	public boolean addZone(AntiGriefZoneProtectionZone zone_) {
		zone_.finalize();
		String parent = zone_.getParent();
		if (parent == null) return this.zones.add(zone_);
		else {
			int x = 0;
			for (AntiGriefZoneProtectionZone zone : zones) {
				if (zone.getName().equals(parent)) {
					this.zones.add(x, zone_);
					return true;
				}
				++x;
			}
		}
		return false;
	}
	
	public String getName() {
		return world;
	}
}
