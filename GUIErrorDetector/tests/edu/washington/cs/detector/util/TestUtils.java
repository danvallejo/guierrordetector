package edu.washington.cs.detector.util;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipException;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.ipa.cha.ClassHierarchyException;

import edu.washington.cs.detector.CGBuilder;
import edu.washington.cs.detector.TestCommons;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class TestUtils extends TestCase {
	public static Test suite() {
		return new TestSuite(TestUtils.class);
	}
	
	public void testGetCustomizedWidget() throws IOException, ClassHierarchyException {
		String appPath = "D:\\research\\guierror\\eclipsews\\TestAndroid\\bin\\classes\\test\\android"
			+ Globals.pathSep +
			"D:\\Java\\android-sdk-windows\\platforms\\android-8\\android.jar";
		CGBuilder builder = new CGBuilder(appPath);
		builder.makeScopeAndClassHierarchy();
		
		String xmlContent = Files.getFileContents("D:\\research\\guierror\\eclipsews\\TestAndroid\\res\\layout\\main.xml");
		Collection<String> classes = AndroidUtils.extractCustomizedWidgets(builder.getClassHierarchy(), xmlContent);
		System.out.println(classes);
	}
	
	public void testGetDeclardWidget() throws ZipException, IOException, ClassHierarchyException {
		String path = //"E:\\market_20101102.tar\\market_20101102\\applications\\Comics\\cat.bcnmultimedia.paraboles.apk";
			"D:\\research\\guierror\\eclipsews\\TestAndroid";
		
		String appPath = "D:\\research\\guierror\\eclipsews\\TestAndroid\\bin\\classes\\test\\android"
			+ Globals.pathSep +
			"D:\\Java\\android-sdk-windows\\platforms\\android-8\\android.jar";
		CGBuilder builder = new CGBuilder(appPath);
		builder.makeScopeAndClassHierarchy();
		
		Collection<String> widgets = AndroidUtils.extractAllWidgets(builder.getClassHierarchy(), new File(path));
		System.out.println(widgets);
		assertEquals(4, widgets.size());
	}
	
	public void testClassesInAndroidLayout() throws IOException {
		String path = "D:\\research\\guierror\\eclipsews\\TestAndroid\\res\\layout\\main.xml";
		String xmlContent = Files.getFileContents(path);
		Collection<String> declaredClasses  = AndroidUtils.extractAndroidWidgets(xmlContent);
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