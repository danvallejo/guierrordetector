package edu.washington.cs.detector.util;

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;

public class Utils {
	
	public static String translateSlashToDot(String str) {
		assert str != null;
		return str.replace('/', '.');
	}
	
	public static String translateDotToSlash(String str) {
		assert str != null;
		return str.replace('.', '/');
	}
	
	public static void checkDirExistence(String dir) {
		File f = new File(dir);
		if(!f.isDirectory()) {
			throw new RuntimeException("File: " + f + " is not a dir");
		}
		if(!f.exists()) {
			throw new RuntimeException("Dir: " + f + " does not exist");
		}
	}
	
	public static void checkFileExistence(String dir) {
		File f = new File(dir);
		if(f.isDirectory()) {
			throw new RuntimeException("File: " + f + " is  a dir");
		}
		if(!f.exists()) {
			throw new RuntimeException("File: " + f + " does not exist");
		}
	}
	
	public static void checkPathEntryExistence(String path) {
		String[] entries = path.split(Globals.pathSep);
		for(String entry : entries) {
			File f = new File(entry);
			if(!f.exists()) {
				throw new RuntimeException("The entry: " + entry + " does not exist.");
			}
		}
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
	
	public static <T> Collection<T> iterableToCollection(Iterable<T> ts) {
		Collection<T> collection = new LinkedList<T>();
		for(T t : ts) {
			collection.add(t);
		}
		return collection;
 	}
}