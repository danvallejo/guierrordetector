package edu.washington.cs.detector;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import edu.washington.cs.detector.util.EclipsePluginCommons;
import edu.washington.cs.detector.util.Files;
import edu.washington.cs.detector.util.Utils;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class TestFileExistence extends TestCase {
	
	public static Test suite() {
		return new TestSuite(TestFileExistence.class);
	}

	public void testDirExistence() {
		Utils.checkDirExistence(TestCommons.swtEx);
		Utils.checkDirExistence(TestCommons.testfolder);
		Utils.checkDirExistence(TestCommons.cdt_60_dir);
		Utils.checkDirExistence(TestCommons.mylyn_362_dir);
		Utils.checkDirExistence(TestCommons.rse_303_dir);
		Utils.checkDirExistence(TestCommons.pde_eclipseplugin_dir);
		Utils.checkDirExistence(TestCommons.plugintest_bin_dir);
		
		Utils.checkDirExistence(EclipsePluginCommons.PLUGIN_DIR);
	}
	
	public void testFileExistence() {
		Utils.checkFileExistence(SWTAppUIErrorMain.swtJar);
	}
	
	public void testPathExistence() {
		Utils.checkPathEntryExistence(EclipsePluginCommons.DEPENDENT_JARS);
	}
	
	public void testCheckingMethodFiles() throws IOException {
		String fPath = "./src/checking_methods.txt";
		Utils.checkFileExistence(fPath);
		List<String> list = Files.readWhole(fPath);
		System.out.println(list);
		assertEquals(list.size(), UIAnomalyMethodFinder.getCheckingMethods().length);
		for(String str : UIAnomalyMethodFinder.getCheckingMethods()) {
			assertTrue("str: " + str + " not in: " + fPath, list.contains(str));
		}
	}
	
	public void testGetResource() {
		String xmlFile = "samplereflection.xml";
		ClassLoader cl = edu.washington.cs.detector.util.Utils.class.getClassLoader();
		InputStream s = cl.getResourceAsStream(xmlFile);
		System.out.println(s);
		assertNotNull("The xmlfile: " + xmlFile + " does not exist.", s);
	}

}
