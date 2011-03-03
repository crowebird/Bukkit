/**
 * AntiGrief
 * Created by Michael Crowe (crowebird)
 * 
 * Feel free to learn from but give credit! :D
 */

package com.crowebird.bukkit.AntiGrief;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Logger;

import org.anjocaido.groupmanager.GroupManager;
import org.anjocaido.groupmanager.permissions.AnjoPermissionsHandler;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.crowebird.bukkit.AntiGrief.ZoneProtection.AntiGriefZoneProtection;
import com.crowebird.bukkit.AntiGrief.ZoneProtection.AntiGriefZoneProtectionException;
import com.crowebird.bukkit.util.Config;

public class AntiGrief extends JavaPlugin {
	
	//Bukkit 493+ //Bukkit 432+ //GroupManager 1.0 pre-alpha 2

	public GroupManager gm;
	public PluginDescriptionFile pdf;
	
	protected Logger log = Logger.getLogger("Minecraft");
	
	private AntiGriefBlockListener blockListener;
	private AntiGriefPlayerListener playerListener;
	private AntiGriefEntityListener entityListener;
	//private AntiGriefInventoryListener inventoryListener = new AntiGriefInventoryListener(this);
	private AntiGriefVehicleListener vehicleListener;
	
	protected AntiGriefZoneProtection zoneProtection;
	
	protected Config.Type config;
	private Config.Type default_config_config;
	private Config.Type default_world_config;
	private Config.Type default_zone_config;
	private HashMap<String, Long> delay;
	
	public AntiGrief() {
		gm = null;
		log = Logger.getLogger("Minecraft");
		
		blockListener = new AntiGriefBlockListener(this);
		playerListener = new AntiGriefPlayerListener(this);
		entityListener = new AntiGriefEntityListener(this);
		//inventoryListener = new AntiGriefInventoryListener(this);
		vehicleListener = new AntiGriefVehicleListener(this);
		
		config = new Config.Type();
		default_config_config = new Config.Type();
		default_world_config = new Config.Type();
		default_zone_config = new Config.Type();
		
		String clevel = "lowest";
		String camessage = "You are not allowed to perform that action!";
		String ccmessage = "You are not allowed to use that command!";
		int cdelay = 5;
		boolean czones = true;
		
		default_config_config.put("priorityLevel", clevel);
		default_config_config.put("message.access", camessage);
		default_config_config.put("message.command", ccmessage);
		default_config_config.put("message.delay", cdelay);
		default_config_config.put("zones", czones);
		
		Config.ALString cbuildfalse = new Config.ALString();
		cbuildfalse.add("block.damage");
		cbuildfalse.add("block.place");
		cbuildfalse.add("block.interact");
		cbuildfalse.add("block.ignite");
		
		cbuildfalse.add("entity.creeper");
		
		cbuildfalse.add("player.damage.cause");
		cbuildfalse.add("player.item.use");
		cbuildfalse.add("player.item.pickup");
		
		cbuildfalse.add("vehicle.use");
		cbuildfalse.add("vehicle.move");
		
		Config.ALString cbuildtrue = new Config.ALString();
		
		Config.ALInteger cinteract = new Config.ALInteger();
		cinteract.add(64);
		
		Config.ALInteger citem = new Config.ALInteger();
		Config.ALInteger cblock = new Config.ALInteger();

		default_world_config.put("nodes.buildfalse", cbuildfalse);
		default_world_config.put("nodes.buildtrue", cbuildtrue);
		default_world_config.put("allow.interact", cinteract);
		default_world_config.put("allow.item", citem);
		default_world_config.put("allow.block", cblock);
		
		String zcreator = "";
		String zparent = "";
		Config.ALString zpoints = new Config.ALString();
		Config.ALInteger gzitem = new Config.ALInteger();
		Config.ALInteger gzinteract = new Config.ALInteger();
		Config.ALInteger gzblock = new Config.ALInteger();
		Config.ALInteger uzitem = new Config.ALInteger();
		Config.ALInteger uzinteract = new Config.ALInteger();
		Config.ALInteger uzblock = new Config.ALInteger();
		Config.ALString gzprevent = new Config.ALString();
		Config.ALString uzprevent = new Config.ALString();
		
		default_zone_config.put("creator", zcreator);
		default_zone_config.put("parent", zparent);
		default_zone_config.put("points", zpoints);
		default_zone_config.put("groups.*.allow.item", gzitem);
		default_zone_config.put("groups.*.allow.interact", gzinteract);
		default_zone_config.put("groups.*.allow.block", gzblock);
		default_zone_config.put("groups.*.nodes.prevent", gzprevent);
		default_zone_config.put("users.*.allow.item", uzitem);
		default_zone_config.put("users.*.allow.interact", uzinteract);
		default_zone_config.put("users.*.allow.block", uzblock);
		default_zone_config.put("users.*.nodes.prevent", uzprevent);
	}
	
	public void onEnable() {
		pdf = getDescription();
		
		if (!setupPermissions()) return;
		
		
		
		zoneProtection = new AntiGriefZoneProtection(this);
		
		buildConfig();	
	
		registerEvents();
		getCommand("ag").setExecutor(new AntiGriefCommand(this));
		
		log.info(pdf.getName() + " - Version " + pdf.getVersion() + " Enabled!");
	}
	
	public void buildConfig() {
		delay = new HashMap<String, Long>();
		config = new Config.Type();
		
		config.putAll(Config.getConfig(pdf.getName(), getDataFolder().toString(), "config", default_config_config, true, false));
		config.putAll(Config.getConfig(pdf.getName(), getDataFolder().toString(), "default", default_world_config, true, false));
		
		try {
			File files[] = (new File(getDataFolder().toString())).listFiles();
			if (files != null) {
				for (File file : files) {
					if (!file.isFile()) continue;
					String name = file.getName();
					int extension = name.lastIndexOf(".");
					name = name.substring(0, (extension == -1 ? name.length() : extension));
					if (name.equals("config") || name.equals("default")) continue;
					config.putAll(Config.getConfig(pdf.getName(), getDataFolder().toString(), name, default_world_config, false, false));
				}
			}
		} catch (Exception ex) { System.out.println(ex.toString()); }
		
		if (config.get("config.zones").equals(false)) return;
		try {
			new File(getDataFolder().toString() + File.separator + "zones").mkdir();
			File dirs[] = (new File(getDataFolder().toString() + File.separator + "zones")).listFiles();
			if (dirs != null) {
				for (File dir : dirs) {
					if (!dir.isDirectory()) continue;
					String world = dir.getName();
					File files[] = (new File(getDataFolder().toString() + File.separator + "zones" + File.separator + world)).listFiles();
					if (files != null) {
						for(File file : files) {
							String name = file.getName();
							int extension = name.lastIndexOf(".");
							name = name.substring(0, (extension == -1 ? name.length() : extension));
							zoneProtection.addZone(world, name, Config.read(pdf.getName(), getDataFolder().toString() + File.separator + "zones" + File.separator + world, name, default_zone_config, true, false));
						}
					}
				}
			}
		} catch (IOException ex) { System.out.println(ex.toString()); }
	}
	
	public void onDisable() {
		log.info(pdf.getName() + " - Disabled!");
	}
	
	private Event.Priority registerEvents() {
		PluginManager pm = getServer().getPluginManager();
		Event.Priority compile_level = Event.Priority.Lowest;
		String level = (String)config.get("config.priorityLevel");
		if (level.equals("lowest")) compile_level = Event.Priority.Lowest;
		else if (level.equals("low")) compile_level = Event.Priority.Low;
		else if (level.equals("normal")) compile_level = Event.Priority.Normal;
		else if (level.equals("high")) compile_level = Event.Priority.High;
		else if (level.equals("highest")) compile_level = Event.Priority.Highest;
		
		log.info(pdf.getName() + " - Using the " + compile_level.toString() + " priority level.");
		
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
		
		return compile_level;
	}
	
	protected boolean access(Player player_, String node_) { return access(player_, node_, -1); }
	protected boolean access(Player player_, String node_, boolean suppress_) { return access(player_, node_, -1, suppress_); }
	protected boolean access(Player player_, String node_, int item_) { return access(player_, node_, item_, false); }
	protected boolean access(Player player_, String node_, int item_, boolean suppress_) { 
		if (player_ == null) return true;
		
		boolean permission = false;
		boolean zoned = false;
		
		if (config.get("config.zones").equals(true)) {
			try {
				permission = zoneProtection.access(player_, node_, item_, suppress_);
				zoned = true;
			} catch (AntiGriefZoneProtectionException ex) { /**/ }
		}
		
		if (!zoned) {
			String world = player_.getWorld().getName();
			
			String group = getGroup(player_);
			boolean canBuild = canBuild(player_, group);
			
			if (group == null) return false;
			if (!canBuild && !Config.has(config, world, "nodes.buildfalse", node_, "default")) canBuild = true;
			else if (canBuild && Config.has(config, world, "nodes.buildtrue", node_, "default")) canBuild = false;
	
			boolean prevent = worldPermissions(player_).has(player_, "antigrief.prevent." + node_);
			boolean allow = worldPermissions(player_).has(player_, "antigrief.allow." + node_);
			
			if (worldPermissions(player_).has(player_, "antigrief.admin")) permission = true;
			else {
				if (prevent ^ allow) permission = (canBuild && !prevent) || (!canBuild && allow);
				else permission = canBuild;
			}
			
			if (!permission) permission = allowItem(world, node_, item_);
		}
		
		if (!permission && !suppress_) {
			int delay = (Integer)config.get("config.message.delay");
			int min_delay = (Integer)default_config_config.get("message.delay");
			if (delay < min_delay) delay = min_delay;
			String msg = (String)config.get("config.message.access");
			if (!msg.equals("")) {
				long time = System.currentTimeMillis() / 1000;
				long prev_time;
				try {
					prev_time = this.delay.get(player_.getName());
				} catch (Exception ex) { prev_time = 0; }
				long diff = time - prev_time;
				if (prev_time == 0 || diff > delay) {
					player_.sendMessage(msg);
					this.delay.put(player_.getName(), time);
				}
			}
		}
		
		return permission;
	}
	
	public boolean allowItem(String world_, String node_, int item_) { return allowItem(world_, node_, item_, "default"); }
	public boolean allowItem(String world_, String node_, int item_, String alternative_) {
		String node;
		if (node_.equals("block.interact")) node = "allow.interact";
		else if (node_.equals("block.damage") || node_.equals("block.place")) node = "allow.block";
		else if (node_.equals("player.item.pickup") || node_.equals("player.item.use")) node = "allow.item";
		else return false;
		
		return Config.has(config, world_, node, item_, alternative_);
	}
	
	protected boolean canBuild(Player player_, String group_) {
		if (group_ != null) return  worldPermissions(player_).canGroupBuild(group_);
		else return false;
	}
	
	public String getGroup(Player player_) {
		return worldPermissions(player_).getGroup(player_.getName());		
	}
	
	public AnjoPermissionsHandler worldPermissions(Player player_) {
		return gm.getWorldsHolder().getWorldPermissions(player_);
	}

	private boolean setupPermissions() {
		if (gm != null) return true;
		Plugin permissions = getServer().getPluginManager().getPlugin("GroupManager");
		if (permissions != null) {
			if (!permissions.isEnabled()) getServer().getPluginManager().enablePlugin(permissions);
			gm = (GroupManager) permissions;
			return true;
		} else {
			log.severe(pdf.getFullName() + " - No instance of GroupManager or Permissions found. Disabling.");
			getServer().getPluginManager().disablePlugin(this);
			return false;
		}
	}
}
