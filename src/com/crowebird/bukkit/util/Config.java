package com.crowebird.bukkit.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.util.config.Configuration;

import com.crowebird.bukkit.AntiGrief.AntiGrief;

public class Config {

	protected static final Logger log = Logger.getLogger("Minecraft");
	
	public static class type extends HashMap<String, ArrayList<String>> {
		private static final long serialVersionUID = 1L;
	};
	
	public static Config.type read(String path_, String file_, Config.type default_) throws Exception {
		File yml = new File(path_ + File.separator + file_);
		if (!yml.exists()) throw new IOException("File does not exist!");

		Config.type hm = new Config.type();
		String[] keys = default_.keySet().toArray(new String[]{});
		
		Configuration config = new Configuration(yml);
		config.load();
		for(String key : keys) {
			ArrayList<String> values = (ArrayList<String>)config.getStringList(key, default_.get(key));
			hm.put(key, values);
		}

		return hm;
	}
	
	public static Config.type getConfig(String path_, String file_, Config.type default_) {
		try {
			return Config.read(path_, file_, default_);
		} catch (Exception ex) {
			if (ex instanceof IOException)
				Config.create(path_, file_, default_);
			log.info(AntiGrief.pdf.getName() + " - using default values!");
			return default_;
		}
	}
	
	public static void create(String path_, String file_, Config.type contents_) {
		try {
			File f = new File(path_ + File.separator + file_);
			if (!f.exists()) {
				new File(path_).mkdir();
				f.createNewFile();
			}
			
			BufferedWriter out = new BufferedWriter(new FileWriter(path_ + File.separator + file_));
			Iterator<Entry<String, ArrayList<String>>> i = contents_.entrySet().iterator();
			while(i.hasNext()) {
				Entry<String, ArrayList<String>> e = i.next();
				out.write(e.getKey() + ": ");
				String line_out = "";
				ArrayList<String> values = e.getValue();
				for (String line : values)
					line_out += (line_out.equals("") ? "" : ",\n") + "'" + line + "'";
				out.write("[" + line_out + "]");
				out.newLine();
			}
			
			out.flush();
			out.close();
			log.log(Level.INFO, "File created: " + path_ + File.separator + file_ + "!");
		} catch (Exception ex) {
			log.log(Level.WARNING, "Unable to create " + path_ + File.separator + file_ + "!");
		}
	}
	
}
