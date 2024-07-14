package com.migrsoft.utils;

public class Info {

	public enum OS {
		Unknown,
		macOS,
		Windows,
		Linux,
	}

	static public OS getOsType() {
		String os = System.getProperty("os.name");
		if (os.startsWith("Mac")) {
			return OS.macOS;
		} else if (os.startsWith("Windows")) {
			return OS.Windows;
		} else if (os.startsWith("Linux")) {
			return OS.Linux;
		} else {
			return OS.Unknown;
		}		
	}
}
