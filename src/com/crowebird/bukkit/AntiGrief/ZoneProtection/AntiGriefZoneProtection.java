package com.crowebird.bukkit.AntiGrief.ZoneProtection;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.crowebird.bukkit.AntiGrief.AntiGrief;
import com.crowebird.bukkit.util.Config;

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
		builder.remove(player_);
	}
	
	public void modifyZone() {
		
	}
	
	public void addPoint(String player_, int x_, int z_) {
		builder.get(player_).addPoint(x_, z_);
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
		return true;
	}
	
	public void addZone(String world_, String zone_, Config.Type config_) {
		AntiGriefZoneProtectionWorld world = worlds.get(world_);
		if (world == null) world = new AntiGriefZoneProtectionWorld(plugin, world_);
		world.addZone(new AntiGriefZoneProtectionZone(plugin, zone_, config_, world_));
		worlds.put(world_, world);
	}
	
	public void load(Config.Type default_) {
		worlds = new HashMap<String, AntiGriefZoneProtectionWorld>();
		
		try {
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
							addZone(world, name, Config.read(plugin.pdf.getName(), plugin.getDataFolder().toString() + File.separator + "zones" + File.separator + world, name, default_, true, false));
						}
					}
				}
			}
		} catch (IOException ex) { System.out.println(ex.toString()); }
	}
}