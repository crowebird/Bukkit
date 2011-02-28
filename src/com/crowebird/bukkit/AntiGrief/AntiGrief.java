/**
 * AntiGrief
 * Created by Michael Crowe (crowebird)
 * 
 * Feel free to learn from but give credit! :D
 */

package com.crowebird.bukkit.AntiGrief;

import java.util.ArrayList;
import java.util.logging.Logger;

import org.anjocaido.groupmanager.GroupManager;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.crowebird.bukkit.util.Config;

public class AntiGrief extends JavaPlugin {
	
	//Bukkit 450+ //Bukkit 415+ //GroupManager 0.9e

	public static GroupManager gm;
	public static PluginDescriptionFile pdf;
	
	protected static Logger log = Logger.getLogger("Minecraft");
	
	private AntiGriefBlockListener blockListener;
	private AntiGriefPlayerListener playerListener;
	private AntiGriefEntityListener entityListener;
	//private AntiGriefInventoryListener inventoryListener = new AntiGriefInventoryListener(this);
	private AntiGriefVehicleListener vehicleListener;
	
	protected Config.Type config;
	private Config.Type default_config;
	
	public AntiGrief() {
		AntiGrief.gm = null;
		AntiGrief.log = Logger.getLogger("Minecraft");
		
		blockListener = new AntiGriefBlockListener(this);
		playerListener = new AntiGriefPlayerListener(this);
		entityListener = new AntiGriefEntityListener(this);
		//inventoryListener = new AntiGriefInventoryListener(this);
		vehicleListener = new AntiGriefVehicleListener(this);
		
		config = new Config.Type();
		default_config = new Config.Type();
		
		
		String clevel = "lowest";
		
		Config.ALString caffects = new Config.ALString();
		caffects.add("block.damage");
		caffects.add("block.place");
		caffects.add("block.interact");
		caffects.add("block.ignite");
		
		caffects.add("entity.creeper");
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
		
		caffects.add("player.damage.cause");
		caffects.add("player.item");
		
		caffects.add("vehicle.use");
		caffects.add("vehicle.move");
		
		Config.ALInteger cinteract = new Config.ALInteger();
		cinteract.add(64);
		
		Config.ALInteger citem = new Config.ALInteger();
		
		this.default_config.put("priorityLevel", clevel);
		this.default_config.put("buildFalseNodes", caffects);
		this.default_config.put("allow.interact", cinteract);
		this.default_config.put("allow.item", citem);
	}
	
	public void onEnable() {
		AntiGrief.pdf = getDescription();
		
		if (!setupPermissions()) return;
		
		config = Config.getConfig(AntiGrief.pdf.getName(), getDataFolder().toString(), "config.yml", this.default_config);
		
		registerEvents();
		
		AntiGrief.log.info(AntiGrief.pdf.getName() + " - Version " + AntiGrief.pdf.getVersion() + " Enabled!");
	}
	
	public void onDisable() {
		AntiGrief.log.info(AntiGrief.pdf.getName() + " - Disabled!");
	}
	
	private void registerEvents() {
		PluginManager pm = getServer().getPluginManager();
		Event.Priority compile_level = Event.Priority.Lowest;
		String level = (String)config.get("priorityLevel");
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
		String group = AntiGrief.gm.getPermissionHandler().getGroup(player_.getName());
		boolean canBuild = true;
		if (group != null) {
			canBuild = AntiGrief.gm.getPermissionHandler().canGroupBuild(group);
			if (!canBuild && !this.config.has("buildFalseNodes", node_))
				canBuild = true;
		}
		
		boolean prevent = AntiGrief.gm.getPermissionHandler().has(player_, "antigrief.prevent." + node_);
		boolean allow = AntiGrief.gm.getPermissionHandler().has(player_, "antigrief.allow." + node_);
		boolean permission;
		
		if (AntiGrief.gm.getPermissionHandler().has(player_, "antigrief.admin")) permission = true;
		else {
			if (((prevent && !allow) || (!prevent && allow)) && (!prevent && !allow)) {
				permission = (canBuild && !AntiGrief.gm.getPermissionHandler().has(player_, "antigrief.prevent." + node_)) ||
					(!canBuild && AntiGrief.gm.getPermissionHandler().has(player_, "antigrief.allow." + node_));
			} else permission = canBuild;
		}
		
		return permission;
	}
	
	protected boolean allowInteract(int id_) {
		return this.config.has("allow.interact", id_);
	}
	
	protected boolean allowItem(int id_) {
		return this.config.has("allow.item", id_);
	}

	private boolean setupPermissions() {
		Plugin permissions = this.getServer().getPluginManager().getPlugin("GroupManager");
		if (permissions != null) {
			if (!permissions.isEnabled()) this.getServer().getPluginManager().enablePlugin(permissions);
			AntiGrief.gm = (GroupManager) permissions;
			return true;
		} else {
			AntiGrief.log.severe(AntiGrief.pdf.getFullName() + " - GroupManager not enabled. Disabling.");
			this.getServer().getPluginManager().disablePlugin(this);
			return false;
		}
	}
}
