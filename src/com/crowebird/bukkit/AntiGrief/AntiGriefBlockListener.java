package com.crowebird.bukkit.AntiGrief;

import org.bukkit.block.BlockDamageLevel;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockInteractEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPlaceEvent;

public class AntiGriefBlockListener extends BlockListener {

	private AntiGrief plugin;
	
	public AntiGriefBlockListener(AntiGrief plugin_) {
		this.plugin = plugin_;
	}
	
	public void onBlockDamage(BlockDamageEvent event_) {
		Player player = event_.getPlayer();
		if (this.plugin.allowInteract(player.getWorld().getName(), event_.getBlock().getType().getId())
				&& event_.getDamageLevel() == BlockDamageLevel.STARTED)
			return;
		if (!this.plugin.canBuild(player, "block.damage")) {
			event_.setCancelled(true);
		}
	}
	
	public void onBlockPlace(BlockPlaceEvent event_) {
		if (!this.plugin.canBuild(event_.getPlayer(), "block.place"))
			event_.setCancelled(true);
	}
	
	public void onBlockInteract(BlockInteractEvent event_) {
		if (event_.isPlayer()) {
			Player player = (Player)event_.getEntity();
			if (this.plugin.allowInteract(player.getWorld().getName(), event_.getBlock().getType().getId())) return;
			if (!this.plugin.canBuild(player, "block.interact"))
				event_.setCancelled(true);
		}
	}
	
	public void onBlockBreak(BlockBreakEvent event_) {
		if (!this.plugin.canBuild(event_.getPlayer(), "block.damage"))
			event_.setCancelled(true);
	}
	
	public void onBlockIgnite(BlockIgniteEvent event_) {
		if (!this.plugin.canBuild(event_.getPlayer(), "block.ignite"))
			event_.setCancelled(true);
	}
}
