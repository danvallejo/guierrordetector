package edu.washington.cs.detector.util;

import java.io.IOException;
import java.util.Collection;

public class EclipsePluginUtils {
	
	public static Collection<String> getAllDeclaredClasses(String jarFile) {
		try {
			return Utils.extractClassFromPluginXML(jarFile);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
