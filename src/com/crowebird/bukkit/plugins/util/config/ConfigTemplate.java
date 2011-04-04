package com.crowebird.bukkit.plugins.util.config;

import java.util.HashMap;

public class ConfigTemplate extends HashMap<String, Object> {
	private static final long serialVersionUID = 6233533928813360948L;

	private HashMap<String, String> depricated;
	
	public ConfigTemplate() {
		depricated = new HashMap<String, String>();
	}
	
	public Object put(String depricated_key_, Object object_, String new_key_) {
		depricated.put(depricated_key_, new_key_);
		return put(depricated_key_, object_);
	}
	
	public boolean isKeyDepricated(String key_) {
		return depricated.containsKey(key_);
	}
	
	public String getNewKey(String key_) {
		if (!isKeyDepricated(key_)) return key_;
		return depricated.get(key_);
	}
}
