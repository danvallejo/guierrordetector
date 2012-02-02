package edu.washington.cs.detector.util;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipException;

import com.ibm.wala.core.tests.callGraph.CallGraphTestUtil;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.shrikeCT.AnnotationsReader.UnimplementedException;
import com.ibm.wala.shrikeCT.InvalidClassFileException;
import com.ibm.wala.util.io.FileProvider;

import edu.washington.cs.detector.CGBuilder;
import edu.washington.cs.detector.TestCommons;
import junit.framework.TestCase;

public class TestNativeAnnotationProcessor extends TestCase {

	public void testNativeAnnotationProcessor() throws IOException, ClassHierarchyException, InvalidClassFileException, UnimplementedException {
		String appPath =  TestCommons.testfolder + "nativeannotation";
		CGBuilder builder = new CGBuilder(appPath, FileProvider.getFile(CallGraphTestUtil.REGRESSION_EXCLUSIONS));
		builder.makeScopeAndClassHierarchy();
		
		ClassHierarchy cha = builder.getClassHierarchy();
		
		NativeAnnotationProcessor.getNativeCallRelations(cha);
	}
	

	//using the Java reflection approach
	public void testNativeAnnotation() {
		String jarFileName = //"D:\\research\\guierror\\subjects\\android-programs\\extracted\\sgtpuzzles.jar";
		    "D:\\research\\guierror\\subjects\\android-programs\\extracted\\sgtpuzzles-annotation.jar";
		
		String lib = "D:\\research\\guierror\\eclipsews\\GUIErrorDetector\\exp-subjects\\original-android.jar";
		try {
			Set<Class<?>> loadedClasses = NativeAnnotationProcessor.loadAllClasses(jarFileName, lib);
			System.out.println("Number of loaded class: " + loadedClasses.size());
			
			//get the relations
			Map<String, Collection<String>> relations
			    = NativeAnnotationProcessor.findCalledByNativesAnntationFromJarFile(jarFileName, lib);
			System.out.println(relations);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void testNativeMethodPairs() {
		String jarFileName = //"D:\\research\\guierror\\subjects\\android-programs\\extracted\\sgtpuzzles.jar";
		    "D:\\research\\guierror\\subjects\\android-programs\\extracted\\sgtpuzzles-annotation.jar";
		
		String lib = "D:\\research\\guierror\\eclipsews\\GUIErrorDetector\\exp-subjects\\original-android.jar";
		try {
			Collection<String[]> pairs = NativeAnnotationProcessor.getAllCallingPairsByNativeAnnotations(jarFileName, lib);
			for(String[] pair : pairs) {
				System.out.println(pair[0] + "  -> " + pair[1]);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
