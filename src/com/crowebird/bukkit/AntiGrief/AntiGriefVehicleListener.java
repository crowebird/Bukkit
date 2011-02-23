package com.crowebird.bukkit.AntiGrief;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;
import org.bukkit.event.vehicle.VehicleListener;
import org.bukkit.event.vehicle.VehicleMoveEvent;

public class AntiGriefVehicleListener extends VehicleListener {
	
	private AntiGrief plugin;
	
	public AntiGriefVehicleListener(AntiGrief plugin_) {
		this.plugin = plugin_;
	}
	
	public void onVehicleMove(VehicleMoveEvent event_) {
		Entity passenger = event_.getVehicle().getPassenger();
		if (passenger instanceof Player) {
			if (!this.plugin.canBuild((Player)passenger, "vehicle.use")) {
				Vehicle vehicle = event_.getVehicle();
				/*
				Vector velocity = vehicle.getVelocity();
				double vx = velocity.getX();
				double vy = velocity.getY();
				double vz = velocity.getZ();
				if (vx != 0 || vy != 0 || vz != 0) {
					velocity.setX(0.0);
					velocity.setY(0.0);
					velocity.setZ(0.0);
				}
				*/
				vehicle.eject();
			}
		}
	}
	
	public void onVehicleEntityCollision(VehicleEntityCollisionEvent event_) {
		Entity collisionEntity = event_.getEntity();
		if (collisionEntity instanceof Player) {
			if (!this.plugin.canBuild((Player)collisionEntity, "vehicle.move")) {
				/*
				Vector velocity = event_.getVehicle().getVelocity();
				velocity.setX(0.0);
				velocity.setY(0.0);
				velocity.setZ(0.0);
				*/
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
