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

import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Spider;
import org.bukkit.entity.Zombie;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.event.entity.EntityTargetEvent;

import com.crowebird.bukkit.plugins.AntiGrief.AntiGrief;

public class AntiGriefEntityListener extends EntityListener {
	
	private final AntiGrief plugin;
	
	public AntiGriefEntityListener(AntiGrief plugin_) {
		this.plugin = plugin_;
	}
	
	public void onEntityTarget(EntityTargetEvent event_) {
		Entity target = (Entity)event_.getTarget();
		if (target instanceof Player){
			if (event_.getEntity() instanceof Creeper) {
				if (!this.plugin.access((Player)target, "creeper", target.getLocation(), true))
					event_.setCancelled(true);
			} else if (event_.getEntity() instanceof Zombie) {
				if (!this.plugin.access((Player)target, "zombie", target.getLocation(), true))
					event_.setCancelled(true);
			} else if (event_.getEntity() instanceof Ghast) {
				if (!this.plugin.access((Player)target, "ghast", target.getLocation(), true))
					event_.setCancelled(true);
			} else if (event_.getEntity() instanceof Monster) {
				if (!this.plugin.access((Player)target, "monster", target.getLocation(), true))
					event_.setCancelled(true);
			} else if (event_.getEntity() instanceof Skeleton) {
				if (!this.plugin.access((Player)target, "skeleton", target.getLocation(), true))
					event_.setCancelled(true);
			} else if (event_.getEntity() instanceof Spider) {
				if (!this.plugin.access((Player)target, "spider", target.getLocation(), true))
					event_.setCancelled(true);
			} else if (event_.getEntity() instanceof Slime) {
				if (!this.plugin.access((Player)target, "slime", target.getLocation(), true))
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
				if (!this.plugin.access((Player)damager, "hit", damager.getLocation()))
					event_.setCancelled(true);
			}
		}
		if (entity instanceof Player) {
			if (!this.plugin.access((Player)entity, event_.getCause().toString().toLowerCase(), entity.getLocation()))
				event_.setCancelled(true);
		}
	}
}
