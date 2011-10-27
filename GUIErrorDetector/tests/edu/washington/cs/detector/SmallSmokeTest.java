package edu.washington.cs.detector;

import edu.washington.cs.detector.util.TestPropertyReader;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * The unit tests that must pass when modifying the program, or
 * configuring in a new environment
 * */
public class SmallSmokeTest extends TestCase {
	
	public static Test suite() {
		TestSuite suite = new TestSuite();
		
		suite.addTest(TestFileExistence.suite());
		suite.addTest(TestPropertyReader.suite());
		suite.addTest(TestCGWithPDFViewer.suite());
		
		suite.addTest(TestUseCustomizedEntries.suite());
		suite.addTest(TestSWTExamples.suite());
		suite.addTest(TestSimpleExamples.suite());
		suite.addTest(TestSimpleEclipsePlugins.suite());
		
		return suite;
	}

}