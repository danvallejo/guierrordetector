package edu.washington.cs.detector;

import java.io.IOException;

import com.ibm.wala.ipa.cha.ClassHierarchyException;

import edu.washington.cs.detector.experiments.swing.AbstractSwingTest;
import edu.washington.cs.detector.util.Log;

import junit.framework.Test;
import junit.framework.TestSuite;

public class TestSimpleSwingExamples extends AbstractSwingTest {

	public static Test suite() {
		return new TestSuite(TestSimpleSwingExamples.class);
	}
	
	public void testSwingError() throws IOException, ClassHierarchyException {
		String appPath = TestCommons.testfolder + "swingerror";
		Log.logConfig("./log.txt");
		UIAnomalyMethodFinder.DEBUG = true;
		this.checkCallChainNumber(2, appPath, new String[]{"test.swingerror"});
	}
	
	public void testSwingNoError() throws IOException, ClassHierarchyException {
		String appPath = TestCommons.testfolder + "swingnoerror";
//		Log.logConfig("./log.txt");
//		UIAnomalyMethodFinder.DEBUG = true;
		this.checkCallChainNumber(0, appPath,  new String[]{"test.swingerror"});
	}
}
