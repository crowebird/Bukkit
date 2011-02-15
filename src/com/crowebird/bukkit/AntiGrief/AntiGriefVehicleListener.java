package com.crowebird.bukkit.AntiGrief;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;
import org.bukkit.event.vehicle.VehicleListener;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.util.Vector;

public class AntiGriefVehicleListener extends VehicleListener {
	
	private AntiGrief plugin;
	
	public AntiGriefVehicleListener(AntiGrief plugin_) {
		this.plugin = plugin_;
	}
	
	public void onVehicleEnter(VehicleEnterEvent event_) {
		Entity passenger = event_.getEntered();
		if (passenger instanceof Player) {
			
			if (!this.plugin.canBuild((Player)passenger, "antigrief.vehicle.ues")) {
				event_.getVehicle().eject();
				
				//extended removal - testing to replace onVehicleMove
				Player player = (Player)passenger;
				if (player.isInsideVehicle())
					player.leaveVehicle();
			}
		}
	}
	
	//This is just to ensure that if somehow a player who can't build got in a vehicle
	//that they get ejected.  Also on vehicle enter does not seem to fire right,
	//so this takes care of the eject
	public void onVehicleMove(VehicleMoveEvent event_) {
		Entity passenger = event_.getVehicle().getPassenger();
		if (passenger instanceof Player) {
			if (!this.plugin.canBuild((Player)passenger, "antigrief.vehicle.use")) {
				Vehicle vehicle = event_.getVehicle();
				Vector velocity = vehicle.getVelocity();
				double vx = velocity.getX();
				double vy = velocity.getY();
				double vz = velocity.getZ();
				if (vx != 0 || vy != 0 || vz != 0) {
					velocity.setX(0.0);
					velocity.setY(0.0);
					velocity.setZ(0.0);
					vehicle.eject();
				}
			}
		}
	}
	
	public void onVehicleEntityCollision(VehicleEntityCollisionEvent event_) {
		Entity collisionEntity = event_.getEntity();
		if (collisionEntity instanceof Player) {
			if (!this.plugin.canBuild((Player)collisionEntity, "antigrief.vehicle.move")) {
				Vector velocity = event_.getVehicle().getVelocity();
				velocity.setX(0.0);
				velocity.setY(0.0);
				velocity.setZ(0.0);
				event_.setCancelled(true);
				event_.setCollisionCancelled(true);
			}
		}
	}
	
	public void onVehicleDamage(VehicleDamageEvent event_) {
		Entity attacker = event_.getAttacker();
		if (attacker instanceof Player) {
			if (!this.plugin.canBuild((Player)attacker, "antigrief.block.damage")) 
				event_.setCancelled(true);
		}
	}
}
