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

package com.crowebird.bukkit.plugins.AntiGrief;

import java.util.HashMap;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class AntiGriefCommand implements CommandExecutor {

	private final AntiGrief plugin;
	
	private HashMap<String, ItemStack> ZPtools;
	
	public AntiGriefCommand(AntiGrief plugin_) {
		plugin = plugin_;
		ZPtools = new HashMap<String, ItemStack>();
	}

	@Override
	public boolean onCommand(CommandSender sender_, Command command_, String label_, String[] args_) {
		if (args_.length == 0) return false;
		
		if (args_.length == 1) {
			if (args_[0].equals("-r")) {
				if (sender_ instanceof Player) {
					Player player = (Player)sender_;
					if (!plugin.hasPermission(player, "antigrief.reload")) {
						String msg = (String)plugin.getValue("settings", "message.command");
						if (!msg.equals("")) plugin.sendMessage(player, msg);
					} else {
						plugin.buildConfig();
						player.sendMessage("AntiGrief reloaded!");
					}
				} else {
					plugin.buildConfig();
					plugin.log("AntiGrief reloaded!");
				}
				return true;
			}
		}
		if (sender_ instanceof Player) {
			Player player = (Player)sender_;
			if (args_.length == 2) {
				if (args_[1].equals("-s")) {
					if (plugin.zoneProtection.isBuilding(player.getName())) {
						plugin.zoneProtection.cancelZone(player.getName());
					} else player.sendMessage("You are not creating a zone!");
					return true;
				} else if (args_[1].equals("-f"))  {
					if (plugin.zoneProtection.isBuilding(player.getName())) {
						if (plugin.zoneProtection.finishZone(player)) {
							ItemStack i = (ItemStack)ZPtools.get(player.getName());
							if (i.getTypeId() == 0) player.getInventory().clear(0);
							else player.getInventory().setItem(0, i);
							player.sendMessage("Zone created!");
						}
					} else 
						player.sendMessage("You are not creating a zone!");
					return true;
				} else {
					String msg = (String)this.plugin.getValue("settings", "message.command");
					if (!msg.equals("")) player.sendMessage(msg);
					return true;
				}
			} else if (args_.length == 3) {
				if (args_[0].equals("-z")) {
					if (plugin.hasPermission(player, "antigrief.zone.create")) {
						String zone = args_[1];
						if (args_[2].equals("-c")) {
							if (plugin.zoneProtection.isBuilding(player.getName()))
								player.sendMessage("You are already building a zone!");
							else {
								if (plugin.zoneProtection.createZone(player, zone)) {
									ZPtools.put(player.getName(), player.getInventory().getItem(0));
									player.getInventory().setItem(0, (new ItemStack((Integer)plugin.getValue("settings", "zones.tool"), 1)));
									player.sendMessage("Zone creation tool given!");
								}
							}
							return true;
						} else if (args_[2].equals("-v")) {
							plugin.zoneProtection.visulize(player, args_[1]);
							return true;
						}
					} else {
						String msg = (String)this.plugin.getValue("settings", "message.command");
						if (!msg.equals("")) player.sendMessage(msg);
						return true;
					}
				}
			} else if (args_.length == 4) {
				if (args_[0].equals("-z")) {
					if (plugin.hasPermission(player, "antigrief.zone.create")) {
						if (args_[2].equals("-au")) {
							plugin.zoneProtection.addUser(player, args_[1], args_[3]);
							return true;
						} else if (args_[2].equals("-ag")) {
							plugin.zoneProtection.addGroup(player, args_[1], args_[3]);
							return true;
						} else if (args_[2].equals("-du")) {
							plugin.zoneProtection.removeUser(player, args_[1], args_[3]);
							return true;
						} else if (args_[2].equals("-dg")) {
							plugin.zoneProtection.removeGroup(player, args_[1], args_[3]);
							return true;
						}
					} else {
						String msg = (String)this.plugin.getValue("settings", "message.command");
						if (!msg.equals("")) player.sendMessage(msg);
						return true;
					}
				}
			}
		}
		return false;
	}
}
