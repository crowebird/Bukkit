package com.crowebird.bukkit.plugins.util.config.cast;

import java.util.ArrayList;

public class ConfigArrayList<T> extends ArrayList<T> {
	private static final long serialVersionUID = 6389658675302142895L;

	public ConfigArrayList(T ... values_) {
		for(T value : values_)
			add(value);
	}
	
	public Class<?> getParameterized() { return this.getClass(); }
}
