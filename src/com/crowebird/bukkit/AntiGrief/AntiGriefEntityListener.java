package com.crowebird.bukkit.AntiGrief;

import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

public class AntiGriefEntityListener extends EntityListener {
	private AntiGrief plugin;
	
	public AntiGriefEntityListener(AntiGrief plugin_) {
		this.plugin = plugin_;
	}
	
	public void onEntityTarget(EntityTargetEvent event_) {
		if (event_.getEntity() instanceof Creeper) {
			Entity target = (Entity)event_.getTarget();
			if (target instanceof Player) {
				if (!this.plugin.canBuild((Player)target, "entity.creeper"))
					event_.setCancelled(true);
			}
		}
	}
	
	public void onEntityDamage(EntityDamageEvent event_) {
		Entity entity = event_.getEntity();
		DamageCause cause = event_.getCause();
		if (entity instanceof Player) {
			if (!this.plugin.canBuild((Player)entity, "entity.damage." + cause.toString().toLowerCase()))
				event_.setCancelled(true);
		}
	}
}
