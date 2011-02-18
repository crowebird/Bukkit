/**
 * AntiGrief
 * Created by Michael Crowe (crowebird)
 * 
 * Feel free to use and learn, but give credit :D
 */

package com.crowebird.bukkit.AntiGrief;

import java.io.File;
import java.util.ArrayList;
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
	
	//Bukkit 341+

	public static PermissionHandler permissions = null;
	public static PluginDescriptionFile pdf;
	
	protected static final Logger log = Logger.getLogger("Minecraft");
	
	private AntiGriefBlockListener blockListener = new AntiGriefBlockListener(this);
	private AntiGriefPlayerListener playerListener = new AntiGriefPlayerListener(this);
	private AntiGriefEntityListener entityListener = new AntiGriefEntityListener(this);
	//private AntiGriefInventoryListener inventoryListener = new AntiGriefInventoryListener(this);
	private AntiGriefVehicleListener vehicleListener = new AntiGriefVehicleListener(this);
	
	protected Config.type config = new Config.type();
	private static final Config.type default_config = new Config.type();
	
	public AntiGrief(PluginLoader pluginLoader, Server instance,
			PluginDescriptionFile desc, File folder, File plugin,
			ClassLoader cLoader) {
		super(pluginLoader, instance, desc, folder, plugin, cLoader);

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
		
		caffects.add("player.item");
		
		caffects.add("vehicle.use");
		caffects.add("vehicle.move");
		
		ArrayList<String> cinteract = new ArrayList<String>();
		cinteract.add("64");
		
		AntiGrief.default_config.put("level", clevel);
		AntiGrief.default_config.put("affects", caffects);
		AntiGrief.default_config.put("canInteract", cinteract);
		
		AntiGrief.pdf = desc;
	}
	
	public void onEnable() {
		setupPermissions();
		
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
		String group = AntiGrief.permissions.getGroup(player_.getName());
		boolean canBuild = true;
		if (group != null) {
			canBuild = AntiGrief.permissions.canGroupBuild(group);
			if (!canBuild && !this.config.get("affects").contains(node_))
				canBuild = true;
		}
		return (
			(canBuild && !AntiGrief.permissions.has(player_, "antigrief.prevent." + node_)) ||
			(!canBuild && AntiGrief.permissions.has(player_, "antigrief.allow." + node_))
		) || AntiGrief.permissions.has(player_, "antigrief.admin");
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
