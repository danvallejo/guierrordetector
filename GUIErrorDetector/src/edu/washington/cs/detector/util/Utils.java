package edu.washington.cs.detector.util;

import java.util.List;

public class Utils {
	
	public static String translateSlashToDot(String str) {
		assert str != null;
		return str.replace('/', '.');
	}
	
	public static String translateDotToSlash(String str) {
		assert str != null;
		return str.replace('.', '/');
	}
	
	public static <T> boolean includedIn(T target, T[] array) {
		assert target != null;
		for(T elem : array) {
			if(elem != null && elem.equals(target)) {
				return true;
			}
		}
		return false;
 	}
}