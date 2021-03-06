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
FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL Michael Crowe OR
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

package com.crowebird.bukkit.plugins.AntiGrief.ZoneProtection;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.crowebird.bukkit.plugins.AntiGrief.AntiGrief;

public class AntiGriefZoneProtectionWorld {

	
	@SuppressWarnings("unused")
	private final AntiGrief plugin;
	
	private ArrayList<AntiGriefZoneProtectionZone> zones;
	private HashMap<String, String> inside;
	
	private final String world;
	
	public AntiGriefZoneProtectionWorld(AntiGrief plugin_, String world_) {
		plugin = plugin_;
		world = world_;
		zones = new ArrayList<AntiGriefZoneProtectionZone>();
		inside = new HashMap<String, String>();
	}
	
	public boolean access(Player player_, String node_, Location location_, int item_, boolean suppress_) throws AntiGriefZoneProtectionException {
		for(AntiGriefZoneProtectionZone zone : this.zones) {
			if (zone.inside(location_.getBlockX(), location_.getBlockY(), location_.getBlockZ())) {
				if (inside.containsKey(player_.getName())) {
					if (!inside.get(player_.getName()).equals(zone.getName())) {
						String msg = zone.getMessage();
						if (!msg.equals(""))
							player_.sendMessage(zone.getMessage());
					}
				}
				inside.put(player_.getName(), zone.getName());
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
	
	public boolean visulize(Player player_, String zone_) {
		for (AntiGriefZoneProtectionZone zone : zones) {
			if (zone.getName().equals(zone_)) {
				if (!zone.isVisulized()) {
					if (!zone.isCompatible()) {
						player_.sendMessage("Unable to build zone visulization!");
						return true;
					} 
					zone.showVisulization();
				}
				else zone.hideVisulization();
				return true;
			}
		}
		
		return false;
	}
	
	public boolean addZone(AntiGriefZoneProtectionZone zone_) {
		zone_.finalize();
		String parent = zone_.getParent();
		
		int x = 0;
		for (AntiGriefZoneProtectionZone zone : zones) {
			if ((parent != null && zone.getName().equals(parent)) || (zone.getParent() != null && zone.getParent().equals(zone_.getName()))) {
				this.zones.add(x, zone_);
				return true;
			}
			++x;
		}
		return this.zones.add(zone_);
	}
	
	public AntiGriefZoneProtectionZone getZone(String zone_) {
		for (AntiGriefZoneProtectionZone zone : zones) {
			if (zone.getName().equals(zone_)) {
				return zone;
			}
		}
		return null;
	}
	
	public String getName() {
		return world;
	}
}
