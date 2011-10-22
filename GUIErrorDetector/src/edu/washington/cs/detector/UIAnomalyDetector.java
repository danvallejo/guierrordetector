package edu.washington.cs.detector;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.ibm.wala.core.tests.callGraph.CallGraphTestUtil;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.util.graph.Graph;
import com.ibm.wala.util.io.FileProvider;

import edu.washington.cs.detector.util.Globals;
import edu.washington.cs.detector.util.Log;

//implements the algorithm
public class UIAnomalyDetector {
	
	public static String exclusion_file = CallGraphTestUtil.REGRESSION_EXCLUSIONS;
	
	public final String appPath;
	
	public UIAnomalyDetector(String appPath) {
		assert appPath != null;
		this.appPath = appPath;
	}
	
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
	
	//use all main as entries
	private CGBuilder getDefaultCGBuilder() throws IOException {
		CGBuilder builder = new CGBuilder(this.appPath, FileProvider.getFile(exclusion_file));
		builder.buildCG();
		return builder;
	}
	
	public List<AnomalyCallChain> detectUIAnomaly(CGBuilder builder) throws IOException { 
		return detectUIAnomaly(FileProvider.getFile(exclusion_file), builder, builder.getCallGraph().getEntrypointNodes());
	}
	
	public List<AnomalyCallChain> detectUIAnomaly(CGBuilder builder, Collection<CGNode> entries) throws IOException { 
		return detectUIAnomaly(FileProvider.getFile(exclusion_file), builder, entries);
	}
	
	private List<AnomalyCallChain> detectUIAnomaly(File eclusionFile, CGBuilder builder, Collection<CGNode> entries) {
		//All anomaly call chain
		List<AnomalyCallChain> anomalyCallChains = new LinkedList<AnomalyCallChain>();
		
		ClassHierarchy cha = builder.getClassHierarchy();
		CallGraph cg = builder.getCallGraph();
		Graph<CGNode> g = builder.getAppCallGraph();
		
		if(cg == null || g == null) {
			throw new RuntimeException("please call buildCG first to construct the call graphs.");
		}
	    
	    //find all methods that may touch UI elements
	    UIMethodSummarizer uisummarizer = new UIMethodSummarizer(g, cha);
	    Set<CGNode> nodes = uisummarizer.getUINodes();
	    
	    System.out.println("After pruning all non-app classes: ");
	    System.out.println("  node num: " + g.getNumberOfNodes());
	    System.out.println("  node touches UI: " + nodes.size());
	    
	    StringBuilder sb = new StringBuilder();
	    
	    System.out.println("Entries used in UIAnomalyDetector: " + entries.size());
	    
	    int count = 0;
	    //see all the reachable thread start method
	    for(CGNode entry : entries) {
	        ThreadStartFinder finder = new ThreadStartFinder(cg, entry);
	        Set<CallChainNode> reachableStarts = finder.getReachableThreadStarts();
	        sb.append("----reach nodes for entry point -----");
	        sb.append(Globals.lineSep);
	        sb.append(entry);
	        sb.append(Globals.lineSep);
	        for(CallChainNode chainNode : reachableStarts) {
	        	sb.append("   start: " + chainNode.node);
	        	sb.append(Globals.lineSep);
	        	//see its reachable UI method
	        	UIAnomalyMethodFinder detector = new UIAnomalyMethodFinder(g, nodes, chainNode.node);
	        	List<CallChainNode> resultNodes = detector.findUINodes();
	        	//remove repetition here
	        	resultNodes = this.removeRepetition(resultNodes);
	        	for(CallChainNode resultNode : resultNodes) {
	        		AnomalyCallChain chain = new AnomalyCallChain();
	        		chain.addCGNodes(chainNode.getChainToRoot());
	        		chain.addCGNodes(resultNode.getChainToRoot());
	        		sb.append("      " + resultNode.node);
	        		sb.append(Globals.lineSep);
	        		sb.append("");
	        		sb.append(Globals.lineSep);
	        		sb.append("--- found anomaly call chain ----, num: " + (count++));
	        		sb.append(Globals.lineSep);
	        		sb.append(chain.getFullCallChainAsString());
	        		sb.append(Globals.lineSep);
	        		//add to the return result
	        		anomalyCallChains.add(chain);
	        	}
	        }
	    }
	    sb.append("Total: " + count);
	    sb.append(Globals.lineSep);
	    System.out.println("total: " + count);
	    Log.logln(sb.toString());
	    
	    return anomalyCallChains;
	}
	
	private List<CallChainNode> removeRepetition(List<CallChainNode> nodeList) {
		List<CallChainNode> uniqueList = new LinkedList<CallChainNode>();
		Set<CGNode> uniqueNodes = new HashSet<CGNode>();
		for(CallChainNode n : nodeList) {
			if(uniqueNodes.contains(n.node)) {
				continue;
			} else {
				uniqueList.add(n);
				uniqueNodes.add(n.node);
			}
		}
		System.out.println("Num before removing repetition: " + nodeList.size());
		System.out.println("Num after removing repetition: " + uniqueList.size());
		return uniqueList;
	}
}