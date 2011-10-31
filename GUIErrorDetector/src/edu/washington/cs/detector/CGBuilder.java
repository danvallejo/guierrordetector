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
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.util.config.AnalysisScopeReader;
import com.ibm.wala.util.graph.Graph;
import com.ibm.wala.util.io.FileProvider;

import edu.washington.cs.detector.util.WALAUtils;

public class CGBuilder {
	
	public enum CG {RTA, ZeroCFA, ZeroContainerCFA, ZeroOneCFA, ZeroOneContainerCFA, OneCFA, TwoCFA, CFA}
	
	public final String appPath;
	public final File exclusionFile;
	
	private CG type = CG.ZeroCFA;
	private int cfaprecision = -1;
	
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
	
	public CG getCGType() {
		return type;
	}
	
	public void setCGType(CG type) {
		assert type != null;
		this.type = type;
	}
	
	public void setCFAPrecision(int i) {
		if(i < 2) {throw new RuntimeException("Please use setCGType instead.");}
		if(type != CG.CFA) {throw new RuntimeException("Please set CG type as CFA first.");}
		this.cfaprecision = i;
	}
	
	//by default it uses all main methods are starting points
	public void buildCG() {
		try {
			// get all files for analysis
			this.makeScopeAndClassHierarchy();
			this.entrypoints = com.ibm.wala.ipa.callgraph.impl.Util
					.makeMainEntrypoints(scope, cha);
			AnalysisOptions options = new AnalysisOptions(scope, entrypoints);
			// use 0-cfa as default
			
			CallGraphBuilder builder = this.chooseCallGraphBuilder(options, new AnalysisCache(), cha, scope);
			
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
			CallGraphBuilder builder = this.chooseCallGraphBuilder(options, new AnalysisCache(), cha, scope);
			
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
	
	private CallGraphBuilder chooseCallGraphBuilder(AnalysisOptions options, AnalysisCache cache,
        IClassHierarchy cha, AnalysisScope scope) {
		CallGraphBuilder builder = null;
		if(this.type == CG.ZeroCFA) {
			System.out.println("Using 0-CFA call graph");
			builder = Util.makeZeroCFABuilder(options, cache, cha, scope);
		} else if (this.type == CG.ZeroOneCFA) {
			System.out.println("Using 0-1-CFA call graph");
			builder = Util.makeVanillaZeroOneCFABuilder(options, cache, cha, scope);
		} else if (this.type == CG.ZeroContainerCFA) {
			System.out.println("Using 0-container-CFA call graph");
			builder = Util.makeVanillaZeroOneContainerCFABuilder(options, cache, cha, scope);
		} else if (this.type == CG.RTA) {
			System.out.println("Using RTA call graph");
			builder = Util.makeRTABuilder(options, cache, cha, scope);
		} else if (this.type == CG.ZeroOneContainerCFA) {
			System.out.println("Using 0-1-container-CFA call graph");
			builder = Util.makeZeroOneContainerCFABuilder(options, cache, cha, scope);
		} else if (this.type == CG.OneCFA) {
			System.out.println("Using 1-CFA call graph");
			builder = WALAUtils.makeOneCFABuilder(options,  cache, cha, scope);
		} else if (this.type == CG.TwoCFA) {
			System.out.println("Using 2-CFA call graph");
			builder = WALAUtils.makeCFABuilder(2, options,  cache, cha, scope);
		} else if (this.type == CG.CFA) { 
			if(this.cfaprecision < 2) {
				throw new RuntimeException("Please set cfa precision first.");
			}
			System.out.println("Use CFA with precision: " + this.cfaprecision);
			builder = WALAUtils.makeCFABuilder(this.cfaprecision, options,  cache, cha, scope);
		} else {
			throw new RuntimeException("The CG type: " + type + " is unknonw");
		}
		assert builder != null;
		return builder;
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