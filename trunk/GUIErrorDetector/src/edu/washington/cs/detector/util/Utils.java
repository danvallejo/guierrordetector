package edu.washington.cs.detector.util;

public class Utils {
	
	public static String translateSlashToDot(String str) {
		assert str != null;
		return str.replace('/', '.');
	}
	
}