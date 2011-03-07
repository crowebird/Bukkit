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

package com.crowebird.bukkit.AntiGrief.ZoneProtection;

import java.awt.Polygon;
import java.io.File;
import java.util.ArrayList;
import java.util.Set;

import org.bukkit.entity.Player;

import com.crowebird.bukkit.AntiGrief.AntiGrief;
import com.crowebird.bukkit.util.Config;

public class AntiGriefZoneProtectionZone {

	private final AntiGrief plugin;
	private Polygon poly;
	private ArrayList<String> groups, users;
	
	private String parent, name, creator, world;
	
	@SuppressWarnings("unused")
	private int baseY, verticalDown, verticalUp, points;

	private boolean finalized;
	
	private Config.Type config;
	
	@SuppressWarnings("unchecked")
	public AntiGriefZoneProtectionZone(AntiGrief plugin_, String name_, Config.Type config_, String world_) {
		plugin = plugin_;
		
		init(name_, (String)config_.get("creator"), (String)config_.get("parent"), world_);
		
		if (config_.containsKey("points")) {
			ArrayList<String> points = (ArrayList<String>) config_.get("points");
			for (String point : points) {
				String cord[] = point.split("\\,");
				if (cord.length != 2) continue;
				poly.addPoint(Integer.parseInt(cord[0]), Integer.parseInt(cord[1]));
			}
		}

		Set<String> keys = config_.keySet();
		for(String key : keys) {
			String type[] = key.split("\\.");
			
			if (type.length >= 2) {
				if (type[0].equals("groups"))
					groups.add(type[1]);
				else if (type[1].equals("users"))
					users.add(type[1]);
			}
		}
		
		if (!users.contains(creator)) users.add(creator);
		
		config = config_;
		finalized = true;
	}
	public AntiGriefZoneProtectionZone(AntiGrief plugin_, String name_, String creator_, String parent_, String world_) {
		plugin = plugin_;
		
		init(name_, creator_, parent_, world_);
		config.put("parent", parent);
		config.put("creator", creator);
		
		users.add(creator_);
		
		finalized = false;
	}
	
	public int getPoints() {
		return points;
	}
	
	private void init(String name_, String creator_, String parent_, String world_) {
		name = name_;
		parent = parent_;
		creator = creator_;
		
		config = new Config.Type();
		
		poly = new Polygon();
		groups = new ArrayList<String>();
		users = new ArrayList<String>();
		
		world = world_;
		baseY = 0;
		verticalDown = 0;
		verticalUp = 0;
		finalized = false;
		
		points = 0;
	}
	
	public String getParent() {
		return parent;
	}
	
	public String getName() {
		return name;
	}
	
	public String getWorld() {
		return world;
	}
	
	public boolean inside(int x_, int y_, int z_) {
		if (!finalized) return false;
		return /*y_ <= baseY + verticalUp && y_ >= baseY - verticalDown &&*/ poly.contains(x_, z_);
	}
	
	@SuppressWarnings("unchecked")
	public void addPoint(int x_, int z_) {
		poly.addPoint(x_, z_);
		if (config.containsKey("points")) {
			ArrayList<String> points = (ArrayList<String>)config.get("points");
			points.add(x_ + "," + z_);
		} else {
			ArrayList<String> points = new ArrayList<String>();
			points.add(x_ + "," + z_);
			config.put("points", points);
		}
		++points;
	}
	
	public void finalize() {
		if (finalized) return;
		finalized = true;
		write();
	}
	
	public boolean access(Player player_, String node_, int item_, boolean suppress_) {
		String group = plugin.getGroup(player_);
		
		boolean access_player = users.contains(player_.getName());
		boolean access_group = group != null && groups.contains(group);
		
		boolean access =  access_player || access_group;
		if (!access) return false;
		
		String player_path = "users." + player_.getName();
		String group_path = "groups." + group;
		
		boolean prevent_player = Config.has(config, player_path + ".nodes.prevent", node_);
		boolean prevent_group = Config.has(config, group_path + ".nodes.prevent", node_);

		if ((prevent_player || prevent_group) && item_ != -1) {
			if (access_player) return allowItem(player_path, node_, item_);
			if (access_group) return allowItem(group_path, node_, item_);
		}
		
		if (access_player) return !prevent_player;
		if (access_group) return !prevent_group;
		
		return false;
	}
	
	private boolean allowItem(String prefix_, String node_, int item_) {
		String node = "";
		if (node_.equals("block.interact")) node = "allow.interact";
		else if (node_.equals("block.damage") || node_.equals("block.place")) node = "allow.block";
		else if (node_.equals("player.item.pickup") || node_.equals("player.item.use")) node = "allow.item";
		else return false;

		return Config.has(config, prefix_ + "." + node, item_);
	}
	
	public void addGroup() {
		
	}
	
	public void addUser() {
		
	}
	
	private void write() {
		System.out.println(plugin.getDataFolder().toString() + File.separator + "zones" + File.separator + world);
		Config.create(plugin.pdf.getName(), plugin.getDataFolder().toString() + File.separator + "zones" + File.separator + world, name, config);
	}
}
