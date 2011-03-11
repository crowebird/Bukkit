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

import java.io.File;
import java.util.HashMap;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.crowebird.bukkit.plugins.AntiGrief.AntiGrief;
import com.crowebird.bukkit.plugins.util.config.Config;
import com.crowebird.bukkit.plugins.util.config.ConfigTemplate;

public class AntiGriefZoneProtection {
	
	private final AntiGrief plugin;
	
	private HashMap<String, AntiGriefZoneProtectionWorld> worlds;
	private HashMap<String, AntiGriefZoneProtectionZone> builder;
	
	public AntiGriefZoneProtection(AntiGrief plugin_) {
		plugin = plugin_;
		worlds = new HashMap<String, AntiGriefZoneProtectionWorld>();
		builder = new HashMap<String, AntiGriefZoneProtectionZone>();
	}
	
	public boolean inZone(Player player_) {
		return false;
	}
	
	public boolean access(Player player_, String node_, Location location_, int item_, boolean suppress_) throws AntiGriefZoneProtectionException {
		AntiGriefZoneProtectionWorld world = worlds.get(player_.getWorld().getName());
		if (world == null) throw new AntiGriefZoneProtectionException("No zones defined!");
		return world.access(player_, node_, location_, item_, suppress_);
	}
	
	public boolean isBuilding(String player_) {
		return builder.containsKey(player_);
	}
		
	public boolean createZone(Player player_, String zone_) {
		String name = player_.getName();
		if (isBuilding(name)) {
			player_.sendMessage("You are already creating a zone!");
			return false;
		}
		
		String parent = null;
		AntiGriefZoneProtectionWorld world = worlds.get(player_.getWorld().getName());
		if (world != null) parent = world.zoneInside(player_);
		
		Set<String> zones = builder.keySet();
		for (String zone : zones) {
			AntiGriefZoneProtectionZone z = builder.get(zone);
			if (z.getName().equals(zone_) && z.getWorld().equals(player_.getWorld().getName())) {
				player_.sendMessage("Another zone with the same name is already being created!");
				return false;
			}
		}
		
		if (world.zoneExists(zone_)) {
			player_.sendMessage("Another zone with the same name already exists!");
			return false;
		} else
			builder.put(name, new AntiGriefZoneProtectionZone(plugin, zone_, name, parent, player_.getWorld().getName()));
		return true;
	}
	
	public void cancelZone(String player_) {
		AntiGriefZoneProtectionZone zone = builder.remove(player_);
		if (zone == null)
			return;
		zone.hideVisulization();
	}
	
	public void modifyZone() {
		
	}
	
	public void addPoint(String player_, int x_, int y_, int z_) {
		builder.get(player_).addPoint(x_, y_, z_);
	}
	
	public void visulize(Player player_, String zone_) {
		String worldn = player_.getWorld().getName();
		AntiGriefZoneProtectionWorld world = worlds.get(worldn);
		if (world == null) {
			player_.sendMessage("No zones exist!");
			return;
		}
		if (!world.visulize(player_, zone_))
			player_.sendMessage("Zone does not exist!");
	}
	
	public boolean finishZone(Player player_) {
		String name = player_.getName();
		
		AntiGriefZoneProtectionZone zone = builder.remove(name);
		if (zone.getPoints() <= 2) {
			player_.sendMessage("Zone needs at least 3 points, you have only defined " + zone.getPoints());
			builder.put(player_.getName(), zone);
			return false;
		}
		
		String worldn = player_.getWorld().getName();
		AntiGriefZoneProtectionWorld world = worlds.get(worldn);
		if (world == null) world = new AntiGriefZoneProtectionWorld(plugin, worldn);
		world.addZone(zone);
		worlds.put(worldn, world);
		
		zone.hideVisulization();
		return true;
	}
	
	public void addZone(String world_, String zone_, Config config_) {
		AntiGriefZoneProtectionWorld world = worlds.get(world_);
		if (world == null) world = new AntiGriefZoneProtectionWorld(plugin, world_);
		world.addZone(new AntiGriefZoneProtectionZone(plugin, zone_, config_, world_));
		worlds.put(world_, world);
	}
	
	public void load(ConfigTemplate templateZone_) {
		worlds = new HashMap<String, AntiGriefZoneProtectionWorld>();
		
		new File(plugin.getDataFolder().toString() + File.separator + "zones").mkdir();
		File dirs[] = (new File(plugin.getDataFolder().toString() + File.separator + "zones")).listFiles();
		if (dirs != null) {
			for (File dir : dirs) {
				if (!dir.isDirectory()) continue;
				String world = dir.getName();
				File files[] = (new File(plugin.getDataFolder().toString() + File.separator + "zones" + File.separator + world)).listFiles();
				if (files != null) {
					for(File file : files) {
						String name = file.getName();
						int extension = name.lastIndexOf(".");
						name = name.substring(0, (extension == -1 ? name.length() : extension));

						addZone(world, name, new Config(plugin.getDataFolder().toString() + File.separator + "zones" + File.separator + world, name, templateZone_));
					}
				}
			}
		}
	}
	
	private AntiGriefZoneProtectionZone getZone(Player player_, String zone_) {
		AntiGriefZoneProtectionWorld world = worlds.get(player_.getWorld().getName());
		if (world == null) {
			player_.sendMessage("Zone does not exist!");
			return null;
		}
		AntiGriefZoneProtectionZone zone = world.getZone(zone_);
		if (zone == null) {
			player_.sendMessage("Zone does not exist!");
		}
		return zone;
	}
	
	public void addUser(Player player_, String zone_, String user_) {
		AntiGriefZoneProtectionZone zone = getZone(player_, zone_);
		if (zone == null) return;
		if (zone.addUser(user_))
			player_.sendMessage("Player added to " + zone_ + "!");
		else player_.sendMessage("Player already has access to " + zone_ + "!");
	}
	
	public void addGroup(Player player_, String zone_, String group_) {
		AntiGriefZoneProtectionZone zone = getZone(player_, zone_);
		if (zone == null) return;
		if (zone.addGroup(group_))
			player_.sendMessage("Group added to " + zone_ + "!");
		else player_.sendMessage("Group already has access to " + zone_ + "!");
	}

	public void removeUser(Player player_, String zone_, String user_) {
		AntiGriefZoneProtectionZone zone = getZone(player_, zone_);
		if (zone == null) return;
		if (zone.removeUser(user_))
			player_.sendMessage("Player removed from " + zone_ + "!");
		else player_.sendMessage("Player does not have access to " + zone_ + "!");
	}

	public void removeGroup(Player player_, String zone_, String group_) {
		AntiGriefZoneProtectionZone zone = getZone(player_, zone_);
		if (zone == null) return;
		if (zone.removeGroup(group_))
			player_.sendMessage("Group removed from " + zone_ + "!");
		else player_.sendMessage("Group does not have access to " + zone_ + "!");
	}
}
