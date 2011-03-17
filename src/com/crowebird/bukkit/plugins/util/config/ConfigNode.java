package com.crowebird.bukkit.plugins.util.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class ConfigNode {

	protected HashMap<String, ConfigNode> children;
	protected Object value;
	protected String key;
	
	public ConfigNode(String key_) {
		key = key_;
		children = new HashMap<String, ConfigNode>();
		value = null;
	}
	
	public void addValue(String path_, Object value_) {
		if (path_.equals("")) {
			value = value_;
			return;
		}
		
		int index = path_.indexOf(".");
		String key;
		if (index == -1) {
			key = path_;
			path_ = "";
		} else {
			key = path_.substring(0, index);
			path_ = path_.substring(index + 1);
		}
		
		if (key.equals("*")) return;
		
		ConfigNode node = children.get(key);
		if (node == null) {
			node = new ConfigNode(key);
			children.put(key, node);
		}
		
		node.addValue(path_, value_);
	}
	
	public Object getValue(String path_) {
		if (path_.equals("")) return value;
		
		int index = path_.indexOf(".");
		String key;
		if (index == -1) {
			key = path_;
			path_ = "";
		} else {
			key = path_.substring(0, index);
			path_ = path_.substring(index + 1);
		}
		
		if (!children.containsKey(key)) return null;
		
		ConfigNode node = children.get(key);
		if (node == null) return null;
		
		return node.getValue(path_);
	}
	
	public boolean hasValue(String path_, Object value_) {	
		Object value = getValue(path_);
		
		if (value instanceof ArrayList<?>) return ((ArrayList<?>) value).contains(value_);
		else return value.equals(value_);
	}
	
	public Set<String> getKeys() {
		return children.keySet();
	}
	
	public boolean hasKey(Object key_) {
		return children.containsKey(key_);
	}
	
	public String print(int level) {
		String output = "";
		Set<String> keys = children.keySet();
		if (keys.size() == 0 && value != null) {
			if (value instanceof ArrayList<?>) {
				ArrayList<?> list = (ArrayList<?>)value;
				for (Object line : list)
					output += (output.equals("") ? "" : ",\n" + getTab(level)) + (line instanceof String ? "'" : "") + line + (line instanceof String ? "'" : "");
				return "[" + output + "]";
			} else return (value instanceof String ? "'" : "") + value + (value instanceof String ? "'" : "");
		}
		if (level > 0) output += "\n";
		for (String key : keys)
			output += getTab(level) + key + ": " + children.get(key).print(level + 1) + "\n";
		return output;
	}
	
	protected String getTab(int level) {
		String tab = "";
		for (int x = 0; x < level; ++x) tab += "  ";
		
		return tab;
	}
}
