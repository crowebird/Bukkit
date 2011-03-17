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

package com.crowebird.bukkit.plugins.AntiGrief.ZoneProtection;

import java.awt.Polygon;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.World;

import com.crowebird.bukkit.plugins.AntiGrief.AntiGrief;
import com.crowebird.bukkit.plugins.util.config.Config;

public class AntiGriefZoneProtectionZone {

	private final AntiGrief plugin;
	private Polygon poly;
	private ArrayList<String> groups, users, points;
	private HashMap<String, AntiGriefZoneProtectionVisulize> visulize;
	
	private String parent, name, creator, world;
	private String message;
	
	@SuppressWarnings("unused")
	private int baseY, verticalDown, verticalUp, numPoints;

	private boolean finalized, visulized;
	
	private Config config;
	
	private boolean compatibility;
	
	@SuppressWarnings("unchecked")
	public AntiGriefZoneProtectionZone(AntiGrief plugin_, String name_, Config config_, String world_) {
		plugin = plugin_;
		
		init(name_, (String)config_.getValue("creator"), (String)config_.getValue("parent"), world_);
		
		if (config_.hasKey("points")) {
			ArrayList<String> points = (ArrayList<String>) config_.getValue("points");
			for (String point : points) {
				String cord[] = point.split("\\,");
				//BACKWARD COMPATIBILITY for zones without y values
				if (cord.length == 2) {
					poly.addPoint(Integer.parseInt(cord[0]), Integer.parseInt(cord[1]));
					compatibility = false;
				}
				else if (cord.length == 3) poly.addPoint(Integer.parseInt(cord[0]), Integer.parseInt(cord[2]));
				else continue;
				
				int x = 0;
				int y = 0;
				int z = 0;
				x = Integer.parseInt(cord[0]);
				try {
					y = Integer.parseInt(cord[1]);
					z = Integer.parseInt(cord[2]);
				} catch (Exception ex) {
					z = Integer.parseInt(cord[1]);
					y = 0;
				}
				AntiGriefZoneProtectionVisulize v = new AntiGriefZoneProtectionVisulize(x, y, z);
				visulize.put(x + "," + y + "," + z, v);
			}
		}

		Set<String> keys = config_.getKeys();
		for(String key : keys) {
			String type[] = key.split("\\.");
			
			if (type.length >= 2) {
				if (type[0].equals("groups"))
					groups.add(type[1]);
				else if (type[1].equals("users"))
					users.add(type[1]);
			}
		}
		
		if (config_.hasKey("message")) message = (String) config_.getValue("message");
		
		if (!users.contains(creator)) users.add(creator);
		
		config = config_;
		finalized = true;
	}
	public AntiGriefZoneProtectionZone(AntiGrief plugin_, String name_, String creator_, String parent_, String world_) {
		plugin = plugin_;
		
		init(name_, creator_, parent_, world_);
		config.addValue("parent", parent);
		config.addValue("creator", creator);
		
		users.add(creator_);
	}
	
	public int getPoints() {
		return numPoints;
	}
	
	private void init(String name_, String creator_, String parent_, String world_) {
		name = name_;
		parent = parent_;
		creator = creator_;
		
		//config = new Config(plugin.getDataFolder().toString() + File.separator + "zones" + File.separator + world, plugin.getName(), name);
		
		poly = new Polygon();
		groups = new ArrayList<String>();
		users = new ArrayList<String>();
		points = new ArrayList<String>();
		visulize = new HashMap<String, AntiGriefZoneProtectionVisulize>();
		
		world = world_;
		baseY = 0;
		verticalDown = 0;
		verticalUp = 0;
		
		message = "";
		
		finalized = false;
		visulized = false;
		
		compatibility = true;
		
		numPoints = 0;
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
	
	public void hideVisulization() {
		World w = plugin.getServer().getWorld(world);
		
		Set<String> keys = visulize.keySet();
		for(String key : keys) {
			AntiGriefZoneProtectionVisulize v = visulize.get(key);
			if (v.getVisible()) {
				Block b = w.getBlockAt(v.getX(), v.getY(), v.getZ());
				int id = v.getTypeId();
				b.setTypeId(id);
				b.setType(v.getType());
				b.setData(v.getData());
				
				if (id == 64 || id == 71) {
				}
				
				v.setVisible(false);
			}
		}
		
		visulized = false;
	}
	
	public void showVisulization() {
		World w = plugin.getServer().getWorld(world);
		Set<String> keys = visulize.keySet();
		for(String key : keys) {
			AntiGriefZoneProtectionVisulize v = visulize.get(key);
			if (!v.getVisible()) {
				Block b = w.getBlockAt(v.getX(), v.getY(), v.getZ());
				v.setData(b.getData());
				v.setType(b.getType());
				v.setTypeId(b.getTypeId());
				b.setTypeId(35);
				b.setData(new Byte("14"));
				v.setVisible(true);
			}
		}
		
		visulized = true;
	}
	
	public boolean isCompatible() {
		return compatibility;
	}
	
	public boolean isVisulized() {
		return visulized;
	}
	
	public boolean inside(int x_, int y_, int z_) {
		if (!finalized) return false;
		return /*y_ <= baseY + verticalUp && y_ >= baseY - verticalDown &&*/ poly.contains(x_, z_);
	}
	
	@SuppressWarnings("unchecked")
	public void addPoint(int x_, int y_, int z_) {
		poly.addPoint(x_, z_);
		String key = x_ + "," + y_ + "," + z_;
		points.add(key);
		if (config.hasKey("points")) {
			ArrayList<String> points = (ArrayList<String>)config.getValue("points");
			points.add(key);
		} else {
			ArrayList<String> points = new ArrayList<String>();
			points.add(key);
			config.addValue("points", points);
		}
		++numPoints;
		
		AntiGriefZoneProtectionVisulize v = new AntiGriefZoneProtectionVisulize(x_, y_, z_);
		visulize.put(key, v);
		
		showVisulization();
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
		
		boolean prevent_player = config.hasValue(player_path + ".nodes.prevent", node_);
		boolean prevent_group = config.hasValue(group_path + ".nodes.prevent", node_);

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

		return config.hasValue(prefix_ + "." + node, item_);
	}
	
	public boolean addGroup(String group_) {
		return true;
	}
	
	public boolean addUser(String user_) {
		return true;
	}
	
	public boolean removeUser(String user_) {
		return true;
	}
	
	public boolean removeGroup(String group_) {
		return true;
	}
	
	private void write() {
		System.out.println(plugin.getDataFolder().toString() + File.separator + "zones" + File.separator + world);
		config.write();
	}
	
	public String getMessage() {
		return message;
	}
}
