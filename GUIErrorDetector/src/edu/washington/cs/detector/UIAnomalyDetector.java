package edu.washington.cs.detector;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import com.ibm.wala.core.tests.callGraph.CallGraphTestUtil;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.util.graph.Graph;
import com.ibm.wala.util.io.FileProvider;

import edu.washington.cs.detector.CGBuilder.CG;
import edu.washington.cs.detector.guider.CGTraverseGuider;
import edu.washington.cs.detector.util.Log;
import edu.washington.cs.detector.util.Utils;
import edu.washington.cs.detector.util.WALAUtils;

//implements the algorithm
public class UIAnomalyDetector {
	
	public static boolean DEBUG = false;
	
	public static boolean LOG_RESULT = false;
	
	public static final String EXCLUSION_FILE_SWING = "Java60RegressionExclusionsWithoutGUI.txt";
	public static final String EMPTY_FILE = "EmptyExclusion.txt";
	
	private final String appPath;
	private String exclusion_file = CallGraphTestUtil.REGRESSION_EXCLUSIONS;
	private List<FilterStrategy> filters = new LinkedList<FilterStrategy>();
	private CGTraverseGuider threadStartGuider = null;
	private CGTraverseGuider uiAnomalyGuider = null;
	private NativeMethodConnector nativeConnector = null;
	private CG cgOpt = null;
	
	public UIAnomalyDetector(String appPath) {
		assert appPath != null;
		this.appPath = appPath;
	}
	
	public String getAppPath() {
		return this.appPath;
	}
	public String getExclusionFile() {
		return exclusion_file;
	}
	public void setExclusionFile(String exclusionFile) {
		assert exclusionFile != null;
		exclusion_file = exclusionFile;
	}
	
	public void addFilterStrategy(FilterStrategy strategy) {
		filters.add(strategy);
	}
	public void addFilterStrategies(Collection<FilterStrategy> strategies) {
		filters.addAll(strategies);
	}
	public List<FilterStrategy> getFilters() {
		return this.filters;
	}
	
	//configure the UI anomaly detector for different purpose
	//You can configure this detector for different UI models, e.g., swing
	public void configureCheckingMethods(String configFilePath) {
		System.err.println("Use : " + configFilePath + " as checking file.");
		UIAnomalyMethodFinder.setCheckingMethods(configFilePath);
	}
	
	//it can be null
	public void setThreadStartGuider(CGTraverseGuider threadStartGuider) {
		this.threadStartGuider = threadStartGuider;
	}
	
	//it can be null
	public void setUIAnomalyGuider(CGTraverseGuider uiAnomalyGuider) {
		this.uiAnomalyGuider = uiAnomalyGuider;
	}
	
	//it can be null
	public void setNativeMethodConnector(NativeMethodConnector connector) {
		this.nativeConnector = connector;
	}
	
	//set the evaluator
	public void setAnomalyMethodEvaluator(MethodEvaluator evl) {
		UIAnomalyMethodFinder.setMethodEvaluator(evl);
	}
	
	//set precision of default cg builder
	public void setDefaultCGType(CG type) {
		this.cgOpt = type;
	}
	
	public CG getDefaultCGType() {
		return this.cgOpt;
	}
	
	/**
	 * Methods for detecting anomaly UI call chains. It treats every main
	 * methods as entries
	 * */
	public List<AnomalyCallChain> detectUIAnomaly() {
		try {
			//get the exclusion file
			CGBuilder defaultBuilder = this.getDefaultCGBuilder();
			//start to detect anomaly
			return this.detectUIAnomaly(defaultBuilder);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		}
	}
	
	//use all main as entries in building CG, the CG has already been builded
	public CGBuilder getDefaultCGBuilder() throws IOException {
		CGBuilder builder = new CGBuilder(this.appPath, FileProvider.getFile(exclusion_file));
		if(this.cgOpt != null) {
			builder.setCGType(cgOpt);
		}
		builder.buildCG();
		return builder;
	}
	
	public List<AnomalyCallChain> detectUIAnomaly(CGBuilder builder) throws IOException { 
		return detectUIAnomaly(FileProvider.getFile(exclusion_file), builder, builder.getCallGraphEntryNodes());
	}
	
	//users can designate specific entries nodes for detecting UI anomaly
	public List<AnomalyCallChain> detectUIAnomaly(CGBuilder builder, Collection<CGNode> entries) throws IOException { 
		return detectUIAnomaly(FileProvider.getFile(exclusion_file), builder, entries);
	}
	
	private List<AnomalyCallChain> detectUIAnomaly(File eclusionFile/*useless*/, CGBuilder builder, Collection<CGNode> entries) {
		CallGraph cg = builder.getCallGraph();
		Graph<CGNode> g = builder.getAppCallGraph();
		
		WALAUtils.logCallGraph(g, DEBUG);
		
		if(cg == null || cg == null) {
			throw new RuntimeException("please call buildCG first to construct the call graphs.");
		}
	    
		Log.logln("Number of entry nodes: " + entries.size());
		for(CGNode node : entries) {
			Log.logln("  entry: " + node);
		}
		
	    //All anomaly call chain
		List<AnomalyCallChain> anomalyCallChains = new LinkedList<AnomalyCallChain>();
	    //see all the reachable thread start method
	    for(CGNode entry : entries) {
	        ThreadStartFinder finder = ThreadStartFinder.createInstance(cg, entry, this.threadStartGuider);
	        
	        Collection<CallChainNode> reachableStarts = finder.getReachableThreadStarts();
	        reachableStarts = Utils.removeRedundantCallChains(reachableStarts);
	        
	        Log.logSeparator();
	        Log.logln("CGNode entry: " + entry);
	        Log.logln("  Number of reachable starts: " + reachableStarts.size());
	        System.out.println("-------");
	        System.out.println("Processing CGNode entry: " + entry + ",  with reachable starts: " + reachableStarts.size());
	        
	        for(CallChainNode threadStartNode : reachableStarts) {
	        	//TODO this needs
	        	AnomalyFinder anomalyFinder
	        	    = UIAnomalyMethodFinder.createInstance(builder.getClassHierarchy(), g /*change from cg*/,
	        	    		threadStartNode.getNode(), this.uiAnomalyGuider, this.nativeConnector); 
	        	
	        	List<CallChainNode> resultNodes = anomalyFinder.findThreadUnsafeUINodes();
	        	resultNodes = Utils.removeRedundantCallChains(resultNodes);
	        	
	        	Log.logln("For thread start node: " + threadStartNode.getNode());
	        	//Log.logln("Path: " + threadStartNode.getChainToRootAsStr());
	        	Log.logln("  Number of UI anomaly nodes: " + resultNodes.size());
	        	System.out.println("Number of UI anomaly nodes: " + resultNodes.size());
	        	
	        	List<AnomalyCallChain> newGeneratedChains = new LinkedList<AnomalyCallChain>(); 
	        	for(CallChainNode resultNode : resultNodes) {
	        		AnomalyCallChain chain = new AnomalyCallChain();
	        		chain.addNodes(threadStartNode.getChainToRoot(), threadStartNode.getNode(), resultNode.getChainToRoot());
	        		//anomalyCallChains.add(chain);
	        		newGeneratedChains.add(chain);
	        	}
	        	//do filtering after traversing from each thread start node
	        	System.out.println("Number of new anomaly chain: " + newGeneratedChains.size() + " before applying: "
	        			+ this.filters.size() + " filters");
	        	if(!this.filters.isEmpty()) {
	        		newGeneratedChains = CallChainFilter.filter(newGeneratedChains, this.filters);
	        	}
	        	anomalyCallChains.addAll(newGeneratedChains);
	        	System.out.println("    Number of new anomaly chain: " + newGeneratedChains.size() + " after applying: "
	        			+ this.filters.size() + " filters" + ", in total it is: " + anomalyCallChains.size());
	        	
	        }
	    }
	    
	    //filter again
	    anomalyCallChains = CallChainFilter.filter(anomalyCallChains, this.filters);
	    
	    //for logging
	    if(LOG_RESULT) {
	        this.logresult(anomalyCallChains);
	    }
	    
	    //clear the cache
	    UIAnomalyMethodFinder.clearCachedResult();
	    return anomalyCallChains;
	}
	
	private void logresult(List<AnomalyCallChain> anomalyCallChains) {
		Log.logln("Number of unique thread.start: " + UIAnomalyMethodFinder.getCachedResult().size());
	    int count = 0;
	    int numOfStart2UI = 0;
	    for(CGNode start : UIAnomalyMethodFinder.getCachedResult().keySet()) {
	    	Log.logln("The " + (count++) + "-th entry, number of call chains: "
	    			+ UIAnomalyMethodFinder.getCachedResult().get(start).size());
	    	numOfStart2UI += UIAnomalyMethodFinder.getCachedResult().get(start).size();
//	    	for(CallChainNode node : UIAnomalyMethodFinder.getCachedResult().get(start)) {
//	    		Log.logln("Call chain to UI: ");
//	    		Log.logln(node.getChainToRootAsStr());
//	    	}
	    }
	    Log.logln("Total number of call chains from start 2 UI: " + numOfStart2UI);
	    Log.logln("Total number of anomaly call chains returned: " + anomalyCallChains.size());
	    for(int i = 0; i < anomalyCallChains.size(); i++) {
	    	AnomalyCallChain chain = anomalyCallChains.get(i);
	    	Log.logln("The " + i + "-th anomaly call chain");
	    	Log.logln(chain.getFullCallChainAsString());
	    }
	}
}