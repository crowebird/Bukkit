package com.crowebird.bukkit.AntiGrief;

import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerItemEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;

public class AntiGriefPlayerListener extends PlayerListener {

	private final AntiGrief plugin;
	
	public AntiGriefPlayerListener(AntiGrief plugin_) {
		this.plugin = plugin_;
	}
	
	public void onPlayerItem(PlayerItemEvent event_) {
		ItemStack item = event_.getItem();
		Player player = event_.getPlayer();
		if (!this.plugin.access(player, "player.item.use", player.getLocation(), item.getTypeId()))
			event_.setCancelled(true);
	}
	
	public void onPlayerPickupItem(PlayerPickupItemEvent event_ ) {
		Item item = event_.getItem();
		Player player = event_.getPlayer();
		if (!this.plugin.access(player, "player.item.pickup", event_.getItem().getLocation(), item.getItemStack().getTypeId(), true))
			event_.setCancelled(true);
	}
}
