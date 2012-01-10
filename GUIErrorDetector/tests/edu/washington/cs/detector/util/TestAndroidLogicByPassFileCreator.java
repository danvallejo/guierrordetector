package edu.washington.cs.detector.util;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;

import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;

import edu.washington.cs.detector.CGBuilder;
import edu.washington.cs.detector.UIAnomalyDetector;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class TestAndroidLogicByPassFileCreator extends TestCase {
	
	public static Test suite() {
		return new TestSuite(TestAndroidLogicByPassFileCreator.class);
	}
	
	public void testCreateSampleFile() throws IOException {
		AndroidLogicByPassFileCreator creator = new AndroidLogicByPassFileCreator(null, null);
		Collection<String> javaClassNames = new LinkedList<String>();
		javaClassNames.add("android.widget.Button");
		javaClassNames.add("android.widget.TextView");
		creator.createByPassLogicFile("androidsamplereflection-test.xml", javaClassNames);
	}
	
	public void testCreateSampleFileWithCHA() throws IOException, ClassHierarchyException {
		String appPath =
			Utils.concatenate(Utils.getClassesRecursive("D:\\research\\guierror\\eclipsews\\TestAndroid\\bin"), Globals.pathSep) 
			    + Globals.pathSep +
			"D:\\research\\guierror\\eclipsews\\GUIErrorDetector\\exp-subjects\\original-android.jar";
		
		String layoutDir = "D:\\research\\guierror\\eclipsews\\TestAndroid";
		
		//initialize the classhierarchy
		CGBuilder builder = new CGBuilder(appPath,UIAnomalyDetector.EXCLUSION_FILE_SWING);
	    builder.makeScopeAndClassHierarchy();
	    ClassHierarchy cha = builder.getClassHierarchy();
	    
	    AndroidLogicByPassFileCreator creator = new AndroidLogicByPassFileCreator(layoutDir, cha);
	    creator.createByPassLogicFile("androidsamplereflection-test.xml");
	}
	
}
