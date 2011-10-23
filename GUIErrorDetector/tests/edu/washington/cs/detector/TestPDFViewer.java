package edu.washington.cs.detector;

import java.io.IOException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.ibm.wala.core.tests.callGraph.CallGraphTestUtil;
import com.ibm.wala.util.io.FileProvider;

import edu.washington.cs.detector.util.PDFViewer;

public class TestPDFViewer extends TestCase {
	
	public static Test suite() {
		return new TestSuite(TestPDFViewer.class);
	}
	
	public void testDisplaySmallCG() throws IOException {
		String appPath =  TestCommons.testfolder + "helloworld";
		CGBuilder builder = new CGBuilder(appPath, FileProvider.getFile(CallGraphTestUtil.REGRESSION_EXCLUSIONS));
		builder.buildCG();
		
		assertEquals(14, builder.getAppCallGraph().getNumberOfNodes());
		PDFViewer.viewCG("smallcg.pdf", builder.getAppCallGraph());
		
	}
	
	public void testDisplayPASensitivity() throws IOException {
		String appPath =  TestCommons.testfolder + "paflowsensitive";
		CGBuilder builder = new CGBuilder(appPath, FileProvider.getFile(CallGraphTestUtil.REGRESSION_EXCLUSIONS));
		builder.buildCG();
		
		assertEquals(8, builder.getAppCallGraph().getNumberOfNodes());
		PDFViewer.viewCG("smallpa.pdf", builder.getAppCallGraph());
		
	}
	
}