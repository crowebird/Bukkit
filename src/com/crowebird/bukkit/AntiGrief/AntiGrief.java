package com.crowebird.bukkit.AntiGrief;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.crowebird.bukkit.util.Config;
import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;

public class AntiGrief extends JavaPlugin {

	public static PermissionHandler permissions = null;
	public static PluginDescriptionFile pdf;
	
	protected static final Logger log = Logger.getLogger("Minecraft");
	
	private AntiGriefBlockListener blockListener = new AntiGriefBlockListener(this);
	private AntiGriefPlayerListener playerListener = new AntiGriefPlayerListener(this);
	private AntiGriefEntityListener entityListener = new AntiGriefEntityListener(this);
	//private AntiGriefInventoryListener inventoryListener = new AntiGriefInventoryListener(this);
	private AntiGriefVehicleListener vehicleListener = new AntiGriefVehicleListener(this);
	
	protected HashMap<String, ArrayList<String>> config = new HashMap<String, ArrayList<String>>();
	private static final HashMap<String, ArrayList<String>> default_config = new HashMap<String, ArrayList<String>>();
	
	public AntiGrief(PluginLoader pluginLoader, Server instance,
			PluginDescriptionFile desc, File folder, File plugin,
			ClassLoader cLoader) {
		super(pluginLoader, instance, desc, folder, plugin, cLoader);

		ArrayList<String> clevel = new ArrayList<String>();
		clevel.add("lowest");
		ArrayList<String> cdefault = new ArrayList<String>();
		cdefault.add("antigrief.block.*");
		cdefault.add("antigrief.entity.*");
		cdefault.add("antigrief.player.*");
		cdefault.add("antigrief.vehicle.*");
		
		AntiGrief.default_config.put("level", clevel);
		AntiGrief.default_config.put("default", cdefault);
		
		AntiGrief.pdf = desc;
		
		registerEvents();
	}
	
	public void onEnable() {
		setupPermissions();
		try {
			config = Config.read("AntiGrief", "config.yml");
		} catch (IOException ex) {
			Config.create("AntiGrief", "config.yml", this.config);
			try {
				config = Config.read("AntiGrief", "config.yml");
			} catch (Exception ex1) {
				config = AntiGrief.default_config;
				AntiGrief.log.info(AntiGrief.pdf.getName() + " - using default values!");
			}
		}
		
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
		
		pm.registerEvent(Event.Type.BLOCK_DAMAGED, blockListener, compile_level, this);
		pm.registerEvent(Event.Type.BLOCK_PLACED, blockListener, compile_level, this);
		pm.registerEvent(Event.Type.BLOCK_INTERACT, blockListener, compile_level, this);
		pm.registerEvent(Event.Type.BLOCK_IGNITE, blockListener, compile_level, this);
		pm.registerEvent(Event.Type.PLAYER_ITEM, playerListener, compile_level, this);
		
		//pm.registerEvent(Event.Type.ENTITY_EXPLODE, entityListener, compile_level, this);
		pm.registerEvent(Event.Type.ENTITY_TARGET, entityListener, compile_level, this);
		
		pm.registerEvent(Event.Type.VEHICLE_ENTER, vehicleListener, compile_level, this);
		pm.registerEvent(Event.Type.VEHICLE_COLLISION_ENTITY, vehicleListener, compile_level, this);
		pm.registerEvent(Event.Type.VEHICLE_MOVE, vehicleListener, compile_level, this);
		pm.registerEvent(Event.Type.VEHICLE_DAMAGE, vehicleListener, compile_level, this);
	}
	
	protected boolean canBuild(Player player_, String node_) {
		//If we don't have player information, we can't pass judgement
		if (player_ == null) return true;
		String group = AntiGrief.permissions.getGroup(player_.getName());
		if (group != null)
			 if (!AntiGrief.permissions.canGroupBuild(group))
				 AntiGrief.permissions.
				 
				 this.config.get("level").
		boolean sub_permission = !AntiGrief.permissions.has(player_, node_);
		return (group != null && AntiGrief.permissions.canGroupBuild(group)) || sub_permission;
	}
	
	//Nijikokun Permissions Handle:
	//We are checking the build parameter, so without permissions just disable AntiGrief.
	private void setupPermissions() {
		Plugin permissions = this.getServer().getPluginManager().getPlugin("Permissions");
		if (AntiGrief.permissions == null) {
			if (permissions != null) AntiGrief.permissions = ((Permissions)permissions).getHandler();
			else {
				log.info("Permission system not enabled. Disabling AntiGrief.");
				this.getServer().getPluginManager().disablePlugin(this);
			}
		}
	}
}
