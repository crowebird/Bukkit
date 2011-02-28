/**
 * AntiGrief
 * Created by Michael Crowe (crowebird)
 * 
 * Feel free to learn from but give credit! :D
 */

package com.crowebird.bukkit.AntiGrief;

import java.io.File;
import java.util.logging.Logger;

import org.anjocaido.groupmanager.GroupManager;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.crowebird.bukkit.util.Config;
import com.nijiko.permissions.PermissionHandler;


public class AntiGrief extends JavaPlugin {
	
	//Bukkit 450+ //Bukkit 415+ //GroupManager 0.9e

	public static PermissionHandler ph;
	public static PluginDescriptionFile pdf;
	
	protected static Logger log = Logger.getLogger("Minecraft");
	
	private AntiGriefBlockListener blockListener;
	private AntiGriefPlayerListener playerListener;
	private AntiGriefEntityListener entityListener;
	//private AntiGriefInventoryListener inventoryListener = new AntiGriefInventoryListener(this);
	private AntiGriefVehicleListener vehicleListener;
	
	private Config.Type config;
	private Config.Type default_config_config;
	private Config.Type default_world_config;
	
	public AntiGrief() {
		AntiGrief.ph = null;
		AntiGrief.log = Logger.getLogger("Minecraft");
		
		blockListener = new AntiGriefBlockListener(this);
		playerListener = new AntiGriefPlayerListener(this);
		entityListener = new AntiGriefEntityListener(this);
		//inventoryListener = new AntiGriefInventoryListener(this);
		vehicleListener = new AntiGriefVehicleListener(this);
		
		config = new Config.Type();
		default_config_config = new Config.Type();
		default_world_config = new Config.Type();
		
		String clevel = "lowest";
		String cmessage = "You are not allowed to perform that action!";
		
		this.default_config_config.put("priorityLevel", clevel);
		this.default_config_config.put("message", cmessage);
		
		Config.ALString cbuildfalse = new Config.ALString();
		cbuildfalse.add("block.damage");
		cbuildfalse.add("block.place");
		cbuildfalse.add("block.interact");
		cbuildfalse.add("block.ignite");
		
		cbuildfalse.add("entity.creeper");
		//caffects.add("entity.damage.block_explosion");
		//caffects.add("entity.damage.contact");
		//caffects.add("entity.damage.drowning");
		//caffects.add("entity.damage.entity_attack");
		//caffects.add("entity.damage.entity_explosion");
		//caffects.add("entity.damage.fire");
		//caffects.add("entity.damage.fire_tick");
		//caffects.add("entity.damage.lava");
		//caffects.add("entity.damage.suffocation");
		//caffects.add("entity.damage.custom");
		
		cbuildfalse.add("player.damage.cause");
		cbuildfalse.add("player.item");
		
		cbuildfalse.add("vehicle.use");
		cbuildfalse.add("vehicle.move");
		
		Config.ALString cbuildtrue = new Config.ALString();
		
		Config.ALInteger cinteract = new Config.ALInteger();
		cinteract.add(64);
		
		Config.ALInteger citem = new Config.ALInteger();

		this.default_world_config.put("nodes.buildfalse", cbuildfalse);
		this.default_world_config.put("nodes.buildtrue", cbuildtrue);
		this.default_world_config.put("allow.interact", cinteract);
		this.default_world_config.put("allow.item", citem);
	}
	
	public void onEnable() {
		AntiGrief.pdf = getDescription();
		
		if (!setupPermissions()) return;
		
		this.config.clear();
		this.config.putAll(Config.getConfig(AntiGrief.pdf.getName(), getDataFolder().toString(), "config", this.default_config_config, true));
		this.config.putAll(Config.getConfig(AntiGrief.pdf.getName(), getDataFolder().toString(), "default", this.default_world_config, true));
		
		try {
			File files[] = (new File(getDataFolder().toString())).listFiles();
			if (files != null) {
				for (File file : files) {
					if (!file.isFile()) continue;
					String name = file.getName();
					int extension = name.lastIndexOf(".");
					name = name.substring(0, (extension == -1 ? name.length() : extension));
					if (name.equals("config") || name.equals("default")) continue;
					config.putAll(Config.getConfig(AntiGrief.pdf.getName(), getDataFolder().toString(), name, this.default_world_config, false));
				}
			}
		} catch (Exception ex) { System.out.println(ex.toString()); }
	
		registerEvents();
		
		AntiGrief.log.info(AntiGrief.pdf.getName() + " - Version " + AntiGrief.pdf.getVersion() + " Enabled!");
	}
	
	public void onDisable() {
		AntiGrief.log.info(AntiGrief.pdf.getName() + " - Disabled!");
	}
	
	private void registerEvents() {
		PluginManager pm = getServer().getPluginManager();
		Event.Priority compile_level = Event.Priority.Lowest;
		String level = (String)config.get("config.priorityLevel");
		if (level.equals("lowest")) compile_level = Event.Priority.Lowest;
		else if (level.equals("low")) compile_level = Event.Priority.Low;
		else if (level.equals("normal")) compile_level = Event.Priority.Normal;
		else if (level.equals("high")) compile_level = Event.Priority.High;
		else if (level.equals("highest")) compile_level = Event.Priority.Highest;
		
		AntiGrief.log.info(AntiGrief.pdf.getName() + " - Using the " + compile_level.toString() + " priority level.");
		
		pm.registerEvent(Event.Type.BLOCK_DAMAGED, blockListener, compile_level, this);
		pm.registerEvent(Event.Type.BLOCK_PLACED, blockListener, compile_level, this);
		pm.registerEvent(Event.Type.BLOCK_INTERACT, blockListener, compile_level, this);
		pm.registerEvent(Event.Type.BLOCK_IGNITE, blockListener, compile_level, this);
		
		pm.registerEvent(Event.Type.ENTITY_TARGET, entityListener, compile_level, this);
		pm.registerEvent(Event.Type.ENTITY_DAMAGED, entityListener, compile_level, this);
		
		pm.registerEvent(Event.Type.PLAYER_ITEM, playerListener, compile_level, this);	
		
		pm.registerEvent(Event.Type.VEHICLE_COLLISION_ENTITY, vehicleListener, compile_level, this);
		pm.registerEvent(Event.Type.VEHICLE_MOVE, vehicleListener, compile_level, this);
		pm.registerEvent(Event.Type.VEHICLE_DAMAGE, vehicleListener, compile_level, this);
		pm.registerEvent(Event.Type.VEHICLE_ENTER, vehicleListener, compile_level, this);
	}
	
	protected boolean canBuild(Player player_, String node_) {
		if (player_ == null) return true;
		String group = AntiGrief.ph.getGroup(player_.getName());
		boolean canBuild;
		if (group != null) {
			canBuild = AntiGrief.ph.canGroupBuild(group);
			if (!canBuild && !has(player_.getWorld().getName(), "nodes.buildfalse", node_)) canBuild = true;
			else if (canBuild && has(player_.getWorld().getName(), "nodes.buildtrue", node_)) canBuild = false;
		} else canBuild = true;
		
		boolean prevent = AntiGrief.ph.has(player_, "antigrief.prevent." + node_);
		boolean allow = AntiGrief.ph.has(player_, "antigrief.allow." + node_);
		boolean permission;
		
		if (AntiGrief.ph.has(player_, "antigrief.admin")) permission = true;
		else {
			if (prevent ^ allow) permission = (canBuild && !prevent) || (!canBuild && allow);
			else permission = canBuild;
		}
		
		if (!permission) {
			String msg = (String)this.config.get("config.message");
			if (!msg.equals("")) player_.sendMessage(msg);
		}
		
		return permission;
	}
	
	protected boolean has(String world_, String node_, Object value_) {
		boolean output = this.config.has(world_ + "." + node_, value_);
		if (!output) return this.config.has("default." + node_, value_);
		return output;
	}
	
	protected boolean allowInteract(String world_, int id_) {
		return has(world_, "allow.interact", id_);
	}
	
	protected boolean allowItem(String world_, int id_) {
		return has(world_, "allow.item", id_);
	}

	private boolean setupPermissions() {
		if (AntiGrief.ph != null) return true;
		Plugin permissions = this.getServer().getPluginManager().getPlugin("GroupManager");
		if (permissions != null) {
			if (!permissions.isEnabled()) this.getServer().getPluginManager().enablePlugin(permissions);
			GroupManager gm = (GroupManager) permissions;
			AntiGrief.ph = gm.getPermissionHandler();
			return true;
		} else {
			AntiGrief.log.severe(AntiGrief.pdf.getFullName() + " - No instance of GroupManager found. Disabling.");
			this.getServer().getPluginManager().disablePlugin(this);
			return false;
		}
	}
}
