package edu.washington.cs.detector;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.util.graph.Graph;

import edu.washington.cs.detector.util.Globals;
import edu.washington.cs.detector.util.Log;
import edu.washington.cs.detector.util.WALAUtils;

public class AnomalyCallChain {
	
	private List<CGNode> ui2start = new LinkedList<CGNode>();
	private CGNode threadStart = null;
	private List<CGNode> start2check = new LinkedList<CGNode>();
	
	public CGNode getThreadStartNode() {
		return this.threadStart;
	}
	
	public void addNodes(Collection<CGNode> ui2Start, CGNode threadStart, Collection<CGNode> start2Check) {
		assert threadStart != null;
		this.threadStart = threadStart;
		this.ui2start.addAll(ui2Start);
		this.start2check.addAll(start2Check);
	}
	
	public List<CGNode> getUI2Start() {
		return this.ui2start;
	}
	
	public List<CGNode> getStart2Check() {
		return this.start2check;
	}
	
	public CGNode getEntryNode() {
		if(this.ui2start.size() < 2) {
			throw new RuntimeException("The size of ui2start should >= 2");
		}
		return this.ui2start.get(0);
	}
	
	public int size() {
		return this.getFullCallChain().size();
	}
	
	/**
	 * Return all nodes from UI method to the checking method
	 * */
	public List<CGNode> getFullCallChain() {
		final List<CGNode> nodes = new LinkedList<CGNode>();
		nodes.addAll(ui2start);
		nodes.remove(threadStart); //remove double count of thread start
		nodes.addAll(start2check);
		return nodes;
	}
	
	public String getFullCallChainAsString() {
		StringBuilder sb = new StringBuilder();
		int count = 0;
		for(CGNode node : getFullCallChain()) {
            if(count != 0) {
			   sb.append(" -> ");	
			}
            count++;
			sb.append(node); //print method?, not the node; avoiding context info
			sb.append(", line: ");
			sb.append(node.getMethod().getLineNumber(0));
			sb.append(Globals.lineSep);
		}
		return sb.toString();
	}
	
	public static String flatCGNodeList(List<CGNode> nodeList) {
		assert nodeList != null;
		StringBuilder sb = new StringBuilder();
		for(CGNode node : nodeList) {
			sb.append(node);
			sb.append(Globals.lineSep);
		}
		return sb.toString();
	}
	
	/**
	 * A utility method to check whether there is a valid call chain from the
	 * startNode to the endNode. return the path, else return an empty list.
	 * */
	public static List<CGNode> extractPathByStartEnd(Graph<CGNode> cg, CGNode startNode,
			CGNode endNode, String excludedPackageName) {
		if(startNode.equals(endNode)) {
			throw new RuntimeException("Start node: " + startNode + " should not equal to endNode.");
		}
		if(startNode == null || endNode == null) {
			throw new RuntimeException("Start or end node can not be null.");
		}
		
		//the path to return
		List<CGNode> nodesInPath = new LinkedList<CGNode>();
		
		Map<CGNode, CallChainNode> cgNodeMap = new LinkedHashMap<CGNode, CallChainNode>();
		List<CGNode> queue = new LinkedList<CGNode>();
		Set<CGNode> visitedNodes = new HashSet<CGNode>();
		
		Iterator<CGNode> nodeIt = cg.getSuccNodes(startNode);
		while(nodeIt.hasNext()) {
			CGNode node = nodeIt.next();
			queue.add(node);
			if(!cgNodeMap.containsKey(node)) {
			    cgNodeMap.put(node, new CallChainNode(node, startNode));
			}
		}
		//perform BFS search here
		while(!queue.isEmpty()) {
			CGNode node = queue.remove(0);
			assert cgNodeMap.containsKey(node);
			CallChainNode chainNode = cgNodeMap.get(node);
			
			if(node.equals(endNode)) {
				CallChainNode backTrackNode = chainNode;
				nodesInPath.add(0, backTrackNode.getNode());
				while(backTrackNode.getParent() != null) {
					backTrackNode = backTrackNode.getParent();
					nodesInPath.add(0, backTrackNode.getNode());
				}
				break;
			}
			
			//skip if already visited, otherwise, add to the visited set
			if(visitedNodes.contains(node)) {
				continue;
			} else {
				if(excludedPackageName != null && WALAUtils.getJavaPackageName(node.getMethod().getDeclaringClass()).startsWith(excludedPackageName)) {
					continue;
				}
			    visitedNodes.add(node);
			}
			//add the succ nodes to the queue and continue to traverse
			Iterator<CGNode> succIt = cg.getSuccNodes(node);
			while(succIt.hasNext()) {
				CGNode succNode = succIt.next();
				queue.add(succNode);
				if(!cgNodeMap.containsKey(succNode)) {
					cgNodeMap.put(succNode, new CallChainNode(succNode, chainNode));
				}
			}
		}
		
		return nodesInPath;
	}
	
	/**
	 * It is a utility method to check whether there is a valid call chain from
	 * the startNode, then following strictly the given node signatures. Suppose
	 * the startNode is "s", and nodeSigns = {a, b, c, d}. So this method will
	 * return a call chain if there is a path s -> a -> b -> c -> d.
	 * Otherwise, it returns null
	 * */
	public static AnomalyCallChain extractCallChainNodeByNode(Graph<CGNode> cg, CGNode startNode, 
			String[] nodeSigs) {
		
		List<CGNode> nodesInPath = new LinkedList<CGNode>();
		nodesInPath.add(startNode);
		
		CGNode currentNode = startNode;
		for(String sig : nodeSigs) {
		    Iterator<CGNode> it = cg.getSuccNodes(currentNode);
		    //a list to keep all next nodes
		    List<CGNode> foundNextNodes = new LinkedList<CGNode>();
		    while(it.hasNext()) {
		    	CGNode next = it.next();
		    	if(next.toString().indexOf(sig) != -1) {
		    		foundNextNodes.add(next);
		    	}
		    }
		    //return if no nodes
		    if(foundNextNodes.isEmpty()) {
		    	//return null;
		    	throw new RuntimeException("No nodes found: " + sig + ", after current node: "
		    			+ currentNode);
		    }
		    //check the next nodes
		    if(foundNextNodes.size() > 1) {
		    	throw new RuntimeException("There should be at most 1 matched node for: "
		    			+ sig + ". In fact, it has: " + foundNextNodes.size() + ", namely: "
		    			+ foundNextNodes);
		    }
		    currentNode = foundNextNodes.get(0);
		    nodesInPath.add(currentNode);
		}
		
		//construct an anomaly call chain from a list of nodes
		int startIndex = -1;
		for(int i = 0; i < nodesInPath.size(); i++) {
			CGNode node = nodesInPath.get(i);
			if(node.getMethod().getSignature().equals(ThreadStartFinder.THREAD_START_SIG)) {
				startIndex = i;
				break;
			}
		}
		if(startIndex == -1) {
			throw new RuntimeException("No thread start method.");
		}
		
		AnomalyCallChain chain = new AnomalyCallChain();
		chain.addNodes(nodesInPath.subList(0, startIndex), nodesInPath.get(startIndex),
				nodesInPath.subList(startIndex + 1, nodesInPath.size()));
		
		return chain;
	}
}