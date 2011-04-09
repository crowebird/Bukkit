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

package com.crowebird.bukkit.plugins.AntiGrief.Listeners;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import com.crowebird.bukkit.plugins.AntiGrief.AntiGrief;

public class AntiGriefPlayerListener extends PlayerListener {

	private final AntiGrief plugin;
	
	public AntiGriefPlayerListener(AntiGrief plugin_) {
		this.plugin = plugin_;
	}
	
	public void onPlayerInteract(PlayerInteractEvent event_) {
		Player player = event_.getPlayer();
		String name = player.getName();
		Block block = event_.getClickedBlock();
		
		if (event_.getAction() == Action.RIGHT_CLICK_AIR ||
				event_.getAction() == Action.RIGHT_CLICK_BLOCK) {
			if (event_.hasItem() && !event_.isBlockInHand()) {
				if (!plugin.access(player, "use", player.getLocation(), event_.getItem().getTypeId()))
					event_.setCancelled(true);
			}
		}
		
		if (event_.getAction() == Action.RIGHT_CLICK_BLOCK) {
			
			Location l = block.getLocation();
			BlockFace d = event_.getBlockFace();
			Material type = block.getType();
			
			int x = l.getBlockX();
			int y = l.getBlockY();
			int z = l.getBlockZ();
						
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
			
			if (plugin.zoneProtection.isBuilding(name) && player.getItemInHand().getTypeId() == (Integer)plugin.getValue("settings", "zones.tool")) {
				plugin.zoneProtection.addPoint(name, x, y, z);
				player.sendMessage("Point added!");
			}
		}
		
		try {
			Material t = block.getType();
			if (event_.getAction() == Action.RIGHT_CLICK_BLOCK ||
				(event_.getAction() == Action.LEFT_CLICK_BLOCK && t == Material.WOODEN_DOOR)) {
				
				if (t == Material.WOODEN_DOOR ||
						t == Material.CHEST ||
						t == Material.STONE_BUTTON) {
					if (!plugin.access(player, "interact", block.getLocation(), block.getTypeId()))
						event_.setCancelled(true);
				}
			}
		} catch (Exception ex) {
			//Usually fails if for some reason material cannot be determined
		}
	}
	
	public void onPlayerPickupItem(PlayerPickupItemEvent event_ ) {
		Item item = event_.getItem();
		Player player = event_.getPlayer();
		if (!plugin.access(player, "pickup", event_.getItem().getLocation(), item.getItemStack().getTypeId(), true))
			event_.setCancelled(true);
	}
}
