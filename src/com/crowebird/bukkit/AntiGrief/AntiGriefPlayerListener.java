package com.crowebird.bukkit.AntiGrief;

import org.bukkit.event.player.PlayerItemEvent;
import org.bukkit.event.player.PlayerListener;

public class AntiGriefPlayerListener extends PlayerListener {

	private AntiGrief plugin;
	
	public AntiGriefPlayerListener(AntiGrief plugin_) {
		this.plugin = plugin_;
	}
	
	public void onPlayerItem(PlayerItemEvent event_) {
		if (!this.plugin.canBuild(event_.getPlayer(), "player.item"))
			event_.setCancelled(true);
	}
}
