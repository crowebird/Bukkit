package com.crowebird.bukkit.AntiGrief;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerItemEvent;
import org.bukkit.event.player.PlayerListener;

public class AntiGriefPlayerListener extends PlayerListener {

	private AntiGrief plugin;
	
	public AntiGriefPlayerListener(AntiGrief plugin_) {
		this.plugin = plugin_;
	}
	
	public void onPlayerItem(PlayerItemEvent event_) {
		Player player = event_.getPlayer();
		if (!this.plugin.canBuild(player, "player.item")) {
			if (this.plugin.allowInteract(player.getWorld().getName(), event_.getItem().getType().getId())) return;
			event_.setCancelled(true);
		}
	}
}
