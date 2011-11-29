package edu.washington.cs.detector.util;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipException;

import edu.washington.cs.detector.TestCommons;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class TestUtils extends TestCase {
	public static Test suite() {
		return new TestSuite(TestUtils.class);
	}
	
	public void testGetDeclardWidget() throws ZipException, IOException {
		String path = //"E:\\market_20101102.tar\\market_20101102\\applications\\Comics\\cat.bcnmultimedia.paraboles.apk";
			"D:\\research\\guierror\\eclipsews\\TestAndroid";
		Collection<String> widgets = AndroidUtils.extractAllWidgets(new File(path));
		System.out.println(widgets);
		assertEquals(3, widgets.size());
	}
	
	public void testClassesInAndroidLayout() throws IOException {
		String path = "D:\\research\\guierror\\eclipsews\\TestAndroid\\res\\layout\\main.xml";
		String xmlContent = Files.getFileContents(path);
		Collection<String> declaredClasses  = AndroidUtils.extractWidgets(xmlContent);
		System.out.println(declaredClasses);
		assertEquals("No of declared classes is incorrect", 3, declaredClasses.size() );
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