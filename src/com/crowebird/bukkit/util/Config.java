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

public class Config {

	protected static final Logger log = Logger.getLogger("Minecraft");
	
	public static HashMap<String, ArrayList<String>> read(String path_, String file_) throws IOException {
		File yml = new File(path_ + File.pathSeparator + file_);
		if (!yml.exists()) throw new IOException();
		
		HashMap<String, ArrayList<String>> hm = new HashMap<String, ArrayList<String>>();
	
		
		return hm;
	}
	
	public static void create(String path_, String file_, HashMap<String, ArrayList<String>> contents_) {
		try {
			new File(path_).mkdir();
			new File(path_ + File.pathSeparator + file_).createNewFile();
			BufferedWriter out = new BufferedWriter(new FileWriter(path_ + File.pathSeparator + file_));
			
			Iterator<Entry<String, ArrayList<String>>> i = contents_.entrySet().iterator();
			while(i.hasNext()) {
				Entry<String, ArrayList<String>> e = i.next();
				out.write(e.getKey());
				for (String line : e.getValue()) {
					out.newLine();
					out.write("- " + line);
				}
				out.newLine();
			}
			
			out.flush();
			out.close();
		} catch (Exception ex) {
			log.log(Level.WARNING, "Unable to create " + path_ + File.pathSeparator + file_ + "!");
		}
	}
	
}
