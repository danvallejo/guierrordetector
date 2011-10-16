package edu.washington.cs.detector;

import java.io.File;

import com.ibm.wala.ipa.callgraph.AnalysisCache;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.CallGraphBuilder;
import com.ibm.wala.ipa.callgraph.CallGraphStats;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.callgraph.impl.Util;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.util.config.AnalysisScopeReader;
import com.ibm.wala.util.graph.Graph;

import edu.washington.cs.detector.util.WALAUtils;

public class CGBuilder {
	
	public final String appPath;
	public final File exclusionFile;
	
	public CGBuilder(String appPath, File exclusionFile) {
		this.appPath = appPath;
		this.exclusionFile = exclusionFile;
	}

	private CallGraph callgraph = null;
	private ClassHierarchy cha = null;
	private Graph<CGNode> appCallGraph = null;
	
	public void buildCG() {
		try {
			// get all files for analysis
			AnalysisScope scope = AnalysisScopeReader
					.makeJavaBinaryAnalysisScope(appPath, exclusionFile);
			this.cha = ClassHierarchy.make(scope);
			// treat all main methods as entry points for call graph
			// XXX need to weak
			Iterable<Entrypoint> entrypoints = com.ibm.wala.ipa.callgraph.impl.Util
					.makeMainEntrypoints(scope, cha);
			AnalysisOptions options = new AnalysisOptions(scope, entrypoints);
			// use 0-cfa as default
			CallGraphBuilder builder = Util.makeZeroCFABuilder(options,
					new AnalysisCache(), cha, scope);
			this.callgraph = builder.makeCallGraph(options, null);

			System.err.println(CallGraphStats.getStats(this.callgraph));
			// only remain the classes loaded by app
			this.appCallGraph = WALAUtils.pruneForAppLoader(this.callgraph);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public CallGraph getCallGraph() {
		return callgraph;
	}
	
	public ClassHierarchy getClassHierarchy() {
		return cha;
	}
	
	public Graph<CGNode> getAppCallGraph() {
		return appCallGraph;
	}
	
}