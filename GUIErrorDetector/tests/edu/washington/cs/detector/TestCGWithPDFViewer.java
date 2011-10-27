package edu.washington.cs.detector;

import java.io.IOException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.ibm.wala.core.tests.callGraph.CallGraphTestUtil;
import com.ibm.wala.util.io.FileProvider;

import edu.washington.cs.detector.CGBuilder.CG;
import edu.washington.cs.detector.util.PDFViewer;

public class TestCGWithPDFViewer extends TestCase {
	
	public static Test suite() {
		return new TestSuite(TestCGWithPDFViewer.class);
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
	
	public void testImpreciseCG() throws IOException {
		String appPath =  TestCommons.testfolder + "imprecisecg";
		CGBuilder builder = new CGBuilder(appPath, FileProvider.getFile(CallGraphTestUtil.REGRESSION_EXCLUSIONS));
		builder.setCGType(CG.ZeroOneContainerCFA);
		builder.buildCG();
		
		
		assertEquals(12, builder.getAppCallGraph().getNumberOfNodes());
		PDFViewer.viewCG("imprecisecg.pdf", builder.getAppCallGraph());
	}
	
	public void testImpactOfControlFlow() throws IOException {
		String appPath =  TestCommons.testfolder + "controlflow";
		CGBuilder builder = new CGBuilder(appPath, FileProvider.getFile(CallGraphTestUtil.REGRESSION_EXCLUSIONS));
		builder.setCGType(CG.ZeroOneContainerCFA);
		builder.buildCG();
		
		
		assertEquals(6, builder.getAppCallGraph().getNumberOfNodes());
		PDFViewer.viewCG("controlflow.pdf", builder.getAppCallGraph());
	}
	
	public void testExceptionhandling() throws IOException {
		String appPath =  TestCommons.testfolder + "exceptionhandling";
		CGBuilder builder = new CGBuilder(appPath, FileProvider.getFile(CallGraphTestUtil.REGRESSION_EXCLUSIONS));
		builder.setCGType(CG.ZeroOneContainerCFA);
		builder.buildCG();
		
		
		assertEquals(5, builder.getAppCallGraph().getNumberOfNodes());
		PDFViewer.viewCG("exceptioncg.pdf", builder.getAppCallGraph());
	}
	
}