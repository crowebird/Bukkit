package com.crowebird.bukkit.plugins.util.config;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import org.bukkit.util.config.Configuration;

import com.crowebird.bukkit.plugins.util.Config;
import com.crowebird.bukkit.plugins.util.ConfigNode;

public class Config {
	
	private String path;
	private String filename;
	private HashMap<String, ConfigNode> roots;
	private ConfigTemplate template;
	
	public Config(String path_, String filename_, ConfigTemplate template_) {
		path = path_;
		filename = filename_;
		template = template_;
	}
	
	public void load() throws IOException {
		String file = path + File.separator + file + ".yml";
		
		File yml = new File(file);
		if (!yml.exists()) throw new IOException("File does not exist!");
   
		ConfigNode a = new ConfigNode("a");
		a.add("bbb");
		
		Set<String> keys = template.keySet();
		
		
		Config.Type hm = new Config.Type();
		TreeSet<String> dkeys = new TreeSet<String>(default_.keySet());
		Configuration config = new Configuration(yml);
		config.load();
	
		
		for(String gkey : dkeys) {
			String pkey[] = gkey.split("\\.");
			Vector<String> ukey = new Vector<String>();
			
			for (int x = 1; x < pkey.length; ++x) {
				if (!pkey[x].equals("*")) continue;
				String suffix = "";
				for (int i = x + 1; i < pkey.length; ++i) suffix += "." + pkey[i];
				
				String path = "";
				for (int i = 0; i < x; ++i) path += (path.equals("") ? "" : ".") + pkey[i];
				List<String> pkeys = config.getKeys(path);
				if (pkeys != null) for(String sub : pkeys) ukey.add(path + "." + sub + suffix);
			}
			if (ukey.size() == 0) ukey.add(gkey);
			
			Object expected = default_.get(gkey);
			for (String key : ukey) {
				Object value = config.getProperty(key);
				
				boolean use_default = false;
				if (value == null) use_default = true;
				else {
					if (value instanceof ArrayList<?> && expected instanceof Config.AL<?>) {
						ArrayList<?> provided_value = (ArrayList<?>)value;
						if (provided_value.size() != 0) {
							if (!provided_value.get(0).getClass().equals(((Config.AL<?>)expected).getParameterized())) use_default = true;
						}
					}
				}
				if (use_default) {
					if (!ignore_) {
						Config.log.warning(plugin_ + " - " + (value == null ? "Missing" : "Unexpected value for") + " " + key + " [" + file + "], using default.");
						value = expected;
					} else {
						if (value != null) Config.log.warning(plugin_ + " - " + "Unexpected value for " + key + " [" + file + "], ignoring.");
						continue;
					}
				}
				hm.put((prefix_ ? file_ + "." : "") + key, value);
			}
		}

		return hm;
	}
	
	public Object getValue(String path_) {
		return null;
	}
	
	public Set<String> getKeys() {
		return roots.keySet();
	}
}
