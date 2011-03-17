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
	
	public void load(boolean ignore_warning_) throws IOException {
		String file = path + File.separator + filename + ".yml";
		
		File yml = new File(file);
		if (!yml.exists()) throw new IOException("File does not exist!");
		
		Set<String> keys = template.keySet();
		Configuration config = new Configuration(yml);
		config.load();
		
		for (String key : keys) {
			String[] path = key.split("\\.");
			Vector<String> ukeys = new Vector<String>();
			
			for (int x = 1; x < path.length; ++x) {
				if (!path[x].equals("*")) continue;
				String suffix = "";
				for (int i = x + 1; i < path.length; ++i) suffix += "." + path[i];
				
				String fpath = "";
				for (int i = 0; i < x; ++i) fpath += (fpath.equals("") ? "" : ".") + path[i];
				List<String> pkeys = config.getKeys(fpath);
				if (pkeys != null) for(String sub : pkeys) ukeys.add(path + "." + sub + suffix);
			}
			if (ukeys.size() == 0) ukeys.add(key);
			
			Object expected = template.get(key);
			for (String ukey : ukeys) {
				Object value = config.getProperty(key);
				
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
					if (!ignore_warning_) {
						plugin.log(Level.WARNING, (value == null ? "Missing" : "Unexpected value for") + " " + ukey + " [" + file + "], using default.");
						value = expected;
					} else {
						if (value != null) plugin.log(Level.WARNING, "Unexpected value for " + ukey + " [" + file + "], ignoring.");
						continue;
					}
				}
				addValue(ukey, value);
			}
 		}
	}
	public void load() throws IOException { load(false); }
	
	public void write() {
		String file = path + File.separator + filename + ".yml";
		try {
			File f = new File(file);
			boolean exists = false;
			if (!f.exists()) {
				new File(path).mkdir();
				f.createNewFile();
			} else exists = true;
			
			BufferedWriter out = new BufferedWriter(new FileWriter(file));
			out.write(print(0));	
			out.flush();
			out.close();

			if (!exists) plugin.log("Configuration file created [" + file + "]!");
		} catch (Exception ex) {
			plugin.log(Level.WARNING, "Unable to properly create config [" + file + "]!");
		}
	}
	
	public void create() {
		Set<String> keys = template.keySet();
		for (String key : keys)
			addValue(key, template.get(key));
		write();
	}
}
