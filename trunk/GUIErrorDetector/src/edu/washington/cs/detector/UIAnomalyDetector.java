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

import edu.washington.cs.detector.util.Files;
import edu.washington.cs.detector.util.Globals;
import edu.washington.cs.detector.util.Log;

//implements the algorithm
public class UIAnomalyDetector {
	
	private final String appPath;
	private String exclusion_file = CallGraphTestUtil.REGRESSION_EXCLUSIONS;
	
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
	
	//configure the UI anomaly detector for different purpose
	//You can configure this detector for different UI models, e.g., swing
	public void configureCheckingMethods(String configFilePath) {
		UIAnomalyMethodFinder.setCheckingMethods(configFilePath);
	}
	public void configureSafeMethods(String configFilePath) {
		UIAnomalyMethodFinder.setSafeMethods(configFilePath);
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
	
	//use all main as entries
	private CGBuilder getDefaultCGBuilder() throws IOException {
		CGBuilder builder = new CGBuilder(this.appPath, FileProvider.getFile(exclusion_file));
		builder.buildCG();
		return builder;
	}
	
	public List<AnomalyCallChain> detectUIAnomaly(CGBuilder builder) throws IOException { 
		return detectUIAnomaly(FileProvider.getFile(exclusion_file), builder, builder.getCallGraphEntryNodes());
		// builder.getCallGraph().getEntrypointNodes());
	}
	
	//users can designate specific entries nodes for detecting UI anomaly
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
	    System.out.println("Entries used in UIAnomalyDetector: " + entries.size());
	    
	    StringBuilder sb = new StringBuilder();
	    int count = 0;
	    
	    //see all the reachable thread start method
	    for(CGNode entry : entries) {
	        ThreadStartFinder finder = new ThreadStartFinder(cg, entry);
	        Collection<CallChainNode> reachableStarts = finder.getReachableThreadStarts();
	        System.out.println("removing the repetitive call chains to start.");
	        System.out.println("Number of starts: " + reachableStarts.size() + ", for entry.");
	        reachableStarts = this.removeChainRepetition(reachableStarts);
	        if(Log.isLoggingOn()) { //check Log is turned on in order to avoid overflow
	            sb.append("----reach nodes for entry point -----");
	            sb.append(Globals.lineSep);
	            sb.append(entry);
	            sb.append(Globals.lineSep);
	        }
	        for(CallChainNode threadStartNode : reachableStarts) {
	        	if(Log.isLoggingOn()) {
	        	    sb.append("   start: " + threadStartNode.node);
	        	    sb.append(Globals.lineSep);
	        	}
	        	//see its reachable UI method
	        	UIAnomalyMethodFinder detector = new UIAnomalyMethodFinder(g, nodes, threadStartNode.node);
	        	List<CallChainNode> resultNodes = detector.findUINodes();
	        	//remove repetition here
	        	resultNodes = this.removeNodeRepetition(resultNodes);
	        	for(CallChainNode resultNode : resultNodes) {
	        		AnomalyCallChain chain = new AnomalyCallChain();
	        		chain.addNodes(threadStartNode.getChainToRoot(), threadStartNode.node, resultNode.getChainToRoot());
	        		if(Log.isLoggingOn()) {
	        		    sb.append("      " + resultNode.node);
	        		    sb.append(Globals.lineSep);
	        		    sb.append("");
	        		    sb.append(Globals.lineSep);
	        		    sb.append("--- found anomaly call chain ----, num: " + (count++));
	        		    sb.append(Globals.lineSep);
	        		    sb.append(chain.getFullCallChainAsString());
	        		    sb.append(Globals.lineSep);
	        		}
	        		//add to the return result
	        		anomalyCallChains.add(chain);
	        	}
	        }
	    }
	    
	    //print out the summarization
	    sb.append("Total: " + count);
	    sb.append(Globals.lineSep);
	    System.out.println("total: " + count);
	    Log.logln(sb.toString());
	    
	    return anomalyCallChains;
	}
	
	private List<CallChainNode> removeChainRepetition(Collection<CallChainNode> nodeList) {
		List<CallChainNode> uniqueNodeList = new LinkedList<CallChainNode>();
		//keep track of unique list in the string form
		Set<String> chainStrSet = new HashSet<String>();
		
		for(CallChainNode node : nodeList) {
			String chainStr = node.getChainToRootAsStr();
			if(chainStrSet.contains(chainStr)) {
				continue;
			} else {
				uniqueNodeList.add(node);
				chainStrSet.add(chainStr);
			}
		}
		
		//write the log file
		int i = 0;
		StringBuilder sb = new StringBuilder();
		for(String chainStr : chainStrSet) {
			sb.append(i++);
			sb.append("-th chain");
			sb.append(Globals.lineSep);
			sb.append(chainStr);
			sb.append(Globals.lineSep);
			sb.append(Globals.lineSep);
		}
		try {
			Files.writeToFile(sb.toString(), "./logs/entry_to_start_chain.txt");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		System.out.println("Num before removing call chain repetition: " + nodeList.size());
		System.out.println("Num after removing call chain repetition: " + uniqueNodeList.size());
		
		return uniqueNodeList;
	}
	
	private List<CallChainNode> removeNodeRepetition(Collection<CallChainNode> nodeList) {
		List<CallChainNode> uniqueNodeList = new LinkedList<CallChainNode>();
		Set<CGNode> uniqueNodes = new HashSet<CGNode>();
		for(CallChainNode n : nodeList) {
			if(uniqueNodes.contains(n.node)) {
				continue;
			} else {
				uniqueNodeList.add(n);
				uniqueNodes.add(n.node);
			}
		}
		System.out.println("Num before removing node repetition: " + nodeList.size());
		System.out.println("Num after removing node repetition: " + uniqueNodeList.size());
		return uniqueNodeList;
	}
}