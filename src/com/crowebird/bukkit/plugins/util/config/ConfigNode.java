/*
Copyright 2011 Michael Crowe. All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are
permitted provided that the following conditions are met:

   1. Redistributions of source code must retain the above copyright notice, this list of
      conditions and the following disclaimer.

   2. Redistributions in binary form must reproduce the above copyright notice, this list
      of conditions and the following disclaimer in the documentation and/or other materials
      provided with the distribution.

THIS SOFTWARE IS PROVIDED BY Michael Crowe ``AS IS'' AND ANY EXPRESS OR IMPLIED
WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL Michael Crowe OR
CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

The views and conclusions contained in the software and documentation are those of the
authors and should not be interpreted as representing official policies, either expressed
or implied, of Michael Crowe.
*/

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
		value = new ConfigNull();
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
	
	public void removeValue(String path_) {
		if (path_.equals("")) return;
		
		int index = path_.indexOf(".");
		String key;
		if (index == -1) {
			key = path_;
			path_ = "";
		} else {
			key = path_.substring(0, index);
			path_ = path_.substring(index + 1);
		}
		
		ConfigNode node = children.get(key);
		if (node == null) return;
		
		node.removeValue(path_);
		if (node.numChildren() == 0) children.remove(key);		
	}
	
	public void removeKey(String path_) {
		if (path_.equals("")) return;
		
		int index = path_.indexOf(".");
		String key;
		if (index == -1) {
			children.remove(path_);
			return;
		}
		
		key = path_.substring(0, index);
		path_ = path_.substring(index + 1);
		
		ConfigNode node = children.get(key);
		if (node == null) return;
		
		node.removeKey(path_);
		if (node.numChildren() == 0) children.remove(key);		
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
		return children.get(key).getValue(path_);
	}
	
	public boolean hasValue(String path_, Object value_) {	
		Object value = getValue(path_);
		if (value == null) return false;
		
		if (value instanceof ArrayList<?>) return ((ArrayList<?>) value).contains(value_);
		else return value.equals(value_);
	}
	
	public Set<String> getKeys() {
		return children.keySet();
	}
	public Set<String> getKeys(String path_) {
		if (path_.equals("")) return children.keySet();
		
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
		return children.get(key).getKeys(path_);
	}
	
	public int numChildren() {
		return children.size();
	}
	
	public boolean hasKey(String key_) {
		return children.containsKey(key_);
	}
	
	public boolean hasPath(String path_) {
		if (path_.equals("")) return true;
		
		int index = path_.indexOf(".");
		String key;
		if (index == -1) {
			key = path_;
			path_ = "";
		} else {
			key = path_.substring(0, index);
			path_ = path_.substring(index + 1);
		}
		
		if (!children.containsKey(key)) return false;
		return children.get(key).hasPath(path_);
	}
	
	public String print(int level) {
		String output = "";
		Set<String> keys = children.keySet();
		if (keys.size() == 0) {
			if (value == null) return "null";
			else {
				if (value instanceof ArrayList<?>) {
					ArrayList<?> list = (ArrayList<?>)value;
					for (Object line : list)
						output += (output.equals("") ? "" : ",\n" + getTab(level)) + (line instanceof String ? "'" : "") + line + (line instanceof String ? "'" : "");
					return "[" + output + "]";
				} else return (value instanceof String ? "'" : "") + value + (value instanceof String ? "'" : "");
			}
		}
		for (String key : keys)
			output += (output.equals("") ? "" : "\n") + getTab(level) + key + ": " + children.get(key).print(level + 1);
		return (level > 0 ? "\n" : "") + output;
	}
	
	protected String getTab(int level) {
		String tab = "";
		for (int x = 0; x < level; ++x) tab += "  ";
		
		return tab;
	}
}
