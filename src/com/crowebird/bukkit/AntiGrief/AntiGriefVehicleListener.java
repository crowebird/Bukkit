package com.crowebird.bukkit.AntiGrief;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;
import org.bukkit.event.vehicle.VehicleListener;
import org.bukkit.event.vehicle.VehicleMoveEvent;

public class AntiGriefVehicleListener extends VehicleListener {
	
	private AntiGrief plugin;
	
	public AntiGriefVehicleListener(AntiGrief plugin_) {
		this.plugin = plugin_;
	}
	
	public void onVehicleEnter(VehicleEnterEvent event_) {
		Entity passenger = event_.getEntered();
		if (passenger instanceof Player) {
			if (!this.plugin.canBuild((Player)passenger, "vehicle.use"))
				event_.setCancelled(true);
		}
	}
	
	public void onVehicleMove(VehicleMoveEvent event_) {
		Entity passenger = event_.getVehicle().getPassenger();
		if (passenger instanceof Player) {
			if (!this.plugin.canBuild((Player)passenger, "vehicle.use"))
				event_.getVehicle().eject();
		}
	}
	
	public void onVehicleEntityCollision(VehicleEntityCollisionEvent event_) {
		Entity collisionEntity = event_.getEntity();
		if (collisionEntity instanceof Player) {
			if (!this.plugin.canBuild((Player)collisionEntity, "vehicle.move")) {
				event_.setCancelled(true);
				event_.setCollisionCancelled(true);
			}
		}
	}
	
	public void onVehicleDamage(VehicleDamageEvent event_) {
		Entity attacker = event_.getAttacker();
		if (attacker instanceof Player) {
			if (!this.plugin.canBuild((Player)attacker, "block.damage")) 
				event_.setCancelled(true);
		}
	}
}
