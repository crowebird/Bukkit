package com.crowebird.bukkit.plugins.util.config;

import java.util.HashMap;

public class ConfigNode {

	private HashMap<String, ConfigNode> children;
	private Object value;
	private String key;
	
	public ConfigNode(String key_) {
		key = key_;
		children = new HashMap<String, ConfigNode>();
		value = null;
	}
	
	public void add(String root_, String ... path_) throws ConfigException {
		if (path_.length == 0)
		throw new ConfigException();
	}
}
