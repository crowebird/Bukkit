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

import org.bukkit.Location;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Cow;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Flying;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.Giant;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Pig;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Spider;
import org.bukkit.entity.Squid;
import org.bukkit.entity.WaterMob;
import org.bukkit.entity.Wolf;
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
			Entity e = event_.getEntity();
			Location l = target.getLocation();
			Player t = (Player)target;
			
			//Follow the tabbing for inheritance
			if ((e instanceof LivingEntity && !this.plugin.access(t, "target_livingentity", l, true)) ||
					(e instanceof Creature && !this.plugin.access(t, "target_creature", l, true)) ||
						(e instanceof Animals && !this.plugin.access(t, "target_animals", l, true)) ||
							(e instanceof Chicken && !this.plugin.access(t, "target_chicken", l, true)) ||
							(e instanceof Cow && !this.plugin.access(t, "target_cow", l, true)) ||
							(e instanceof Pig && !this.plugin.access(t, "target_pig", l, true)) ||
							(e instanceof Sheep && !this.plugin.access(t, "target_sheep", l, true)) ||
							(e instanceof Wolf && !this.plugin.access(t, "target_wolf", l, true)) ||
						(e instanceof Monster && !this.plugin.access(t, "target_monster", l, true)) ||
							(e instanceof Creeper && !this.plugin.access(t, "target_creeper", l, true)) ||
							(e instanceof Giant && !this.plugin.access(t, "target_giant", l, true)) ||
							(e instanceof Skeleton && !this.plugin.access(t, "target_skeleton", l, true)) ||
							(e instanceof Spider && !this.plugin.access(t, "target_spider", l, true)) ||
							(e instanceof Zombie && !this.plugin.access(t, "target_zombit", l, true)) ||
								(e instanceof PigZombie && !this.plugin.access(t, "target_pigzombie", l, true)) ||
						(e instanceof WaterMob && !this.plugin.access(t, "target_watermob", l, true)) ||
							(e instanceof Squid && !this.plugin.access(t, "target_squid", l, true)) ||
					(e instanceof Flying && !this.plugin.access(t, "target_flying", l, true)) ||
						(e instanceof Ghast && !this.plugin.access(t, "target_ghast", l, true)) ||
					(e instanceof Slime && !this.plugin.access(t, "target_slime", l, true)))
				event_.setCancelled(true);
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
