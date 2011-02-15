package com.crowebird.bukkit.AntiGrief;

import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.event.entity.EntityTargetEvent;

public class AntiGriefEntityListener extends EntityListener {
	private AntiGrief plugin;
	
	public AntiGriefEntityListener(AntiGrief plugin_) {
		this.plugin = plugin_;
	}
	
	public void onEntityTarget(EntityTargetEvent event_) {
		if (event_.getEntity() instanceof Creeper) {
			Entity target = (Entity)event_.getTarget();
			if (target instanceof Player) {
				if (!this.plugin.canBuild((Player)target, "antigrief.entity.creeper"))
					event_.setCancelled(true);
			}
		}
	}
}
