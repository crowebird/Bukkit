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

/*
 * Using CraftBukkit 527+
 * (Bukkit 451+)
 * 
 * With Permissions:
 * - GroupManager 1.0 alpha 5
 * - Permissions 2.5.2
*/

package com.crowebird.bukkit.plugins.AntiGrief;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.plugin.PluginManager;

import com.crowebird.bukkit.plugins.BukkitPlugin;
import com.crowebird.bukkit.plugins.AntiGrief.Listeners.AntiGriefBlockListener;
import com.crowebird.bukkit.plugins.AntiGrief.Listeners.AntiGriefEntityListener;
import com.crowebird.bukkit.plugins.AntiGrief.Listeners.AntiGriefPlayerListener;
import com.crowebird.bukkit.plugins.AntiGrief.Listeners.AntiGriefVehicleListener;
import com.crowebird.bukkit.plugins.AntiGrief.ZoneProtection.AntiGriefZoneProtection;
import com.crowebird.bukkit.plugins.AntiGrief.ZoneProtection.AntiGriefZoneProtectionException;
import com.crowebird.bukkit.plugins.util.config.Config;
import com.crowebird.bukkit.plugins.util.config.ConfigTemplate;
import com.crowebird.bukkit.plugins.util.config.cast.ConfigArrayListInteger;
import com.crowebird.bukkit.plugins.util.config.cast.ConfigArrayListString;

public class AntiGrief extends BukkitPlugin {
	private AntiGriefBlockListener blockListener;
	private AntiGriefPlayerListener playerListener;
	private AntiGriefEntityListener entityListener;
	private AntiGriefVehicleListener vehicleListener;

	protected AntiGriefZoneProtection zoneProtection;
	
	private HashMap<String, Long> message_delay;
	
	private final ConfigTemplate template_settings;
	private final ConfigTemplate template_world;
	public final ConfigTemplate template_zone;
	
	public AntiGrief() {
		super("AntiGrief", "0.9", true);
		
		blockListener = new AntiGriefBlockListener(this);
		playerListener = new AntiGriefPlayerListener(this);
		entityListener = new AntiGriefEntityListener(this);
		vehicleListener = new AntiGriefVehicleListener(this);
		
		message_delay = new HashMap<String, Long>();
				
		template_settings = new ConfigTemplate();
		template_settings.put("priority", "lowest");
		template_settings.put("message.access", "You are not allowed to perform that action!");
		template_settings.put("message.command", "You are not allowed to use that command!");
		template_settings.put("message.delay", 5);
		template_settings.put("zones.enable", true);
		template_settings.put("zones.tool", 280);
		
		template_world = new ConfigTemplate();
		template_world.put("nodes.buildfalse", new ConfigArrayListString(
			"block.damage",
			"block.place",
			"block.interact",
			"block.ignite",
			"entity.creeper",
			"player.damage.cause",
			"player.item.use",
			"player.item.pickup",
			"vehicle.use",
			"vehicle.move"
		));
		template_world.put("nodes.buildtrue", new ConfigArrayListString());
		template_world.put("allow.interact", new ConfigArrayListInteger(64));
		template_world.put("allow.item", new ConfigArrayListInteger());
		template_world.put("allow.block", new ConfigArrayListInteger());
		
		template_zone = new ConfigTemplate();
		template_zone.put("creator", "");
		template_zone.put("parent", "");
		template_zone.put("points", new ConfigArrayListString());
		template_zone.put("message.enter", "");
		template_zone.put("groups.*.allow.item", new ConfigArrayListInteger());
		template_zone.put("groups.*.allow.interact", new ConfigArrayListInteger());
		template_zone.put("groups.*.allow.block", new ConfigArrayListInteger());
		template_zone.put("groups.*.nodes.prevent", new ConfigArrayListString());
		template_zone.put("users.*.allow.item", new ConfigArrayListInteger());
		template_zone.put("users.*.allow.interact", new ConfigArrayListInteger());
		template_zone.put("users.*.allow.block", new ConfigArrayListInteger());
		template_zone.put("users.*.nodes.prevent", new ConfigArrayListString());
	}
	
	public void onEnable() {
		zoneProtection = new AntiGriefZoneProtection(this);
		getCommand("ag").setExecutor(new AntiGriefCommand(this));
		
		super.onEnable();
	}
	
	public void buildConfig() {		
		configs.put("settings", new Config(this, getDataFolder().toString(), "settings", template_settings));
		configs.put("default", new Config(this, getDataFolder().toString(), "default", template_world));

		File worlddirs[] = (new File(getDataFolder().toString())).listFiles();
		if (worlddirs != null) {
			for (File dir : worlddirs) {
				if (!dir.isDirectory()) continue;
				String world = dir.getName();
				File config = new File(getDataFolder().toString() + File.separator + world + File.separator + "config.yml");
				if (!config.exists()) continue;
				configs.put("world." + world, new Config(this, getDataFolder().toString() + File.separator + world, "config", template_zone));
			}
		}

		for(String key : configs.keySet()) {
			Config config = configs.get(key);
			try {
				config.load();
			} catch (IOException ex) {
				config.create();
			}
		}
		
		if (configs.get("settings").getValue("zones.enable").equals(true)) 
			zoneProtection.load(template_zone);
	}
	
	protected void registerEvents() {
		PluginManager pm = getServer().getPluginManager();
		
		String level = (String) getValue("settings", "priority");
		Event.Priority compile_level = Event.Priority.Lowest;
		if (level.equals("low"))
			compile_level = Event.Priority.Low;
		else if (level.equals("normal"))
			compile_level = Event.Priority.Normal;
		else if (level.equals("high"))
			compile_level = Event.Priority.High;
		else if (level.equals("highest"))
			compile_level = Event.Priority.Highest;
		
		pm.registerEvent(Event.Type.BLOCK_DAMAGED, blockListener, compile_level, this);
		pm.registerEvent(Event.Type.BLOCK_PLACED, blockListener, compile_level, this);
		pm.registerEvent(Event.Type.BLOCK_INTERACT, blockListener, compile_level, this);
		pm.registerEvent(Event.Type.BLOCK_IGNITE, blockListener, compile_level, this);
		pm.registerEvent(Event.Type.BLOCK_RIGHTCLICKED, blockListener, compile_level, this);
		
		pm.registerEvent(Event.Type.ENTITY_TARGET, entityListener, compile_level, this);
		pm.registerEvent(Event.Type.ENTITY_DAMAGED, entityListener, compile_level, this);
		
		pm.registerEvent(Event.Type.PLAYER_ITEM, playerListener, compile_level, this);
		pm.registerEvent(Event.Type.PLAYER_PICKUP_ITEM, playerListener, compile_level, this);
		
		pm.registerEvent(Event.Type.VEHICLE_COLLISION_ENTITY, vehicleListener, compile_level, this);
		pm.registerEvent(Event.Type.VEHICLE_MOVE, vehicleListener, compile_level, this);
		pm.registerEvent(Event.Type.VEHICLE_DAMAGE, vehicleListener, compile_level, this);
		pm.registerEvent(Event.Type.VEHICLE_ENTER, vehicleListener, compile_level, this);
		
		
		log("Using the " + compile_level.toString() + " priority level.");
	}

	protected boolean access(Player player_, String node_, Location location_, int item_, boolean supress_) {
		if (player_ == null) return true;
		
		if (hasPermission(player_, "antigrief.admin")) return true;
		
		boolean permission = false;
		boolean zoned = false;
		
		if (getValue("settings", "zones.enable").equals(true)) {
			try {
				permission = zoneProtection.access(player_, node_, location_, item_, supress_);
				zoned = true;
			} catch (AntiGriefZoneProtectionException ex) { /**/ }
		}
		
		if (!zoned) {
			String world = player_.getWorld().getName();
			String config = "world." + world;
			
			String group = getGroup(player_);
			boolean canBuild = canGroupBuild(player_, group);
			
			if (group == null) return false;
			if (!canBuild && !hasValue(config, "nodes.buildfalse", node_, "default")) canBuild = true;
			else if (canBuild && hasValue(config, "nodes.buildtrue", node_, "default")) canBuild = false;
	
			boolean prevent = hasPermission(player_, "antigrief.prevent." + node_);
			boolean allow = hasPermission(player_, "antigrief.allow." + node_);

			if (prevent ^ allow) permission = (canBuild && !prevent) || (!canBuild && allow);
			else permission = canBuild;
			
			if (!permission) permission = allowItem(world, node_, item_);
		}
		
		if (!permission && !supress_) {
			int delay = (Integer) getValue("settings", "message.delay");
			int min_delay = (Integer) template_settings.get("message.delay");
			if (delay < min_delay) delay = min_delay;
			String msg = (String) getValue("settings", "message.access");
			if (!msg.equals("")) {
				long time = System.currentTimeMillis() / 1000;
				long prev_time;
				try {
					prev_time = message_delay.get(player_.getName());
				} catch (Exception ex) { prev_time = 0; }
				long diff = time - prev_time;
				if (prev_time == 0 || diff > delay) {
					player_.sendMessage(msg);
					message_delay.put(player_.getName(), time);
				}
			}
		}
		
		return permission;
	}
	protected boolean access(Player player_, String node_, Location location_) { return access(player_, node_, location_, -1); }
	protected boolean access(Player player_, String node_, Location location_, int item_) { return access(player_, node_, location_, item_, false); }
	protected boolean access(Player player_, String node_, Location location_, boolean supress_) { return access(player_, node_, location_, -1, supress_); }
	
	public boolean allowItem(String world_, String node_, int item_, String alternative_) {
		String node;
		if (node_.equals("block.interact")) node = "allow.interact";
		else if (node_.equals("block.damage") || node_.equals("block.place")) node = "allow.block";
		else if (node_.equals("player.item.pickup") || node_.equals("player.item.use")) node = "allow.item";
		else return false;
		
		return hasValue("world." + world_, node, item_, "default");
	}
	public boolean allowItem(String world_, String node_, int item_) { return allowItem(world_, node_, item_, "default"); }
}
