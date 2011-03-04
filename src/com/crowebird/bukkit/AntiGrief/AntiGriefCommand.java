package com.crowebird.bukkit.AntiGrief;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AntiGriefCommand implements CommandExecutor {

	private final AntiGrief plugin;
	
	public AntiGriefCommand(AntiGrief plugin_) {
		this.plugin = plugin_;
	}

	@Override
	public boolean onCommand(CommandSender sender_, Command command_, String label_, String[] args_) {
		if (args_.length == 1) {
			if (args_[0].equals("reload")) {
				if (sender_ instanceof Player) {
					Player player = (Player)sender_;
					if (!plugin.worldPermissions(player).has(player, "antigrief.command.reload")) {
						String msg = (String)this.plugin.config.get("config.message.command");
						if (!msg.equals("")) player.sendMessage(msg);
					} else {
						plugin.buildConfig();
						player.sendMessage("AntiGrief reloaded!");
					}
				} else {
					plugin.buildConfig();
					plugin.log.info("AntiGrief reloaded!");
				}
				return true;
			}
		} else {
			if (sender_ instanceof Player) {
				Player player = (Player)sender_;
				if (plugin.worldPermissions(player).has(player, "antigrief.zone.create")) {
					if (args_[0].equals("zone")) {
						if (args_[1].equals("create")) {
							if (args_.length < 3) {
								player.sendMessage("/ag zone create <name> <parent>");
								return true;
							}
							String zone = args_[2];
							String parent = (args_.length > 3 ? args_[3] : null);
							
							if (plugin.zoneProtection.isBuilding(player.getName()))
								player.sendMessage("You are already building a zone!");
							else {
								plugin.zoneProtection.createZone(player, zone, parent);
								player.sendMessage("Right click to place points!");
							}
							return true;
						}
						if (args_[1].equals("finish"))  {
							if (plugin.zoneProtection.isBuilding(player.getName())) {
								plugin.zoneProtection.finishZone(player);
								player.sendMessage("Zone created!");
							} else 
								player.sendMessage("You are not creating a zone!");
							return true;
						}
						if (args_[1].equals("cancel")) {
							if (plugin.zoneProtection.isBuilding(player.getName())) {
								plugin.zoneProtection.cancelZone(player.getName());
							} else player.sendMessage("You are not creating a zone!");
							return true;
						}
					}
				} else {
					String msg = (String)this.plugin.config.get("config.message.command");
					if (!msg.equals("")) player.sendMessage(msg);
					return true;
				}
			}
		}
		return false;
	}
}
