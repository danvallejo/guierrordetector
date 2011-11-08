package edu.washington.cs.detector;

import edu.washington.cs.detector.util.PDFViewer;
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
		PDFViewer.DISABLE_VIEW_CG = true;
		
		TestSuite suite = new TestSuite();
		
		//remove this, since it need environment-specific settings
		//suite.addTest(TestUtils.suite());
		suite.addTest(TestFileExistence.suite());
		suite.addTest(TestPropertyReader.suite());
		
		suite.addTest(TestCGWithPDFViewer.suite());
		suite.addTest(TestCGBuilder.suite());
		suite.addTest(TestUIThreadStartFinder.suite());
		suite.addTest(TestUIAnomalyDetector.suite());
		
		suite.addTest(TestUseCustomizedEntries.suite());
		suite.addTest(TestSWTExamples.suite());
		suite.addTest(TestSimpleExamples.suite());
		suite.addTest(TestSimpleEclipsePlugins.suite());
		
		return suite;
	}

}