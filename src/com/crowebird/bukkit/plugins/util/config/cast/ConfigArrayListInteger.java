package com.crowebird.bukkit.plugins.util.config.cast;

import com.crowebird.bukkit.plugins.util.config.cast.ConfigArrayList;

public class ConfigArrayListInteger extends ConfigArrayList<Integer> {
	private static final long serialVersionUID = -8522898352991704135L;

	public ConfigArrayListInteger(Integer ... values_) {
		super(values_);
	}
	
	public Class<?> getParameterized() { return Integer.class; }
}
