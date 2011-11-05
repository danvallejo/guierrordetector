package edu.washington.cs.detector;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.cha.ClassHierarchyException;

import edu.washington.cs.detector.CGBuilder.CG;
import edu.washington.cs.detector.util.Globals;
import edu.washington.cs.detector.util.PDFViewer;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class TestUIAnomalyDetector extends TestCase {

	public static Test suite() {
		return new TestSuite(TestUIAnomalyDetector.class);
	}

	public void testNoErrorUsingOnePublicMethod() throws IOException,
			ClassHierarchyException {
		String appPath = TestCommons.testfolder + "undetectable"
				+ Globals.pathSep + SWTAppUIErrorMain.swtJar;
		// only use methods in class Bar as entry points
		CGBuilder builder = new CGBuilder(appPath);
		builder.makeScopeAndClassHierarchy();
		Iterable<Entrypoint> entries = CGEntryManager.getAllPublicMethods(
				builder, "test.undetectable.Bar");
		builder.buildCG(entries);
		// try to detect errors from all public methods
		UIAnomalyDetector detector = new UIAnomalyDetector(appPath);
		List<AnomalyCallChain> chains = detector.detectUIAnomaly(builder);
		System.out.println("Number of anomaly call chains: " + chains.size());
		PDFViewer.viewCG("undetectable.pdf", builder.getAppCallGraph());
		assertEquals(0, chains.size(), 0);
	}

	public void testFindErrorsUsingAllPublicMethods() throws IOException, ClassHierarchyException {
		String appPath = TestCommons.testfolder + "undetectable"
				+ Globals.pathSep + SWTAppUIErrorMain.swtJar;
		// only use methods in class Bar as entry points
		CGBuilder builder = new CGBuilder(appPath);
		
		builder.makeScopeAndClassHierarchy();
		Iterable<Entrypoint> entries = CGEntryManager.getAllPublicMethods(
				builder, "test.undetectable.Bar", "test.undetectable.Foo", "test.undetectable.Bridge", "test.undetectable.IFoo",
				"test.undetectable.AbBar", "test.undetectable.UndetectableBugs");
		
		builder.setCGType(CG.RTA);
		builder.buildCG(entries);
		//PDFViewer.viewCG("undetectable.pdf", builder.getAppCallGraph());
		
		// try to detect errors from all public methods
		UIAnomalyDetector detector = new UIAnomalyDetector(appPath);
		
		//only traverse from a few entries
		Iterable<Entrypoint> barEntries = CGEntryManager.getAllPublicMethods(
				builder, "test.undetectable.Bar");
		Collection<CGNode> barEntryNodes = builder.getCallGraphEntryNodes(barEntries);
		System.out.println("The bar Entry node number: " + barEntryNodes.size());
		System.out.println("The bar entries: " + barEntryNodes);
		
		List<AnomalyCallChain> chains = detector.detectUIAnomaly(builder, barEntryNodes);
		System.out.println("Number of anomaly call chains: " + chains.size());
		
		for(AnomalyCallChain c : chains) {
			System.out.println(c.getFullCallChainAsString());
			System.out.println("---");
		}
		//PDFViewer.viewCG("undetectable.pdf", builder.getAppCallGraph());
		assertEquals(2, chains.size());
	}
	
	public void testMessageQueueSample() throws IOException, ClassHierarchyException {
		String appPath = TestCommons.testfolder + "messagequeue"
				+ Globals.pathSep + SWTAppUIErrorMain.swtJar;
		// use main method as
		CGBuilder builder = new CGBuilder(appPath);
		builder.makeScopeAndClassHierarchy();
		builder.buildCG();
		// try to detect errors from all public methods
		UIAnomalyDetector detector = new UIAnomalyDetector(appPath);
		List<AnomalyCallChain> chains = detector.detectUIAnomaly(builder);
		System.out.println("Number of anomaly call chains: " + chains.size());
		//PDFViewer.viewCG("messagequeue.pdf", builder.getAppCallGraph());
		assertEquals(1, chains.size());
		
		System.out.println("Here is the anomaly chain: ");
		System.out.println(chains.get(0).getFullCallChainAsString());
	}

}