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
	
	private boolean needsReWrite;
	
	public Config(BukkitPlugin plugin_, String path_, String filename_, ConfigTemplate template_, String name_) {
		super("root");
		
		plugin = plugin_;
		path = path_;
		filename = filename_;
		template = template_;
		name = name_;
		
		needsReWrite = false;
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
		Vector<String> ukeys = new Vector<String>();
		
		boolean hasStar = false;
		for (int x = 1; x < path.length; ++x) {
			if (!path[x].equals("*")) continue;
			hasStar = true;
			String suffix = "";
			for (int i = x + 1; i < path.length; ++i) suffix += "." + path[i];
			
			String fpath = "";
			for (int i = 0; i < x; ++i) fpath += (fpath.equals("") ? "" : ".") + path[i];
			List<String> pkeys = config_.getKeys(fpath);
			if (pkeys != null) for(String sub : pkeys) ukeys.add(path + "." + sub + suffix);
		}
		if (ukeys.size() == 0) {
			if (!hasStar) ukeys.add(key_);
			else return;
		}
		
		Object expected = template.get(key_);
		boolean depricated = template.isKeyDepricated(key_);
		for (String ukey : ukeys) {
			Object value = config_.getProperty(ukey);
			
			if (depricated) {
				if (value == null) value = config_.getProperty(template.getNewKey(ukey));
				else needsReWrite = true;
			}
			
			boolean use_default = false;
			if (value == null) use_default = true;
			else {
				if (value instanceof ArrayList<?> && expected instanceof ConfigArrayList<?>) {
					ArrayList<?> provided_value = (ArrayList<?>)value;
					if (provided_value.size() != 0) {
						if (!provided_value.get(0).getClass().equals(((ConfigArrayList<?>)expected).getParameterized())) use_default = true;
					}
				}
			}
			
			if (use_default) {
				if (value != null) plugin.log(Level.WARNING, "Unexpected value for " + ukey + " [" + file_ + "], using default.");
				value = expected;
			}
			addValue(depricated ? template.getNewKey(ukey) : ukey, value);
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
		
		if (needsReWrite)
			write();
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
		
		needsReWrite = false;
	}
	
	public void create() {
		Set<String> keys = template.keySet();
		for (String key : keys) {
			if (key.indexOf("*") == -1)
				addValue(template.isKeyDepricated(key) ? template.getNewKey(key) : key, template.get(key));
		}
		write();
	}
}
