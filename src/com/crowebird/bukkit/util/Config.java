/**
 * Configuration reader for yml using Bukkit Configuration class
 * Created by Michael Crowe (crowebird)
 * 
 * Feel free to learn from but give credit! :D
 */

package com.crowebird.bukkit.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;
import java.util.TreeSet;
import java.util.logging.Logger;

import org.bukkit.util.config.Configuration;

public class Config {

	protected static final Logger log = Logger.getLogger("Minecraft");
	
	private static class AL<T> extends ArrayList<T> {
		private static final long serialVersionUID = 2L;
		
		public Class<?> getParameterized() {
			return this.getClass();
		}
	}
	
	public static class ALInteger extends Config.AL<Integer> {
		private static final long serialVersionUID = 3L;
		
		public Class<?> getParameterized() {
			return Integer.class;
		}
	}
	
	public static class ALString extends Config.AL<String> {
		private static final long serialVersionUID = 4L;
		
		public Class<?> getParameterized() {
			return String.class;
		}
	}
	
	public static class Type extends HashMap<String, Object> {
		private static final long serialVersionUID = 1L;
		
		public boolean has(String key_, Object value_) {
			if (!this.containsKey(key_)) return false;
			Object value = this.get(key_);

			if (value instanceof ArrayList<?>) return ((ArrayList<?>) value).contains(value_);
			else return value.equals(value_);
		}
	};
	
	public static Config.Type read(String plugin_, String path_, String file_, Config.Type default_) throws IOException {
		File yml = new File(path_ + File.separator + file_);
		if (!yml.exists()) throw new IOException();

		Config.Type hm = new Config.Type();
		TreeSet<String> keys = new TreeSet<String>(default_.keySet());
		
		Configuration config = new Configuration(yml);
		config.load();
		
		for(String key : keys) {
			Object value = config.getProperty(key);
			Object expected = default_.get(key);
			
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
				Config.log.warning(plugin_ + " - " + (value == null ? "Missing value" : "Unexpected value for") + " " + key + " in [" + path_ + File.separator + file_ + "], using default.");
				value = expected;
			}
			hm.put(key, value);
		}

		return hm;
	}
	
	public static Config.Type getConfig(String plugin_, String path_, String file_, Config.Type default_) {
		try {
			return Config.read(plugin_, path_, file_, default_);
		} catch (Exception ex) {
			if (ex instanceof IOException)
				Config.create(plugin_, path_, file_, default_);
			return default_;
		}
	}
	
	public static void create(String plugin_, String path_, String file_, Config.Type contents_) {
		try {
			File f = new File(path_ + File.separator + file_);
			if (!f.exists()) {
				new File(path_).mkdir();
				f.createNewFile();
			}
			
			BufferedWriter out = new BufferedWriter(new FileWriter(path_ + File.separator + file_));
			TreeSet<String> keys = new TreeSet<String>(contents_.keySet());		
			
			Stack<String> s = new Stack<String>();
			for(String key : keys) {
				String[] levels = key.split("\\.");
				for(int x = 0; x < levels.length - 1; ++x) {
					try {
						if (!s.get(x).equals(levels[x])) for (int y = x; y < s.size(); ++y) s.removeElementAt(y);
						else continue;
					} catch (IndexOutOfBoundsException ex) { /**/ }
					out.write(Config.getTab(x) + levels[x] + ": ");
					out.newLine();
					s.push(levels[x]);
				}
				if (levels.length == 1) s.clear();

				Object value = contents_.get(key);
				out.write(Config.getTab(s.size()) + levels[levels.length - 1] + ": ");
				String line_out = "";
				if (value instanceof ArrayList<?>) {
					ArrayList<?> list = (ArrayList<?>)value;
					for (Object line : list)
						line_out += (line_out.equals("") ? "" : ",\n" + Config.getTab(s.size() + 1)) + (line instanceof String ? "'" : "") + line + (line instanceof String ? "'" : "");
					line_out = "[" + line_out + "]";
				} else line_out = (value instanceof String ? "'" : "") + value + (value instanceof String ? "'" : "");
				out.write(line_out);
				out.newLine();
			}
			
			out.flush();
			out.close();
			Config.log.info(plugin_ + " - Configuration file created [" + path_ + File.separator + file_ + "]!");
		} catch (Exception ex) {
			Config.log.warning(plugin_ + " - Unable to properly create config [" + path_ + File.separator + file_ + "]!");
		}
	}
	
	private static String getTab(int depth_) {
		String output = "";
		for (int x = 0; x < depth_; ++x) output += "  ";
		return output;
	}
}
