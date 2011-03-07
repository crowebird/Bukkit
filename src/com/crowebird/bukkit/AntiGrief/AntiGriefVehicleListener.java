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
FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> OR
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

package com.crowebird.bukkit.AntiGrief;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;
import org.bukkit.event.vehicle.VehicleListener;
import org.bukkit.event.vehicle.VehicleMoveEvent;

public class AntiGriefVehicleListener extends VehicleListener {
	
	private final AntiGrief plugin;
	
	public AntiGriefVehicleListener(AntiGrief plugin_) {
		this.plugin = plugin_;
	}
	
	public void onVehicleEnter(VehicleEnterEvent event_) {
		Entity passenger = event_.getEntered();
		Vehicle vehicle = event_.getVehicle();
		if (passenger instanceof Player) {
			if (!this.plugin.access((Player)passenger, "vehicle.use", vehicle.getLocation()))
				event_.setCancelled(true);
		}
	}
	
	public void onVehicleMove(VehicleMoveEvent event_) {
		Entity passenger = event_.getVehicle().getPassenger();
		if (passenger instanceof Player) {
			Vehicle vehicle = event_.getVehicle();
			if (!this.plugin.access((Player)passenger, "vehicle.use", vehicle.getLocation(), true))
				event_.getVehicle().eject();
		}
	}
	
	public void onVehicleEntityCollision(VehicleEntityCollisionEvent event_) {
		Entity collisionEntity = event_.getEntity();
		if (collisionEntity instanceof Player) {
			Vehicle vehicle = event_.getVehicle();
			if (!this.plugin.access((Player)collisionEntity, "vehicle.move", vehicle.getLocation(), true)) {
				event_.setCancelled(true);
				event_.setCollisionCancelled(true);
			}
		}
	}
	
	public void onVehicleDamage(VehicleDamageEvent event_) {
		Entity attacker = event_.getAttacker();
		if (attacker instanceof Player) {
			Vehicle vehicle = event_.getVehicle();
			if (!this.plugin.access((Player)attacker, "block.damage", vehicle.getLocation())) 
				event_.setCancelled(true);
		}
	}
}
