/*
 Copyright 2011 Michael Crowe. All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are
permitted provided that the following conditions are met:

   1. Redistributions of source code must retain the above copyright notice, this list of
      conditions and the following disclaimer.

   2. Redistributions in binary form must reproduce the above copyright notice, this list
      of conditions and the following disclaimer in the documentation and/or other materials
      provided with the distribution.

THIS SOFTWARE IS PROVIDED BY Michael Crowe ``AS IS'' AND ANY EXPRESS OR IMPLIED
WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> OR
CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

The views and conclusions contained in the software and documentation are those of the
authors and should not be interpreted as representing official policies, either expressed
or implied, of Michael Crowe.
*/

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
