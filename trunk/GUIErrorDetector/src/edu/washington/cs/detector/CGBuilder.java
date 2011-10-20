package edu.washington.cs.detector;

import java.io.File;
import java.io.IOException;

import com.ibm.wala.core.tests.callGraph.CallGraphTestUtil;
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
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.util.config.AnalysisScopeReader;
import com.ibm.wala.util.graph.Graph;
import com.ibm.wala.util.io.FileProvider;

import edu.washington.cs.detector.util.WALAUtils;

public class CGBuilder {
	
	public final String appPath;
	public final File exclusionFile;
	
	public CGBuilder(String appPath) throws IOException {
		this.appPath = appPath;
		this.exclusionFile = FileProvider.getFile(CallGraphTestUtil.REGRESSION_EXCLUSIONS);
	}
	
	public CGBuilder(String appPath, File exclusionFile) {
		this.appPath = appPath;
		this.exclusionFile = exclusionFile;
	}

	private CallGraph callgraph = null;
	private ClassHierarchy cha = null;
	private Graph<CGNode> appCallGraph = null;
	private AnalysisScope scope = null;
	private Iterable<Entrypoint> entrypoints = null;
	
	//by default it uses all main methods are starting points
	public void buildCG() {
		try {
			// get all files for analysis
			this.makeScopeAndClassHierarchy();
			this.entrypoints = com.ibm.wala.ipa.callgraph.impl.Util
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
	
	public void buildCG(Iterable<Entrypoint> eps) {
		try {
			// get all files for analysis
			//must call makeScopeAndClassHierarchy before calling this
			if(this.scope == null || this.cha == null) {
				throw new RuntimeException("Please call makeScopeAndClassHierarchy() before calling this.");
			}
			this.entrypoints = eps;
			
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
	
	public void makeScopeAndClassHierarchy() throws IOException, ClassHierarchyException {
		this.scope = AnalysisScopeReader.makeJavaBinaryAnalysisScope(appPath, exclusionFile);
        this.cha = ClassHierarchy.make(scope);
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
	
	public AnalysisScope getAnalysisScope() {
		return scope;
	}
	
	public Iterable<Entrypoint> getEntrypoints() {
		return this.entrypoints;
	}
}