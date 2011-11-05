package edu.washington.cs.detector.util;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import edu.washington.cs.detector.TestCommons;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class TestUtils extends TestCase {
	public static Test suite() {
		return new TestSuite(TestUtils.class);
	}

	public void testExtractPluginXML() throws IOException {
		String content = Utils
				.getPluginXMLContent("D:\\research\\guierror\\subjects\\site-1.6.18\\plugins\\org.tigris.subversion.subclipse.ui_1.6.18.jar");
		
		Collection<String> clazzList = Utils.extractClasses(content);
		for(String clazz : clazzList) {
			System.out.println(clazz);
		}
	}
	
	public void testExtractAllPluginXML() throws IOException {
		String pluginDir = TestCommons.rse_303_dir + Globals.fileSep + "plugins";
		Set<String> allClasses = new LinkedHashSet<String>();
		
		List<String> jarFiles = TestCommons.getNonSourceNonTestsJars(pluginDir);
		for(String jarFile : jarFiles) {
			allClasses.addAll(Utils.extractClassFromPluginXML(jarFile));
		}
		System.out.println("Number of exposed class: " + allClasses.size());
	}
}