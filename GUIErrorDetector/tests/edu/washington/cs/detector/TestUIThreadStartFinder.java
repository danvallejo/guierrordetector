package edu.washington.cs.detector;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import com.ibm.wala.core.tests.callGraph.CallGraphTestUtil;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.util.io.FileProvider;

import edu.washington.cs.detector.util.Globals;
import edu.washington.cs.detector.util.Log;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class TestUIThreadStartFinder extends TestCase {
	
	public static Test suite() {
		return new TestSuite(TestUIThreadStartFinder.class);
	}

	public void testMultiPathToStart() throws IOException {
		String appPath = TestCommons.testfolder + "multipaths";
		
		CGBuilder builder = new CGBuilder(appPath, FileProvider.getFile(CallGraphTestUtil.REGRESSION_EXCLUSIONS));
		builder.buildCG();
		
		CallGraph cg = builder.getCallGraph();
		
		Collection<CGNode> entries = builder.getCallGraphEntryNodes();
		assertEquals(1, entries.size());
		
		//PDFViewer.viewCG("multipaths.pdf", builder.getAppCallGraph());
		for(CGNode entry : entries) {
		    ThreadStartFinder finder = new ThreadStartFinder(cg, entry);
		    Collection<CallChainNode> reachableStarts = finder.getReachableThreadStarts();
		    System.out.println("Number of reachabl starts: " + reachableStarts.size());
		    System.out.println("See the call chains: ");
		    for(CallChainNode node : reachableStarts) {
		    	System.out.println(node.getChainToRootAsStr());
		    	System.out.println("------------------------");
		    }
		    break;
		}
	}
	
	public void testMultiChecks() throws IOException {
        String appPath = TestCommons.testfolder + "multichecks" + Globals.pathSep
            + SWTAppUIErrorMain.swtJar;;
		
		CGBuilder builder = new CGBuilder(appPath, FileProvider.getFile(CallGraphTestUtil.REGRESSION_EXCLUSIONS));
		builder.buildCG();
		
		CallGraph cg = builder.getCallGraph();
		
		Collection<CGNode> entries = builder.getCallGraphEntryNodes();
		assertEquals(1, entries.size());
		
		//PDFViewer.viewCG("multipaths.pdf", builder.getAppCallGraph());
		for(CGNode entry : entries) {
		    ThreadStartFinder finder = new ThreadStartFinder(cg, entry);
		    Collection<CallChainNode> reachableStarts = finder.getReachableThreadStarts();
		    System.out.println("Number of reachabl starts: " + reachableStarts.size());
		    System.out.println("See the call chains: ");
		    for(CallChainNode node : reachableStarts) {
		    	System.out.println(node.getChainToRootAsStr());
		    	System.out.println("------------------------");
		    }
		    break;
		}
		
		//see how many anomaly paths to find
		UIAnomalyDetector detector = new UIAnomalyDetector(appPath);
		List<AnomalyCallChain> chains = detector.detectUIAnomaly(builder);
		System.out.println("Number of anomaly call chains: " + chains.size());
		
		assertEquals(2, chains.size());
	}
}
