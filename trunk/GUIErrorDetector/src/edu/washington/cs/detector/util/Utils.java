package edu.washington.cs.detector.util;

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