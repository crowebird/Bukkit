/**
 * AntiGrief
 * Created by Michael Crowe (crowebird)
 * 
 * Feel free to use and learn, but give credit :D
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
	
	//Bukkit 432+ //GroupManager 0.9e

	public static GroupManager gm;
	public static PluginDescriptionFile pdf;
	
	protected static Logger log = Logger.getLogger("Minecraft");
	
	private AntiGriefBlockListener blockListener;
	private AntiGriefPlayerListener playerListener;
	private AntiGriefEntityListener entityListener;
	//private AntiGriefInventoryListener inventoryListener = new AntiGriefInventoryListener(this);
	private AntiGriefVehicleListener vehicleListener;
	
	protected Config.type config;
	private static Config.type default_config;
	
	public AntiGrief() {
		AntiGrief.gm = null;
		AntiGrief.log = Logger.getLogger("Minecraft");
		
		blockListener = new AntiGriefBlockListener(this);
		playerListener = new AntiGriefPlayerListener(this);
		entityListener = new AntiGriefEntityListener(this);
		//inventoryListener = new AntiGriefInventoryListener(this);
		vehicleListener = new AntiGriefVehicleListener(this);
		
		config = new Config.type();
		default_config = new Config.type();
		
		
		ArrayList<String> clevel = new ArrayList<String>();
		clevel.add("lowest");
		
		ArrayList<String> caffects = new ArrayList<String>();
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
		
		caffects.add("player.item");
		
		caffects.add("vehicle.use");
		caffects.add("vehicle.move");
		
		ArrayList<String> cinteract = new ArrayList<String>();
		cinteract.add("64");
		
		AntiGrief.default_config.put("level", clevel);
		AntiGrief.default_config.put("affects", caffects);
		AntiGrief.default_config.put("canInteract", cinteract);
	}
	
	public void onEnable() {
		AntiGrief.pdf = getDescription();
		
		if (!setupPermissions()) return;
		
		config = Config.getConfig(getDataFolder().toString(), "config.yml", AntiGrief.default_config);
		
		registerEvents();
		
		AntiGrief.log.info(AntiGrief.pdf.getName() + " version " + AntiGrief.pdf.getVersion() + " was enabled!");
	}
	
	public void onDisable() {
		AntiGrief.log.info(AntiGrief.pdf.getName() + " version " + AntiGrief.pdf.getVersion() + " was disabled!");
	}
	
	private void registerEvents() {
		PluginManager pm = getServer().getPluginManager();
		Event.Priority compile_level = Event.Priority.Lowest;
		ArrayList<String> level = config.get("level");
		if (level.contains("lowest")) compile_level = Event.Priority.Lowest;
		else if (level.contains("low")) compile_level = Event.Priority.Low;
		else if (level.contains("normal")) compile_level = Event.Priority.Normal;
		else if (level.contains("high")) compile_level = Event.Priority.High;
		else if (level.contains("highest")) compile_level = Event.Priority.Highest;
		
		AntiGrief.log.info(AntiGrief.pdf.getName() + " is running on the " + compile_level.toString() + " priority level.");
		
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
	}
	
	protected boolean canBuild(Player player_, String node_) {
		if (player_ == null) return true;
		String group = AntiGrief.gm.getPermissionHandler().getGroup(player_.getName());
		boolean canBuild = true;
		if (group != null) {
			canBuild = AntiGrief.gm.getPermissionHandler().canGroupBuild(group);
			if (!canBuild && !this.config.get("affects").contains(node_))
				canBuild = true;
		}
		return (
			(canBuild && !AntiGrief.gm.getPermissionHandler().has(player_, "antigrief.prevent." + node_)) ||
			(!canBuild && AntiGrief.gm.getPermissionHandler().has(player_, "antigrief.allow." + node_))
		) || AntiGrief.gm.getPermissionHandler().has(player_, "antigrief.admin");
	}
	
	protected boolean allowInteract(int id_) {
		return contains("canInteract", id_ + "");
	}
	
	private boolean contains(String key_, String value_) {
		ArrayList<String> values = this.config.get(key_);
		if (values != null)
			return values.contains(value_);
		return false;
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
