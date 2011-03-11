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

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockDamageLevel;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockInteractEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockRightClickEvent;

public class AntiGriefBlockListener extends BlockListener {

	private final AntiGrief plugin;
	
	public AntiGriefBlockListener(AntiGrief plugin_) {
		plugin = plugin_;
	}
	
	public void onBlockDamage(BlockDamageEvent event_) {
		Player player = event_.getPlayer();
		Block block = event_.getBlock();
		
		if (this.plugin.access(player, "block.interact", block.getLocation(), block.getTypeId(), true) &&
			event_.getDamageLevel().equals(BlockDamageLevel.STARTED)) return;
		
		if (!this.plugin.access(player, "block.damage", block.getLocation(), block.getTypeId()))
			event_.setCancelled(true);
	}
	
	public void onBlockPlace(BlockPlaceEvent event_) {
		Player player = event_.getPlayer();
		Block block = event_.getBlock();
		if (!this.plugin.access(player, "block.place", block.getLocation(), block.getTypeId()))
			event_.setCancelled(true);
	}
	
	public void onBlockInteract(BlockInteractEvent event_) {
		if (event_.isPlayer()) {
			Player player = (Player)event_.getEntity();
			Block block = event_.getBlock();
			if (!this.plugin.access(player, "block.interact", block.getLocation(), block.getTypeId()))
				event_.setCancelled(true);
		}
	}
	
	public void onBlockBreak(BlockBreakEvent event_) {
		Player player = event_.getPlayer();
		Block block = event_.getBlock();
		if (!this.plugin.access(player, "block.damage", block.getLocation(), block.getTypeId()))
			event_.setCancelled(true);
	}
	
	public void onBlockIgnite(BlockIgniteEvent event_) {
		Block block = event_.getBlock();
		if (!this.plugin.access(event_.getPlayer(), "block.ignite", block.getLocation()))
			event_.setCancelled(true);
	}
	
	public void onBlockRightClick(BlockRightClickEvent event_) {
		Player player = event_.getPlayer();
		String name = event_.getPlayer().getName();
		
		Location l = event_.getBlock().getLocation();
		BlockFace d = event_.getDirection();
		int x = l.getBlockX();
		int y = l.getBlockY();
		int z = l.getBlockZ();
		
		Material type = event_.getBlock().getType();
		if (type == Material.TORCH ||
				type == Material.REDSTONE_TORCH_OFF ||
				type == Material.REDSTONE_TORCH_ON) { }
		else {
			if (d == BlockFace.DOWN) --y;
			if (d == BlockFace.UP) ++y;
			if (d == BlockFace.NORTH) --x;
			if (d == BlockFace.SOUTH) ++x;
			if (d == BlockFace.WEST)++z;
			if (d == BlockFace.EAST) --z;
		}
		
		if (plugin.zoneProtection.isBuilding(name) && player.getItemInHand().getTypeId() == (Integer)plugin.getValue("settings", "config.zones.tool")) {
			plugin.zoneProtection.addPoint(name, x, y, z);
			player.sendMessage("Point added!");
		}
	}
}
