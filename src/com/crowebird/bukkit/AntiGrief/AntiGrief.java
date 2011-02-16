package com.crowebird.bukkit.AntiGrief;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;

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
		ArrayList<String> cignore = new ArrayList<String>();
		cignore.add("");
		
		AntiGrief.default_config.put("level", clevel);
		AntiGrief.default_config.put("ignore", cignore);
		
		AntiGrief.pdf = desc;
	}
	
	public void onEnable() {
		setupPermissions();
		
		try {
			config = readConfig(Config.read(getDataFolder().toString(), "config.yml"));
		} catch (Exception ex) {
			AntiGrief.log.info(ex.toString());
			if (ex instanceof IOException)
				Config.create(getDataFolder().toString(), "config.yml", AntiGrief.default_config);
			config = AntiGrief.default_config;
			AntiGrief.log.info(AntiGrief.pdf.getName() + " - using default values!");
		}
		
		AntiGrief.log.info(AntiGrief.pdf.getName() + " version " + AntiGrief.pdf.getVersion() + " was enabled!");
		
		registerEvents();
	}
	
	private HashMap<String, ArrayList<String>> readConfig(Configuration config_) throws Exception {
		config_.load();

		HashMap<String, ArrayList<String>> hm = new HashMap<String, ArrayList<String>>();
		
		ArrayList<String> clevel = (ArrayList<String>)config_.getStringList("level", null);
		ArrayList<String> cignore = (ArrayList<String>)config_.getStringList("ignore", null);
		
		if (clevel.size() == 0 || cignore.size() == 0) throw new IOException("Invalid Configuration File!");
		
		hm.put("level", clevel);
		hm.put("ignore", cignore);
		
		return hm;
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
		pm.registerEvent(Event.Type.PLAYER_ITEM, playerListener, compile_level, this);
		
		pm.registerEvent(Event.Type.ENTITY_TARGET, entityListener, compile_level, this);
		
		pm.registerEvent(Event.Type.VEHICLE_ENTER, vehicleListener, compile_level, this);
		pm.registerEvent(Event.Type.VEHICLE_COLLISION_ENTITY, vehicleListener, compile_level, this);
		pm.registerEvent(Event.Type.VEHICLE_MOVE, vehicleListener, compile_level, this);
		pm.registerEvent(Event.Type.VEHICLE_DAMAGE, vehicleListener, compile_level, this);
	}
	
	protected boolean canBuild(Player player_, String node_) {
		if (player_ == null) return true;
		String group = AntiGrief.permissions.getGroup(player_.getName());
		boolean canBuild = true;
		if (group != null) {
			canBuild = AntiGrief.permissions.canGroupBuild(group);
			if (!canBuild && this.config.get("ignore").contains(node_))
				canBuild = true;
		}
		return canBuild && (!AntiGrief.permissions.has(player_, node_) || AntiGrief.permissions.has(player_, "antigrief.admin"));
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
