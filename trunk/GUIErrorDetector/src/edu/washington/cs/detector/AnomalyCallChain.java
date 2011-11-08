package edu.washington.cs.detector;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.util.graph.Graph;

import edu.washington.cs.detector.util.Globals;

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