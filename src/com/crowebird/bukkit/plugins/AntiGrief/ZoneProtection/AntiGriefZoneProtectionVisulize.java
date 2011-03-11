package com.crowebird.bukkit.plugins.AntiGrief.ZoneProtection;

import org.bukkit.Material;

public class AntiGriefZoneProtectionVisulize {

	private boolean visible;
	private int x, y ,z, id;
	private byte data;
	private Material material;
	
	public AntiGriefZoneProtectionVisulize(int x_, int y_, int z_) {
		visible = false;
		x = x_;
		y = y_;
		z = z_;
		
		material = null;
		id = -1;
		data = 0;
	}
	
	public void setVisible(boolean visible_) {
		visible = visible_;
	}
	
	public boolean getVisible() {
		return visible;
	}
	
	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
	
	public int getZ() {
		return z;
	}
	
	public void setTypeId(int id_) {
		id = id_;
	}
	
	public void setType(Material material_) {
		material = material_;
	}
	
	public void setData(byte data_) {
		data = data_;
	}
	
	public int getTypeId() {
		return id;
	}
	
	public Material getType() {
		return material;
	}
	
	public byte getData() {
		return data;
	}
	
}
