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

package com.crowebird.bukkit.plugins;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.anjocaido.groupmanager.GroupManager;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;

public abstract class BukkitPlugin extends JavaPlugin {

	private final Logger logger;
	private GroupManager gm;
	private PermissionHandler p;
	private boolean permissionsUse, permissionsRequired;
	
	private String name, version;

	/**
	 * Sets up a BukkitPlugin without permissions.
	 * 
	 * @param name_ Name of the plugin
	 * @param version_ Version number of the plugin
	 */
	public BukkitPlugin(String name_, String version_) {
		logger = Logger.getLogger("Minecraft");
		
		gm = null;
		p = null;
		
		permissionsUse = false;
		permissionsRequired = false;
		
		name = name_;
		version = version_;
	}
	/**
	 * Sets up a BukkitPlugin with permissions.
	 * 
	 * The BukkitPlugin will disable itself if it cannot find permissions
	 * if permissionsRequired_ is given true, otherwise
	 * it will enable properly as if permissions is not being used.
	 * 
	 * @param name_ Name of the plugin
	 * @param version_ Version number of the plugin
	 * @param permissionsRequired_ Boolean if permissions are required or not
	 */
	public BukkitPlugin(String name_, String version_, boolean permissionsRequired_) { 
		this(name_, version_);
		permissionsUse = true;
		permissionsRequired = permissionsRequired_;
	}
	
	/* JavaPlugin */
	
	/**
	 * Default method for onEnable.
	 * 
	 * If overriden, it is necessary to call super.onEnable(),
	 * especially if you want permissions.
	 */
	public void onEnable() {
		if (permissionsUse) 
			if (!setupPermissions())
				return;
		registerEvents();
		log("Version " + version + " Enabled!");
	};
	
	/**
	 * Method for disabling.
	 * 
	 * If overriden you can either call super.onDisable(),
	 * or just give your own disable log message.
	 */
	public void onDisable() {
		log("Disabled!");
	};
	
	/* BukkitPlugin */
	
	protected abstract void registerEvents();
	
	/**
	 * Gets the name of the plugin.
	 * 
	 * @return The name of the plugin
	 */
	public String getName() {
		return name;
	};
	
	/**
	 * Gets the version number of the plugin.
	 * 
	 * @return The version number of the plugin
	 */
	public String getVersion() {
		return version;
	};
	
	/* Logging */
	
	/**
	 * General purpose logging of message_ at the INFO level.
	 * 
	 * @param message_ Message to log
	 */
	public void log(String message_) { log(Level.INFO, message_); };
	/**
	 * General purpose logging of message_ at the level_ level.
	 * 
	 * @param level_ Level to log the message at
	 * @param message_ Message to log
	 */
	public void log(Level level_, String message_) {
		logger.log(level_, name + " - " + message_);
	};
	
	/* Permissions */
	
	/**
	 * Sets up permissions for the plugin.  If GroupManager cannout be found,
	 * will disable itself if the plugin requires permissions to work,
	 * otherwise it continues normally.
	 */
	private boolean setupPermissions() {
		if (gm != null) return true;
		Plugin permissions = getServer().getPluginManager().getPlugin("GroupManager");
		if (permissions != null) {
			if (!permissions.isEnabled())
				getServer().getPluginManager().enablePlugin(permissions);
			gm = (GroupManager) permissions;
			return true;
		} else {
			permissions = getServer().getPluginManager().getPlugin("Permissions");
			if (permissions != null) {
				if (!permissions.isEnabled())
					getServer().getPluginManager().enablePlugin(permissions);
				p = ((Permissions)permissions).getHandler();
				return true;
			}
		}
		if (permissionsRequired)
			return true;
		log(Level.SEVERE, "No instance of GroupManager or Permissions found. Disabling.");
		getServer().getPluginManager().disablePlugin(this);
		return false;
	};
	
	/**
	 * Determines if the given player_ has permissions
	 * on node_
	 * 
	 * @param player_ The player to check permissions on
	 * @param node_ The node path of the permission
	 * @return True if the player has permissions, false otherwise.
	 * If for some reason no permission handler found return true
	 */
	public boolean has(Player player_, String node_) {
		if (gm != null) return gm.getWorldsHolder().getWorldPermissions(player_).has(player_, node_);
		else if (p != null) return p.has(player_, node_);
		return true;
	}
	
	/**
	 * Gives the group of the given player_.
	 * 
	 * @param player_ The player we are checking permissions on
	 * @return Group of player_ if it exists and permissions
	 * is enabled, null otherwise
	 */
	public String getGroup(Player player_) {
		if (gm != null) return gm.getWorldsHolder().getWorldPermissions(player_).getGroup(player_.getName());		
		else if (p != null) return p.getGroup(player_.getWorld().getName(), player_.getName());
		return null;
	};
	
	/**
	 * Determines if the player_ in group_ has build rights.
	 * 
	 * @param player_ The player we are checking permissions on
	 * @param group_ The name of the group of the player we are checking
	 * permissions on
	 * @return true if they have build rights or if permisisons
	 * are not enabled, false otherwise
	 */
	public boolean canGroupBuild(Player player_, String group_) {
		if (group_ != null) {
			if (gm != null) return gm.getWorldsHolder().getWorldPermissions(player_).canGroupBuild(group_);
			else if (p != null) return p.canGroupBuild(player_.getWorld().getName(), player_.getName());
		}
		return true;
	};
}
