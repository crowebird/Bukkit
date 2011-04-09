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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;

import org.bukkit.util.config.Configuration;

import com.crowebird.bukkit.plugins.BukkitPlugin;
import com.crowebird.bukkit.plugins.util.config.ConfigNode;
import com.crowebird.bukkit.plugins.util.config.cast.ConfigArrayList;

public class Config extends ConfigNode {
	
	BukkitPlugin plugin;
	
	private String path;
	private String filename;
	private ConfigTemplate template;
	
	private String name;
	
	public Config(BukkitPlugin plugin_, String path_, String filename_, ConfigTemplate template_, String name_) {
		super("root");
		
		plugin = plugin_;
		path = path_;
		filename = filename_;
		template = template_;
		name = name_;
	}
	public Config(BukkitPlugin plugin_, String path_, String filename_, ConfigTemplate template_) { this(plugin_, path_, filename_, template_, filename_); }
	
	public void setName(String name_) {
		name = name_;
	}
	
	public String getName() {
		return name;
	}
	
	private void determineValue(String key_, Configuration config_, String file_, String value_) {
		String[] path = key_.split("\\.");
		
		Vector<String> use_keys = new Vector<String>();
		
		boolean hasStar = false;
		String fix = "";
		for(int x = 0; x < path.length; ++x) {
			if (!path[x].equals("*")) {
				fix += (fix.equals("") ? "" : ".") + path[x];
				continue;
			}
			
			hasStar = true;
			
			if (use_keys.size() == 0) {
				List<String> keys = config_.getKeys(fix);
				if (keys == null)
					break;
				for(String key : keys)
					use_keys.add(fix + (fix.equals("") ? "" : ".") + key);
			} else {
				Vector<String> new_use_keys = new Vector<String>();
				for(int y = 0; y < use_keys.size(); ++y) {
					List<String> keys = config_.getKeys(use_keys.get(y) + "." + fix);
					if (keys == null)
						break;
					for(String key : keys)
						new_use_keys.add(use_keys.get(y) + "." + fix + (fix.equals("") ? "" : ".") + key);
				}
				use_keys = new Vector<String>(new_use_keys);
			}
			
			fix = "";
		}
		if (use_keys.size() == 0 && hasStar)
			return;
		else if (!fix.equals("") && use_keys.size() == 0)
			use_keys.add(fix);
		else {
			Vector<String> new_use_keys = new Vector<String>();
			for(int x = 0; x < use_keys.size(); ++x)
				new_use_keys.add(use_keys.get(x) + "." + fix);
			use_keys = new Vector<String>(new_use_keys);
		}
		
		Object expected = template.get(key_);
		for (String key : use_keys) {
			if (hasKey(key)) continue;
			
			Object value = config_.getProperty(key);
			
			boolean use_default = false;
			if (value == null) {
				if (hasStar) continue;
				use_default = true;
			} else {
				if (value instanceof ArrayList<?> && expected instanceof ConfigArrayList<?>) {
					ArrayList<?> provided_value = (ArrayList<?>)value;
					if (provided_value.size() != 0) {
						if (!provided_value.get(0).getClass().equals(((ConfigArrayList<?>)expected).getParameterized())) use_default = true;
					}
				}
			}
			
			if (use_default) {
				//if (value != null) plugin.log(Level.WARNING, "Unexpected value for " + k + " [" + file_ + "], using default.");
				value = expected;
			}
			
			addValue(key, value);
		}
	}
	private void determineValue(String key_, Configuration config_, String file_) { determineValue(key_, config_, file_, null); }
	
	public void load() throws IOException {
		String file = path + File.separator + filename + ".yml";
		
		File yml = new File(file);
		if (!yml.exists()) throw new IOException("File does not exist!");
		
		Set<String> keys = template.keySet();
		Configuration config = new Configuration(yml);
		config.load();
		
		for (String key : keys)
			determineValue(key, config, file);
	}
	
	public void write() {
		String file = path + File.separator + filename + ".yml";
		try {
			File f = new File(file);
			boolean exists = false;
			if (!f.exists()) {
				new File(path).mkdirs();
				f.createNewFile();
			} else exists = true;
			
			BufferedWriter out = new BufferedWriter(new FileWriter(file));
			out.write(print(0));	
			out.flush();
			out.close();

			if (!exists) plugin.log("Configuration file created [" + file + "]!");
		} catch (Exception ex) {
			plugin.log(Level.WARNING, "Unable to write config [" + file + "]!");
		}
	}
	
	public void create() {
		Set<String> keys = template.keySet();
		for (String key : keys) {
			if (key.indexOf("*") == -1)
				addValue(key, template.get(key));
		}
		write();
	}
}
