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

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPlaceEvent;

import com.crowebird.bukkit.plugins.AntiGrief.AntiGrief;

public class AntiGriefBlockListener extends BlockListener {

	private final AntiGrief plugin;
	
	public AntiGriefBlockListener(AntiGrief plugin_) {
		plugin = plugin_;
	}
	
	public void onBlockDamage(BlockDamageEvent event_) {
		Player player = event_.getPlayer();
		Block block = event_.getBlock();
		
		if (block.getType() == Material.WOODEN_DOOR) {
			if (!this.plugin.access(player, "interact", block.getLocation(), block.getTypeId()))
				event_.setCancelled(true);
		}
	}
	
	public void onBlockPlace(BlockPlaceEvent event_) {
		Player player = event_.getPlayer();
		Block block = event_.getBlock();
		if (!this.plugin.access(player, "place", block.getLocation(), block.getTypeId()))
			event_.setCancelled(true);
	}

	public void onBlockBreak(BlockBreakEvent event_) {
		Player player = event_.getPlayer();
		Block block = event_.getBlock();
		if (!this.plugin.access(player, "break", block.getLocation(), block.getTypeId()))
			event_.setCancelled(true);
	}
	
	public void onBlockIgnite(BlockIgniteEvent event_) {
		Block block = event_.getBlock();
		if (!this.plugin.access(event_.getPlayer(), "ignite", block.getLocation()))
			event_.setCancelled(true);
	}
}
