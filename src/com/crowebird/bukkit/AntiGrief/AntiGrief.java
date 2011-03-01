/**
 * AntiGrief
 * Created by Michael Crowe (crowebird)
 * 
 * Feel free to learn from but give credit! :D
 */

package com.crowebird.bukkit.AntiGrief;

import java.io.File;
import java.util.HashMap;
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
import com.nijikokun.bukkit.Permissions.Permissions;

public class AntiGrief extends JavaPlugin {
	
	//Bukkit 478+ //Bukkit 428+ //GroupManager 0.99c + Permissions 2.4

	public PermissionHandler ph;
	public PluginDescriptionFile pdf;
	public String pversion;
	
	protected Logger log = Logger.getLogger("Minecraft");
	
	private AntiGriefBlockListener blockListener;
	private AntiGriefPlayerListener playerListener;
	private AntiGriefEntityListener entityListener;
	//private AntiGriefInventoryListener inventoryListener = new AntiGriefInventoryListener(this);
	private AntiGriefVehicleListener vehicleListener;
	
	protected Config.Type config;
	private Config.Type default_config_config;
	private Config.Type default_world_config;
	private HashMap<String, Long> delay;
	
	public AntiGrief() {
		this.pversion = null;
		this.ph = null;
		this.log = Logger.getLogger("Minecraft");
		
		blockListener = new AntiGriefBlockListener(this);
		playerListener = new AntiGriefPlayerListener(this);
		entityListener = new AntiGriefEntityListener(this);
		//inventoryListener = new AntiGriefInventoryListener(this);
		vehicleListener = new AntiGriefVehicleListener(this);
		
		config = new Config.Type();
		default_config_config = new Config.Type();
		default_world_config = new Config.Type();
		
		String clevel = "lowest";
		String camessage = "You are not allowed to perform that action!";
		String ccmessage = "You are not allowed to use that command!";
		int cdelay = 5;
		
		this.default_config_config.put("priorityLevel", clevel);
		this.default_config_config.put("message.access", camessage);
		this.default_config_config.put("message.command", ccmessage);
		this.default_config_config.put("message.delayy", cdelay);
		
		Config.ALString cbuildfalse = new Config.ALString();
		cbuildfalse.add("block.damage");
		cbuildfalse.add("block.place");
		cbuildfalse.add("block.interact");
		cbuildfalse.add("block.ignite");
		
		cbuildfalse.add("entity.creeper");
		
		cbuildfalse.add("player.damage.cause");
		cbuildfalse.add("player.item");
		
		cbuildfalse.add("vehicle.use");
		cbuildfalse.add("vehicle.move");
		
		Config.ALString cbuildtrue = new Config.ALString();
		
		Config.ALInteger cinteract = new Config.ALInteger();
		cinteract.add(64);
		
		Config.ALInteger citem = new Config.ALInteger();
		Config.ALInteger cblock = new Config.ALInteger();

		this.default_world_config.put("nodes.buildfalse", cbuildfalse);
		this.default_world_config.put("nodes.buildtrue", cbuildtrue);
		this.default_world_config.put("allow.interact", cinteract);
		this.default_world_config.put("allow.item", citem);
		this.default_world_config.put("allow.block", cblock);
	}
	
	public void onEnable() {
		this.pdf = getDescription();
		
		if (!setupPermissions()) return;
		
		buildConfig();	
	
		registerEvents();
		getCommand("ag").setExecutor(new AntiGriefCommand(this));
		
		this.log.info(this.pdf.getName() + " - Version " + this.pdf.getVersion() + " Enabled!");
	}
	
	public void buildConfig() {
		this.delay = new HashMap<String, Long>();
		this.config = new Config.Type();
		
		this.config.putAll(Config.getConfig(this.pdf.getName(), getDataFolder().toString(), "config", this.default_config_config, true));
		this.config.putAll(Config.getConfig(this.pdf.getName(), getDataFolder().toString(), "default", this.default_world_config, true));
		
		try {
			File files[] = (new File(getDataFolder().toString())).listFiles();
			if (files != null) {
				for (File file : files) {
					if (!file.isFile()) continue;
					String name = file.getName();
					int extension = name.lastIndexOf(".");
					name = name.substring(0, (extension == -1 ? name.length() : extension));
					if (name.equals("config") || name.equals("default")) continue;
					config.putAll(Config.getConfig(this.pdf.getName(), getDataFolder().toString(), name, this.default_world_config, false));
				}
			}
		} catch (Exception ex) { System.out.println(ex.toString()); }
	}
	
	public void onDisable() {
		this.log.info(this.pdf.getName() + " - Disabled!");
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
		
		this.log.info(this.pdf.getName() + " - Using the " + compile_level.toString() + " priority level.");
		
		pm.registerEvent(Event.Type.BLOCK_DAMAGED, blockListener, compile_level, this);
		pm.registerEvent(Event.Type.BLOCK_PLACED, blockListener, compile_level, this);
		pm.registerEvent(Event.Type.BLOCK_INTERACT, blockListener, compile_level, this);
		pm.registerEvent(Event.Type.BLOCK_IGNITE, blockListener, compile_level, this);
		
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
	
	protected boolean access(Player player_, String node_) {
		return access(player_, node_, false);
	}
	
	@SuppressWarnings("deprecation")
	protected boolean access(Player player_, String node_, boolean suppress) {		
		if (player_ == null) return true;
		String group = null;
		if (this.pversion == "GM") group = this.ph.getGroup(player_.getName());
		else if (this.pversion == "P") group = this.ph.getGroup(player_.getWorld().getName(), player_.getName());
		else return false;
		boolean canBuild;
		if (group != null) {
			if (this.pversion == "GM") canBuild = this.ph.canGroupBuild(group);
			else if (this.pversion == "P") canBuild = this.ph.canGroupBuild(player_.getWorld().getName(), group);
			else return false;
			if (!canBuild && !has(player_.getWorld().getName(), "nodes.buildfalse", node_)) canBuild = true;
			else if (canBuild && has(player_.getWorld().getName(), "nodes.buildtrue", node_)) canBuild = false;
		} else canBuild = true;
		

		boolean prevent = this.ph.has(player_, "antigrief.prevent." + node_);
		boolean allow = this.ph.has(player_, "antigrief.allow." + node_);
		boolean permission;
		
		if (this.ph.has(player_, "antigrief.admin")) permission = true;
		else {
			if (prevent ^ allow) permission = (canBuild && !prevent) || (!canBuild && allow);
			else permission = canBuild;
		}
		
		if (!permission && !suppress) {
			int delay = (Integer)this.config.get("config.delay");
			int min_delay = (Integer)this.default_config_config.get("config.delay");
			if (delay < min_delay) delay = min_delay;
			String msg = (String)this.config.get("config.message.access");
			if (!msg.equals("")) {
				long time = System.currentTimeMillis() / 1000;
				long prev_time;
				try {
					prev_time = this.delay.get(player_.getName());
				} catch (Exception ex) { prev_time = 0; }
				this.delay.put(player_.getName(), time);
				long diff = time - prev_time;
				if (prev_time == 0 || diff > delay)
					player_.sendMessage(msg);
			}
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
	
	protected boolean allowBlock(String world_, int id_) {
		return has(world_, "allow.block", id_);
	}

	private boolean setupPermissions() {
		if (this.ph != null) return true;
		Plugin permissions = this.getServer().getPluginManager().getPlugin("GroupManager");
		if (permissions != null) {
			if (!permissions.isEnabled()) this.getServer().getPluginManager().enablePlugin(permissions);
			GroupManager gm = (GroupManager) permissions;
			this.ph = gm.getPermissionHandler();
			this.pversion = "GM";
			return true;
		} else {
			permissions = this.getServer().getPluginManager().getPlugin("Permissions");
			if (permissions != null) {
				if (!permissions.isEnabled()) this.getServer().getPluginManager().enablePlugin(permissions);
				Permissions p = (Permissions) permissions;
				this.ph = p.getHandler();
				this.pversion = "P";
				return true;
			} else {
				this.log.severe(this.pdf.getFullName() + " - No instance of GroupManager or Permissions found. Disabling.");
				this.getServer().getPluginManager().disablePlugin(this);
				return false;
			}
		}
	}
}
