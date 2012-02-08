package edu.washington.cs.detector.util;

import java.io.IOException;
import java.util.Map;

import com.ibm.wala.core.tests.callGraph.CallGraphTestUtil;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;

import edu.washington.cs.detector.CGBuilder;
import junit.framework.TestCase;

public class LayoutParserTest extends TestCase {
	
	public void testSGTPuzzlers() throws IOException, ClassHierarchyException {
		String path = 
			"D:\\research\\guierror\\subjects\\android-programs\\extracted\\sgtpuzzles.jar"
			+ Globals.pathSep +	"D:\\research\\guierror\\eclipsews\\GUIErrorDetector\\exp-subjects\\original-android.jar";
		String dir = "D:\\research\\guierror\\subjects\\android-programs\\extracted\\SGTPuzzles-9306-7.apk";
		
		CGBuilder builder = new CGBuilder(path,CallGraphTestUtil.REGRESSION_EXCLUSIONS);
	    builder.makeScopeAndClassHierarchy();
	    ClassHierarchy cha = builder.getClassHierarchy();
	    
	    Map<String, String> uiMap = AndroidLayoutParser.extractAndroidUIMapping(cha, dir);
	    for(String key : uiMap.keySet()) {
	    	System.out.println(key + ": --- " + uiMap.get(key));
	    }
	}
	
    public void testMytracks() throws IOException, ClassHierarchyException {
    	String path = 
    		"D:\\research\\guierror\\subjects\\android-programs\\sourcecode\\mytracks-april-29\\mytracks-april-29.jar"
			+ Globals.pathSep +
			"D:\\research\\guierror\\subjects\\android-programs\\sourcecode\\mytracks-april-29\\mytrackslib-april-29.jar"
			+ Globals.pathSep +
			"D:\\research\\guierror\\subjects\\android-programs\\sourcecode\\mytracks-april-29\\MyTracks\\libs\\accounts.jar"
		    + Globals.pathSep +
		    "D:\\research\\guierror\\subjects\\android-programs\\sourcecode\\mytracks-april-29\\MyTracks\\libs\\google-api-client-1.2.2-alpha.jar"
		    + Globals.pathSep +
		    "D:\\research\\guierror\\subjects\\android-programs\\sourcecode\\mytracks-april-29\\MyTracks\\libs\\google-common.jar"
		    + Globals.pathSep +
		    "D:\\research\\guierror\\subjects\\android-programs\\sourcecode\\mytracks-april-29\\MyTracks\\libs\\googleloginclient-helper.jar"
		    +Globals.pathSep +
		    "D:\\research\\guierror\\subjects\\android-programs\\sourcecode\\mytracks-april-29\\MyTracksLib\\libs\\protobuf-java-2.3.0-lite.jar"
		    + Globals.pathSep + 
			"D:\\research\\guierror\\eclipsews\\GUIErrorDetector\\exp-subjects\\original-android.jar"
		    + Globals.pathSep +
		    "D:\\Java\\android-sdk-windows\\add-ons\\addon-google_apis-google_inc_-8\\libs\\maps.jar";
    	
		String dir = "D:\\research\\guierror\\subjects\\android-programs\\sourcecode\\mytracks-april-29\\MyTracks";
		
		CGBuilder builder = new CGBuilder(path,CallGraphTestUtil.REGRESSION_EXCLUSIONS);
	    builder.makeScopeAndClassHierarchy();
	    ClassHierarchy cha = builder.getClassHierarchy();
	    
	    Map<String, String> uiMap = AndroidLayoutParser.extractAndroidUIMapping(cha, dir);
	    for(String key : uiMap.keySet()) {
	    	System.out.println(key + ": --- " + uiMap.get(key));
	    }
	}

    public void testFennec() throws IOException, ClassHierarchyException {
    	String path = 
    		"D:\\research\\guierror\\subjects\\android-programs\\extracted\\mozilla-android-abstract-removed\\classes\\org\\mozilla"
			+ Globals.pathSep
			+ "D:\\research\\guierror\\eclipsews\\GUIErrorDetector\\exp-subjects\\original-android.jar";
    	
		String dir = "D:\\research\\guierror\\subjects\\android-programs\\extracted\\mozilla-android-base";
		
		CGBuilder builder = new CGBuilder(path,CallGraphTestUtil.REGRESSION_EXCLUSIONS);
	    builder.makeScopeAndClassHierarchy();
	    ClassHierarchy cha = builder.getClassHierarchy();
	    
	    Map<String, String> uiMap = AndroidLayoutParser.extractAndroidUIMapping(cha, dir);
	    for(String key : uiMap.keySet()) {
	    	System.out.println(key + ": --- " + uiMap.get(key));
	    }
    }
}