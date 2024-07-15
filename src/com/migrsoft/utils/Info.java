package com.migrsoft.utils;

import java.nio.file.Paths;

public class Info {

	public enum OS {
		Unknown,
		macOS,
		Windows,
		Linux,
	}

	public static OS getOsType() {
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

	public static String getCurrentPath() {
		return Paths.get(".").toAbsolutePath().normalize().toString();
	}
}
