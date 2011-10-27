package edu.washington.cs.detector.util;

import java.io.IOException;


import com.ibm.wala.core.tests.callGraph.CallGraphTestUtil;
import com.ibm.wala.ipa.callgraph.AnalysisCache;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.CallGraphBuilder;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.util.CancelException;
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
	
	public void testContextSensitiveCG1() throws IllegalArgumentException, IOException, CancelException, WalaException {
		this.buildContextSensitiveCG(1);
	}
	
	public void testContextSensitiveCG2() throws IllegalArgumentException, IOException, CancelException, WalaException {
		this.buildContextSensitiveCG(2);
	}

	public void buildContextSensitiveCG(int level) throws IOException,
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
		CallGraphBuilder builder = WALAUtils.makeCFABuilder(level, options, new AnalysisCache(), cha, scope);
		CallGraph cg = builder.makeCallGraph(options, null);
		Graph<CGNode> appGraph = WALAUtils.pruneForAppLoader(cg);
		PDFViewer.viewCG(level + "-cfa", appGraph);
		
		//i want to see the context
		for(CGNode node : appGraph) {
			System.out.println(node);
//			System.out.println("   node number: ");
			System.out.println("   its context: " + node.getContext());
		}
	}
}
