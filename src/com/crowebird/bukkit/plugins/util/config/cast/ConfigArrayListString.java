package com.crowebird.bukkit.plugins.util.config.cast;

import com.crowebird.bukkit.plugins.util.config.cast.ConfigArrayList;

public class ConfigArrayListString extends ConfigArrayList<String> {
	private static final long serialVersionUID = -435347809081963354L;

	public ConfigArrayListString(String ... values_) {
		super(values_);
	}
	
	public Class<?> getParameterized() { return String.class; }
}
