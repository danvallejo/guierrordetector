package edu.washington.cs.detector;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.util.WalaException;
import com.ibm.wala.util.debug.UnimplementedError;
import com.ibm.wala.util.graph.Graph;

import edu.washington.cs.detector.CGBuilder.CG;
import edu.washington.cs.detector.util.PDFViewer;
import edu.washington.cs.detector.util.WALAUtils;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * More precisely, test the entry points in a CG
 * */
public class TestCGBuilder extends TestCase {
	
	public static Test suite() {
		return new TestSuite(TestCGBuilder.class);
	}
	
	public void testCopyACG() throws IOException, WalaException {
		String appPath = TestCommons.testfolder + "useinterface";
		CGBuilder builder = new CGBuilder(appPath);
		builder.setCGType(CG.OneCFA);
		builder.buildCG();
		
		System.out.println(builder.getCallGraphEntryNodes());
		Graph<CGNode> appCG = builder.getAppCallGraph();
//		PDFViewer.viewCG("mainentry.pdf", appCG);
		
		//let's try to make a copy
		Graph<CGNode> copiedCG = WALAUtils.copy(appCG);
//		PDFViewer.viewCG("copiedcg.pdf", copiedCG);
		assertTrue(appCG.getNumberOfNodes() == copiedCG.getNumberOfNodes());
		
		CGNode fakeRoot = builder.getCallGraph().getFakeRootNode();
		//make some edits to the copy, then see is there any change to the orignal graph
		System.out.println("contain root: " + copiedCG.containsNode(fakeRoot));
		Iterator<CGNode> it = copiedCG.getSuccNodes(fakeRoot);
		try {
		    copiedCG.removeEdge(fakeRoot, it.next());
		    assertTrue(false);
		} catch (UnimplementedError e) {
			//ok
		}
	}
	
	public void testMainMethodAsEntryPoints() throws IOException {
		String appPath = TestCommons.testfolder + "useinterface";
		CGBuilder builder = new CGBuilder(appPath);
		builder.setCGType(CG.OneCFA);
		builder.buildCG();
		
		System.out.println(builder.getCallGraphEntryNodes());
		PDFViewer.viewCG("mainentry.pdf", builder.getAppCallGraph());
		
		assertEquals(2, builder.getCallGraphEntryNodes().size());
	}
	
	public void testAllPublicMethodsAsEntryPoints() throws IOException, ClassHierarchyException {
		String appPath = TestCommons.testfolder + "useinterface";
		CGBuilder builder = new CGBuilder(appPath);
		
		//build class hierarchy and use user-specific entrypoints
		builder.makeScopeAndClassHierarchy();
		Iterable<Entrypoint> entries = CGEntryManager.getAllPublicMethods(builder.getAnalysisScope(),
				builder.getClassHierarchy(), "test.useinterface.Bar");
		builder.setCGType(CG.OneCFA);
		builder.buildCG(entries);
		
		System.out.println(builder.getCallGraphEntryNodes());
		//PDFViewer.viewCG("allpublicentry.pdf", builder.getAppCallGraph());
		
		assertEquals(1, builder.getCallGraphEntryNodes().size());
	}
	
	public void testPartialMethodsAsEntryPoints() throws IOException, ClassHierarchyException {
		String appPath = TestCommons.testfolder + "useinterface";
		CGBuilder builder = new CGBuilder(appPath);
		
		//build class hierarchy and use user-specific entrypoints, note there are multiple classes
		builder.makeScopeAndClassHierarchy();
		Iterable<Entrypoint> entries = CGEntryManager.getAllPublicMethods(builder.getAnalysisScope(),
				builder.getClassHierarchy(), "test.useinterface.Bar", "test.useinterface.MainClass");
		builder.setCGType(CG.OneCFA);
		builder.buildCG(entries);
		
		System.out.println(builder.getCallGraphEntryNodes());
//		PDFViewer.viewCG("partialpublicentry.pdf", builder.getAppCallGraph());
		
		assertEquals(2, builder.getCallGraphEntryNodes().size());
		
		//then we are only interested in a few entry nodes
		Collection<CGNode> interestedNodes = builder.getCallGraphEntryNodes(CGEntryManager.getAllPublicMethods(builder.getAnalysisScope(),
				builder.getClassHierarchy(), "test.useinterface.Bar"));
		assertEquals(1, interestedNodes.size());
	}
}