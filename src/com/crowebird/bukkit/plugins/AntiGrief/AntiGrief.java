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
 * Using CraftBukkit 670+
 * (Bukkit 652+)
 * 
 * With Permissions:
 * - GroupManager 1.0 alpha 5
 * - Permissions 2.5.5
*/

package com.crowebird.bukkit.plugins.AntiGrief;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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

	public AntiGriefZoneProtection zoneProtection;
	
	private HashMap<String, Long> message_delay;
	
	private final ConfigTemplate template_settings;
	private final ConfigTemplate template_world;
	public final ConfigTemplate template_zone;
	
	public AntiGrief() {
		super("AntiGrief", "1.0.2", true);
		
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
		template_world.put("buildfalse.prevent_nodes", new ConfigArrayListString(
			"damage",
			"ignite",
			"target_creeper",
			"hit",
			"use",
			"pickup",
			"vehicle_use",
			"vehicle_move"
		));
		template_world.put("buildtrue.prevent_nodes", new ConfigArrayListString());
		template_world.put("buildfalse.allow_nodes", new ConfigArrayListString());
		template_world.put("buildtrue.allow_nodes", new ConfigArrayListString());
		template_world.put("buildfalse.interact.allow", new ConfigArrayListInteger(64));
		template_world.put("buildfalse.use.allow", new ConfigArrayListInteger());
		template_world.put("buildfalse.place.allow", new ConfigArrayListInteger());
		template_world.put("buildfalse.*.prevent", new ConfigArrayListInteger());
		template_world.put("buildfalse.*.allow", new ConfigArrayListInteger());
		template_world.put("groups.*.prevent_nodes", new ConfigArrayListString());
		template_world.put("groups.*.allow_nodes", new ConfigArrayListString());
		template_world.put("groups.*.*.prevent", new ConfigArrayListInteger());
		template_world.put("groups.*.*.allow", new ConfigArrayListInteger());
		template_world.put("users.*.prevent_nodes", new ConfigArrayListString());
		template_world.put("users.*.allow_nodes", new ConfigArrayListString());
		template_world.put("users.*.*.prevent", new ConfigArrayListInteger());
		template_world.put("users.*.*.allow", new ConfigArrayListInteger());
		
		template_zone = new ConfigTemplate();
		template_zone.put("creator", "");
		template_zone.put("parent", "");
		template_zone.put("points", new ConfigArrayListString());
		template_zone.put("message.enter", "");
		template_zone.put("groups.*.prevent_nodes", new ConfigArrayListString());
		template_zone.put("groups.*.allow_nodes", new ConfigArrayListString());
		template_zone.put("groups.*.*.allow", new ConfigArrayListInteger());
		template_zone.put("groups.*.*.prevent", new ConfigArrayListInteger());
		template_zone.put("users.*.prevent_nodes", new ConfigArrayListString());
		template_zone.put("users.*.allow_nodes", new ConfigArrayListString());
		template_zone.put("users.*.*.allow", new ConfigArrayListInteger());
		template_zone.put("users.*.*.prevent", new ConfigArrayListInteger());
	}
	
	public void onEnable() {
		zoneProtection = new AntiGriefZoneProtection(this);
		getCommand("ag").setExecutor(new AntiGriefCommand(this));
		
		super.onEnable();
	}
	
	public void buildConfig() {
		super.buildConfig();
		
		configs.put("settings", new Config(this, getDataFolder().toString(), "settings", template_settings));
		configs.put("default", new Config(this, getDataFolder().toString(), "default", template_world));

		File worlddirs[] = (new File(getDataFolder().toString())).listFiles();
		if (worlddirs != null) {
			for (File dir : worlddirs) {
				if (!dir.isDirectory()) continue;
				String world = dir.getName();
				File config = new File(getDataFolder().toString() + File.separator + world + File.separator + "config.yml");
				if (!config.exists()) continue;
				configs.put("world." + world, new Config(this, getDataFolder().toString() + File.separator + world, "config", template_world));
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
		
		if (configs.get("settings").getValue("zones.enable").equals(true));
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
		
		pm.registerEvent(Event.Type.BLOCK_DAMAGE, blockListener, compile_level, this);
		pm.registerEvent(Event.Type.BLOCK_PLACE, blockListener, compile_level, this);
		pm.registerEvent(Event.Type.BLOCK_IGNITE, blockListener, compile_level, this);
		pm.registerEvent(Event.Type.BLOCK_BREAK, blockListener, compile_level, this);
		
		pm.registerEvent(Event.Type.ENTITY_TARGET, entityListener, compile_level, this);
		pm.registerEvent(Event.Type.ENTITY_DAMAGE, entityListener, compile_level, this);
		
		pm.registerEvent(Event.Type.PLAYER_INTERACT, playerListener, compile_level, this);
		pm.registerEvent(Event.Type.PLAYER_PICKUP_ITEM, playerListener, compile_level, this);
		
		pm.registerEvent(Event.Type.VEHICLE_COLLISION_ENTITY, vehicleListener, compile_level, this);
		pm.registerEvent(Event.Type.VEHICLE_MOVE, vehicleListener, compile_level, this);
		pm.registerEvent(Event.Type.VEHICLE_DAMAGE, vehicleListener, compile_level, this);
		pm.registerEvent(Event.Type.VEHICLE_ENTER, vehicleListener, compile_level, this);
		
		log("Using the " + compile_level.toString() + " priority level.");
	}

	public boolean getPermission(String config_, Player player_, String node_, int item_) {
		boolean permission = true;
		
		String group = getGroup(player_);
		if (group == null) return false;
		
		boolean canBuild = canGroupBuild(player_, group);
		
		int ca = checkAccess(config_, canBuild ? "buildtrue" : "buildfalse", node_, item_);
		if (ca != -1) permission = (ca == 1 ? true : false);
		
		ca = checkAccess(config_, "groups." + group, node_, item_);
		if (ca != -1) permission = (ca == 1 ? true : false);
		
		ca = checkAccess(config_, "users." + player_.getName(), node_, item_);
		if (ca != -1) permission = (ca == 1 ? true : false);
		
		return permission;
	}
	
	public boolean access(Player player_, String node_, Location location_, int item_, boolean supress_) {
		if (player_ == null) return true;
		
		if (hasPermission(player_, "antigrief.admin")) return true;
		
		boolean permission = true;
		boolean zoned = false;
		
		if (getValue("settings", "zones.enable").equals(true)) {
			try {
				permission = zoneProtection.access(player_, node_, location_, item_, supress_);
				zoned = true;
			} catch (AntiGriefZoneProtectionException ex) {  }
		}
		
		if (!zoned) 	
			permission = getPermission("world." + player_.getWorld().getName(), player_, node_, item_);
		
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
	public boolean access(Player player_, String node_, Location location_) { return access(player_, node_, location_, -1); }
	public boolean access(Player player_, String node_, Location location_, int item_) { return access(player_, node_, location_, item_, false); }
	public boolean access(Player player_, String node_, Location location_, boolean supress_) { return access(player_, node_, location_, -1, supress_); }
	
	@SuppressWarnings("unchecked")
	private int checkAccess(String config_, String path_, String node_, int item_) {
		boolean prevent_node = hasValue(config_, path_ + ".prevent_nodes", node_, "default");
		boolean allow_node = hasValue(config_, path_ + ".allow_nodes", node_, "default");
		if (allow_node)
			return 1;
		if (prevent_node)
			return 0;
		
		if (item_ == -1)
			return -1;
		
		ArrayList<Integer> prevent_item = (ArrayList<Integer>) getValue(config_, path_ + "." + node_ + ".prevent", "default");
		ArrayList<Integer> allow_item = (ArrayList<Integer>) getValue(config_, path_ + "." + node_ + ".allow", "default");
		
		boolean prevent_has_item = false;
		boolean allow_has_item = false;
		if (prevent_item != null) {
			allow_has_item = true;
			prevent_has_item = prevent_item.contains(item_);
		}
		if (allow_item != null) {
			prevent_has_item = true;
			allow_has_item = allow_item.contains(item_);
		}
		
		if (allow_has_item)
			return 1;
		if (prevent_has_item)
			return 0;
		
		boolean prevent_id = hasValue(config_, path_ + ".id.prevent", item_, "default");
		boolean allow_id = hasValue(config_, path_ + ".id.allow", item_, "default");	
		if (allow_id)
			return 1;
		if (prevent_id)
			return 0;
		return -1;
	}
}
