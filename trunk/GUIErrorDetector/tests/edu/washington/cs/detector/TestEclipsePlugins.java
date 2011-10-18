package edu.washington.cs.detector;

import java.io.IOException;

import com.ibm.wala.ipa.cha.ClassHierarchyException;

import junit.framework.TestCase;

public class TestEclipsePlugins extends TestCase {
	
	public void testSimplePlugin() throws ClassHierarchyException, IOException {
		EclipsePluginUIErrorMain.APP_PATH = "D:\\research\\guierror\\eclipsews\\plugintest\\bin";
		EclipsePluginUIErrorMain.UI_CLASSES_FILE = "./tests/uiclasses_plugintest.txt";
		
		EclipsePluginUIErrorMain.main(null);
	}

}
