package com.crowebird.bukkit.AntiGrief;

import org.bukkit.Location;
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
		this.plugin = plugin_;
	}
	
	public void onBlockDamage(BlockDamageEvent event_) {
		Player player = event_.getPlayer();
		if (!this.plugin.access(player, "block.damage", event_.getBlock().getTypeId(), true)) {
			event_.setCancelled(true);
		}
	}
	
	public void onBlockPlace(BlockPlaceEvent event_) {
		Player player = event_.getPlayer();
		if (!this.plugin.access(player, "block.place", event_.getBlock().getTypeId()))
			event_.setCancelled(true);
	}
	
	public void onBlockInteract(BlockInteractEvent event_) {
		if (event_.isPlayer()) {
			Player player = (Player)event_.getEntity();
			if (!this.plugin.access(player, "block.interact", event_.getBlock().getTypeId()))
				event_.setCancelled(true);
		}
	}
	
	public void onBlockBreak(BlockBreakEvent event_) {
		Player player = event_.getPlayer();
		if (!this.plugin.access(player, "block.damage", event_.getBlock().getTypeId()))
			event_.setCancelled(true);
	}
	
	public void onBlockIgnite(BlockIgniteEvent event_) {
		if (!this.plugin.access(event_.getPlayer(), "block.ignite"))
			event_.setCancelled(true);
	}
	
	public void onBlockRightClick(BlockRightClickEvent event_) {
		Player player = event_.getPlayer();
		//int item = player.getItemInHand().getTypeId();
		/*
		if (!this.plugin.access(player, "block.place", item)) {
			//event_.getDirection().
			BlockFace face = event_.getDirection();
			Block block = event_.getBlock();
			block = block.getFace(face);
			Block rel = block.getRelative(face);
			System.out.println(face.toString());
			System.out.println(rel.getTypeId());
			System.out.println(block.getData());
			System.out.println(block.getRawData());
		}
		*/
		
		String name = event_.getPlayer().getName();
		Location l = event_.getBlock().getLocation();
		if (plugin.zoneProtection.isBuilding(name)) {
			plugin.zoneProtection.addPoint(name, l.getBlockX(), l.getBlockZ());
			player.sendMessage("Point added!");
		}
	}
}
