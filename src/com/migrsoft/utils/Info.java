package com.migrsoft.utils;

public class Info {
	
	public static final int OS_UNKNOWN = 0;

	public static final int OS_MAC = 1;
	
	public static final int OS_WINDOWS = 2;
	
	public static final int OS_LINUX = 3;
	
	static public int getOsType() {
		String os = System.getProperty("os.name");
		if (os.startsWith("Mac")) {
			return OS_MAC;
		} else if (os.startsWith("Windows")) {
			return OS_WINDOWS;
		} else if (os.startsWith("Linux")) {
			return OS_LINUX;
		} else {
			return OS_UNKNOWN;
		}		
	}
}
