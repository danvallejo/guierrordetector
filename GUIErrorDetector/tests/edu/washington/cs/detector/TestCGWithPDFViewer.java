package edu.washington.cs.detector;

import java.io.IOException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.ibm.wala.core.tests.callGraph.CallGraphTestUtil;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.util.io.FileProvider;

import edu.washington.cs.detector.CGBuilder.CG;
import edu.washington.cs.detector.util.PDFViewer;
import edu.washington.cs.detector.util.WALAUtils;

public class TestCGWithPDFViewer extends TestCase {
	
	public static Test suite() {
		return new TestSuite(TestCGWithPDFViewer.class);
	}
	
	public void testMultiThread() throws IOException {
		String appPath =  TestCommons.testfolder + "multithreads";
		CGBuilder builder = new CGBuilder(appPath, FileProvider.getFile(CallGraphTestUtil.REGRESSION_EXCLUSIONS));
		
		builder.setCGType(CG.OneCFA);
		
//		builder.setCGType(CG.CFA);
//		builder.setCFAPrecision(4);
		builder.buildCG();
		
		String irstr = WALAUtils.getAllIRAsString(builder.getCallGraph().getFakeRootNode());
		System.out.println(irstr);
		
		assertEquals(23, builder.getAppCallGraph().getNumberOfNodes());
		PDFViewer.viewCG("multithread.pdf", builder.getAppCallGraph());
	}
	
	public void testCGWithoutConstructor() throws IOException, ClassHierarchyException {
		String appPath =  TestCommons.testfolder + "cgiwthoutnew";
		CGBuilder builder = new CGBuilder(appPath, FileProvider.getFile(CallGraphTestUtil.REGRESSION_EXCLUSIONS));
		builder.makeScopeAndClassHierarchy();
		
		//builder.setCGType(CG.RTA);
//		Iterable<Entrypoint> entries = CGEntryManager.getAllPublicMethods(builder,
//				"test.cgiwthoutnew.CGWithoutConstructor", "test.cgiwthoutnew.A");
//		builder.buildCG(entries);
		
		builder.buildCG();
		
		PDFViewer.viewCG("cgNoConstructor.pdf", builder.getAppCallGraph());
		
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
	
	public void testCallNativeMethod() throws IOException {
		String appPath =  TestCommons.testfolder + "nativemethod";
		CGBuilder builder = new CGBuilder(appPath, FileProvider.getFile(CallGraphTestUtil.REGRESSION_EXCLUSIONS));
		builder.setCGType(CG.ZeroCFA);
		builder.buildCG();
		
		assertEquals(5, builder.getAppCallGraph().getNumberOfNodes());
		PDFViewer.viewCG("nativecall.pdf", builder.getAppCallGraph());
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