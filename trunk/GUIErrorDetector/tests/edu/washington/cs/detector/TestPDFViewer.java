package edu.washington.cs.detector;

import java.io.IOException;

import junit.framework.TestCase;

import com.ibm.wala.core.tests.callGraph.CallGraphTestUtil;
import com.ibm.wala.util.io.FileProvider;

import edu.washington.cs.detector.util.PDFViewer;

public class TestPDFViewer extends TestCase {
	
	public void testDisplaySmallCG() throws IOException {
		String appPath =  TestCommons.testfolder + "helloworld";
		CGBuilder builder = new CGBuilder(appPath, FileProvider.getFile(CallGraphTestUtil.REGRESSION_EXCLUSIONS));
		builder.buildCG();
		
		PDFViewer.viewCG("smallcg.pdf", builder.getAppCallGraph());
		
	}
	
	public void testDisplayPASensitivity() throws IOException {
		String appPath =  TestCommons.testfolder + "paflowsensitive";
		CGBuilder builder = new CGBuilder(appPath, FileProvider.getFile(CallGraphTestUtil.REGRESSION_EXCLUSIONS));
		builder.buildCG();
		
		PDFViewer.viewCG("smallpa.pdf", builder.getAppCallGraph());
		
	}
	
}