package edu.washington.cs.detector.experimental;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.ibm.wala.core.tests.callGraph.CallGraphTestUtil;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.util.graph.Graph;
import com.ibm.wala.util.io.FileProvider;

import edu.washington.cs.detector.AnomalyCallChain;
import edu.washington.cs.detector.AnomalyFinder;
import edu.washington.cs.detector.CGBuilder;
import edu.washington.cs.detector.CGBuilder.CG;
import edu.washington.cs.detector.CallChainFilter;
import edu.washington.cs.detector.CallChainNode;
import edu.washington.cs.detector.FilterStrategy;
import edu.washington.cs.detector.MethodEvaluator;
import edu.washington.cs.detector.NativeMethodConnector;
import edu.washington.cs.detector.ThreadStartFinder;
import edu.washington.cs.detector.guider.CGTraverseGuider;
import edu.washington.cs.detector.util.Log;
import edu.washington.cs.detector.util.Utils;
import edu.washington.cs.detector.util.WALAUtils;

//implements the algorithm
public class InvalidThreadAccessDetector {
	
	public static boolean DEBUG = false;
	
	public static boolean LOG_RESULT = false;
	
	public static final String EXCLUSION_FILE_SWING = "Java60RegressionExclusionsWithoutGUI.txt";
	public static final String EMPTY_FILE = "EmptyExclusion.txt";
		
	public static void setToUseDFS() {
		ThreadStartFinder.USE_DEF = true;
		UIAccessingMethodFinder.USE_DEF = true;
	}
	
	private final String appPath;
	private String exclusion_file = CallGraphTestUtil.REGRESSION_EXCLUSIONS;
	private List<FilterStrategy> filters = new LinkedList<FilterStrategy>();
	private CGTraverseGuider threadStartGuider = null;
	private CGTraverseGuider uiAnomalyGuider = null;
	private NativeMethodConnector nativeConnector = null;
	private CG cgOpt = null;
	
	public InvalidThreadAccessDetector(String appPath) {
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
		UIAccessingMethodFinder.setCheckingMethods(configFilePath);
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
		UIAccessingMethodFinder.setMethodEvaluator(evl);
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
		//keep mapping between reachable starts and nodes
		Map<CGNode, Collection<CallChainNode>> startCGNodeMap = new LinkedHashMap<CGNode, Collection<CallChainNode>>(); 
		//first get all reachable starts from all entry nodes
		Collection<CGNode> allReachableStarts = new LinkedHashSet<CGNode>();
		for(CGNode entry : entries) {
			ThreadStartFinder finder = ThreadStartFinder.createInstance(cg, entry, this.threadStartGuider);
			Collection<CallChainNode> reachablStarts = finder.getReachableThreadStarts();
			for(CallChainNode ccn : reachablStarts) {
				CGNode startNode = ccn.getNode();
				allReachableStarts.add(startNode);
				//save a copy of the reachable starts
				if(!startCGNodeMap.containsKey(startNode)) {
					startCGNodeMap.put(startNode, new LinkedHashSet<CallChainNode>());
				}
				startCGNodeMap.get(startNode).add(ccn);
			}
		}
		Log.logln("All reachable starts: " + allReachableStarts);
		Log.logln("Total number: "  + allReachableStarts.size());
		//find the UIAccessing methods
		UIAccessingMethodFinder anomalyFinder = UIAccessingMethodFinder.createInstance(builder.getClassHierarchy(), g /*change from cg*/,
				allReachableStarts, this.uiAnomalyGuider, this.nativeConnector); 
		
		List<CallChainNode> uiAccessNodes = anomalyFinder.findThreadUnsafeUINodes();
        List<AnomalyCallChain> newGeneratedChains = new LinkedList<AnomalyCallChain>(); 
    	for(CallChainNode uiAccess : uiAccessNodes) {
    		CGNode threadStart = uiAccess.getChainToRoot().get(0);
    		if(!ThreadStartFinder.isThreadStartNode(threadStart)) {
    			throw new RuntimeException("Not a thread start() : " + threadStart);
    		}
    		if(!startCGNodeMap.containsKey(threadStart)) {
    			throw new RuntimeException("The CG start map does not contain: " + threadStart);
    		}
    		Collection<CallChainNode> threadStartCCNodes = startCGNodeMap.get(threadStart);
    		for(CallChainNode threadStartCCNode : threadStartCCNodes) {
    			AnomalyCallChain chain = new AnomalyCallChain();
    			chain.addNodes(threadStartCCNode.getChainToRoot(), threadStart, uiAccess.getChainToRoot());
    			newGeneratedChains.add(chain);
    		}
    	}
    	//do filtering after traversing from each thread start node
    	System.out.println("Number of new anomaly chain: " + newGeneratedChains.size() + " before applying: "
    			+ this.filters.size() + " filters");
    	anomalyCallChains.addAll(newGeneratedChains);
    	if(!this.filters.isEmpty()) {
    		anomalyCallChains = CallChainFilter.filter(anomalyCallChains, this.filters);
    	}
    	System.out.println("    Number of new anomaly chain: " + newGeneratedChains.size() + " after applying: "
    			+ this.filters.size() + " filters" + ", in total it is: " + anomalyCallChains.size());
    	
	    
	    //for logging
	    if(LOG_RESULT) {
	        this.logresult(anomalyCallChains);
	    }
	    
	    //clear the cache
	    UIAccessingMethodFinder.clearCachedResult();
	    return anomalyCallChains;
	}
	
	private void logresult(List<AnomalyCallChain> anomalyCallChains) {
		Log.logln("Number of unique thread.start: " + UIAccessingMethodFinder.getCachedResult().size());
	    int count = 0;
	    int numOfStart2UI = 0;
	    for(CGNode start : UIAccessingMethodFinder.getCachedResult().keySet()) {
	    	Log.logln("The " + (count++) + "-th entry, number of call chains: "
	    			+ UIAccessingMethodFinder.getCachedResult().get(start).size());
	    	numOfStart2UI += UIAccessingMethodFinder.getCachedResult().get(start).size();
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