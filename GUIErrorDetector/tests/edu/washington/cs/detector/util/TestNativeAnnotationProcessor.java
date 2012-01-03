package edu.washington.cs.detector.util;

import java.io.IOException;

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
	
}
