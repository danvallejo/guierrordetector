package edu.washington.cs.detector.util;

public class Utils {
	
	public static String translateSlashToDot(String str) {
		assert str != null;
		return str.replace('/', '.');
	}
	
	public static String translateDotToSlash(String str) {
		assert str != null;
		return str.replace('.', '/');
	}
}