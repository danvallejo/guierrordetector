package edu.washington.cs.detector.util;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import edu.washington.cs.detector.AnomalyCallChain;

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
	
	/** This project-specific methods */
	public static <T> int countIterable(Iterable<T> c) {
		int count = 0;
		for(T t: c) {
			count++;
		}
		return count;
	}
	public static <T> void dumpCollection(Iterable<T> c, PrintStream out) {
		out.println(dumpCollection(c));
	}
	public static <T> void dumpCollection(Iterable<T> c, String fileName) {
		try {
			Files.writeToFile(dumpCollection(c), fileName);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	public static <T> String dumpCollection(Iterable<T> c) {
		StringBuilder sb = new StringBuilder();
		int num = 0;
		for(T t : c) {
			sb.append(t);
			sb.append(Globals.lineSep);
			num ++;
		}
		sb.append("Num in total: " + num);
		return sb.toString();
	}
	
	public static void dumpAnomalyCallChains(List<AnomalyCallChain> chains, String fileName) {
		dumpAnomalyCallChains(chains, new File(fileName));
	}
	
    public static void dumpAnomalyCallChains(List<AnomalyCallChain> chains, File file) {
		StringBuilder sb = new StringBuilder();
		
		int count = 0;
		for(AnomalyCallChain c : chains) {
			sb.append(count++ + "-th anomaly call chain");
			sb.append(Globals.lineSep);
			sb.append(c.getFullCallChainAsString());
			sb.append(Globals.lineSep);
			sb.append(Globals.lineSep);
		}
		
		try {
			Files.writeToFile(sb.toString(), file);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}