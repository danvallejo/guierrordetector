package edu.washington.cs.detector.util;

import java.io.IOException;
import java.util.Iterator;

import org.eclipse.core.runtime.NullProgressMonitor;

import com.ibm.wala.core.tests.callGraph.CallGraphTestUtil;
import com.ibm.wala.core.tests.util.TestConstants;
import com.ibm.wala.ipa.callgraph.AnalysisCache;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.CallGraphBuilder;
import com.ibm.wala.ipa.callgraph.CallGraphBuilderCancelException;
import com.ibm.wala.ipa.callgraph.ContextSelector;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.callgraph.impl.Util;
import com.ibm.wala.ipa.callgraph.propagation.CloneContextSelector;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.ipa.callgraph.propagation.PointerKey;
import com.ibm.wala.ipa.callgraph.propagation.TargetMethodContextSelector;
import com.ibm.wala.ipa.callgraph.propagation.cfa.CallStringContextSelector;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.types.Selector;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.ProgressMonitorDelegate;
import com.ibm.wala.util.WalaException;
import com.ibm.wala.util.config.AnalysisScopeReader;
import com.ibm.wala.util.graph.Graph;
import com.ibm.wala.util.io.FileProvider;

import edu.washington.cs.detector.TestCommons;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class TestWalaCGContextSensitivity extends TestCase {
	
	public static Test suite() {
		return new TestSuite(TestWalaCGContextSensitivity.class);
	}

	public void testContextSensitiveCG() throws IOException, IllegalArgumentException, WalaException, CancelException {
		String appPath =  TestCommons.testfolder + "sensitivecg";
		
		AnalysisScope scope = AnalysisScopeReader.makeJavaBinaryAnalysisScope(appPath, FileProvider.getFile(CallGraphTestUtil.REGRESSION_EXCLUSIONS));
		IClassHierarchy cha = ClassHierarchy.make(scope);
		
		Iterable<Entrypoint> entrypoints = com.ibm.wala.ipa.callgraph.impl.Util.makeMainEntrypoints(scope, cha);
		AnalysisOptions options = new AnalysisOptions(scope, entrypoints);
		
		CallGraphBuilder builder = Util.makeZeroOneContainerCFABuilder(options, new AnalysisCache(), cha, scope);
	    CallGraph cg = builder.makeCallGraph(options, null);
	    
	    PointerAnalysis pa = builder.getPointerAnalysis();
	    System.out.println("---- pointer key ----");
	    for(PointerKey key : pa.getPointerKeys()) {
	    	System.out.println(" " + key);
	    }
	    System.out.println("---- insance key ----");
	    for(InstanceKey key : pa.getInstanceKeys()) {
	    	System.out.println(" " + key);
	    }
	    
	    //no context info
	    Iterator<CGNode> it = cg.iterator();
	    while(it.hasNext()) {
	    	System.out.println(it.next().getContext());
	    }
	    
	    Graph<CGNode> appGraph = WALAUtils.pruneForAppLoader(cg);
	    //System.out.println(pa.getInstanceKeys());
	    PDFViewer.viewCG("cscg.pdf", appGraph);
	}

	public void testContextSensitiveCG2() throws IOException,
			IllegalArgumentException, CancelException, WalaException {
		//all configurations
		String appPath = TestCommons.testfolder + "sensitivecg";
		AnalysisScope scope = AnalysisScopeReader.makeJavaBinaryAnalysisScope(appPath,
				FileProvider.getFile(CallGraphTestUtil.REGRESSION_EXCLUSIONS));
		ClassHierarchy cha = ClassHierarchy.make(scope);
		Iterable<Entrypoint> entrypoints = com.ibm.wala.ipa.callgraph.impl.Util
				.makeMainEntrypoints(scope, cha);
		AnalysisOptions options = CallGraphTestUtil.makeAnalysisOptions(scope,
				entrypoints);
		//set up the call graph builder
		CallGraphBuilder builder = WALAUtils.makeOneCFABuilder(options, new AnalysisCache(), cha, scope);
		CallGraph cg = builder.makeCallGraph(options, null);
		Graph<CGNode> appGraph = WALAUtils.pruneForAppLoader(cg);
		PDFViewer.viewCG("1-cfa", appGraph);
	}
}
