package com.crowebird.bukkit.AntiGrief;

import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.event.entity.EntityTargetEvent;

public class AntiGriefEntityListener extends EntityListener {
	
	private final AntiGrief plugin;
	
	public AntiGriefEntityListener(AntiGrief plugin_) {
		this.plugin = plugin_;
	}
	
	public void onEntityTarget(EntityTargetEvent event_) {
		if (event_.getEntity() instanceof Creeper) {
			Entity target = (Entity)event_.getTarget();
			if (target instanceof Player) {
				if (!this.plugin.access((Player)target, "entity.creeper", true))
					event_.setCancelled(true);
			}
		}
	}
	
	public void onEntityDamage(EntityDamageEvent event_) {
		Entity entity = event_.getEntity();
		if (event_ instanceof EntityDamageByEntityEvent) {
			EntityDamageByEntityEvent edbe = (EntityDamageByEntityEvent) event_;
			Entity damager = edbe.getDamager();
			if (damager instanceof Player) {
				if (!this.plugin.access((Player)damager, "player.damage.cause"))
					event_.setCancelled(true);
			}
		}
		if (entity instanceof Player) {
			if (!this.plugin.access((Player)entity, "player.damage.take." +  event_.getCause().toString().toLowerCase()))
				event_.setCancelled(true);
		}
	}
}
