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

package com.crowebird.bukkit.plugins.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;
import java.util.Vector;
import java.util.logging.Logger;

import org.bukkit.util.config.Configuration;

public class Config {

	protected static final Logger log = Logger.getLogger("Minecraft");
	
	private static class AL<T> extends ArrayList<T> {
		private static final long serialVersionUID = 225L;
		
		public Class<?> getParameterized() { return this.getClass(); }
	}
	
	public static class ALInteger extends Config.AL<Integer> {
		private static final long serialVersionUID = 2254L;
		
		public Class<?> getParameterized() { return Integer.class; }
	}
	
	public static class ALString extends Config.AL<String> {
		private static final long serialVersionUID = 2257L;
		
		public Class<?> getParameterized() { return String.class; }
	}
	
	public static class Type extends HashMap<String, Object> {
		private static final long serialVersionUID = 28L;
		
		public boolean has(String key_, Object value_) {	
			if (!this.containsKey(key_)) return false;
			Object value = this.get(key_);
			
			if (value instanceof ArrayList<?>) return ((ArrayList<?>) value).contains(value_);
			else return value.equals(value_);
		}
	};
	
	public static boolean has(Config.Type config_, String node_, Object value_) { return Config.has(config_, "", node_, value_); }
	public static boolean has(Config.Type config_, String world_, String node_, Object value_) { return Config.has(config_, world_, node_, value_, ""); }
	public static boolean has(Config.Type config_, String world_, String node_, Object value_, String alternative_) {
		String key = (world_.equals("") ? "" : world_ + ".") + node_;
		if (config_.containsKey(key)) return config_.has(key, value_);
		if (alternative_.equals("")) return false;
		return config_.has(alternative_ + "." + node_, value_);
	}
	
	public static Config.Type read(String plugin_, String path_, String file_, Config.Type default_, boolean ignore_) throws IOException {
		return Config.read(plugin_, path_, file_, default_, ignore_, true);
	}
	public static Config.Type read(String plugin_, String path_, String file_, Config.Type default_, boolean ignore_, boolean prefix_) throws IOException {
		String file = path_ + File.separator + file_ + ".yml";
		
		File yml = new File(file);
		if (!yml.exists()) throw new IOException("File does not exist!");

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
	
	public static Config.Type getConfig(String plugin_, String path_, String file_, Config.Type default_, boolean create_, boolean ignore_) {
		return Config.getConfig(plugin_, path_, file_, default_, create_, ignore_, true);
	}
	public static Config.Type getConfig(String plugin_, String path_, String file_, Config.Type default_, boolean create_, boolean ignore_, boolean prefix_) {
		try {
			return Config.read(plugin_, path_, file_, default_, ignore_);
		} catch (Exception ex) {
			if (!create_) {
				Config.log.warning(plugin_ + " - Unable to read " + file_ + " [" + path_ + File.separator + file_ + "], ignoring file.");
				return new Config.Type();
			}
			if (ex instanceof IOException)
				Config.create(plugin_, path_, file_, default_);
			
			if (prefix_) {
				Config.Type hm = new Config.Type();
				Set<String> keys = default_.keySet();
				for(String key : keys) hm.put(file_ + "." + key, default_.get(key));
				return hm;
			} return default_;
		}
	}
	
	public static void create(String plugin_, String path_, String file_, Config.Type contents_) { Config.create(plugin_, path_, file_, contents_, false); }
	public static void create(String plugin_, String path_, String file_, Config.Type contents_, boolean content_prefixed) {
		file_ += ".yml";
		try {
			File f = new File(path_ + File.separator + file_);
			boolean exists = false;
			if (!f.exists()) {
				new File(path_).mkdir();
				f.createNewFile();
				exists = true;
			}
			
			BufferedWriter out = new BufferedWriter(new FileWriter(path_ + File.separator + file_));
			TreeSet<String> keys = new TreeSet<String>(contents_.keySet());		
			
			Stack<String> s = new Stack<String>();
			for(String key : keys) {
				String[] levels = key.split("\\.");
				int length = levels.length;
				int start = 0;
				if (content_prefixed) {
					length -= 1;
					start = 1;
				}
				for(int x = start; x < levels.length - 1; ++x) {
					try {
						if (!s.get(x).equals(levels[x])) for (int y = x; y < s.size(); ++y) s.removeElementAt(y);
						else continue;
					} catch (IndexOutOfBoundsException ex) { /**/ }
					out.write(Config.getTab(x) + levels[x] + ": ");
					out.newLine();
					s.push(levels[x]);
				}
				if (length == 1) s.clear();

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
			if (!exists) Config.log.info(plugin_ + " - Configuration file created [" + path_ + File.separator + file_ + "]!");
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
