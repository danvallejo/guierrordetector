package edu.washington.cs.detector;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.util.graph.Graph;

public class ThreadStartFinder {

	public final Graph<CGNode> cg;

	public final CGNode startPoint;
	
	public final static String THREAD_START_SIG = "java.lang.Thread.start()V";
	
	public ThreadStartFinder(Graph<CGNode> cg, CGNode startPoint) {
		this.cg = cg;
		this.startPoint = startPoint;
		assert this.cg.containsNode(startPoint);
		assert !this.startPoint.getMethod().getSignature().equals(THREAD_START_SIG);
	}
	
	public Set<CallChainNode> getReachableThreadStarts() {
		//the set of thread start methods
		Set<CallChainNode> threadStarts = new HashSet<CallChainNode>();
		
		Map<CGNode, CallChainNode> cgNodeMap = new LinkedHashMap<CGNode, CallChainNode>();
		
		//keep track of all visited cg nodes
		Set<CGNode> visited = new HashSet<CGNode>();
		
		//do a BFS to find all reachable start
		List<CGNode> queue = new LinkedList<CGNode>();
		queue.add(startPoint);
		cgNodeMap.put(startPoint, new CallChainNode(startPoint));
		
		while(!queue.isEmpty()) {
			CGNode node = queue.remove(0);
			
			assert cgNodeMap.containsKey(node);
			CallChainNode chainNode = cgNodeMap.get(node);
			
			if(visited.contains(node)) {
				continue;
			} else {
			    visited.add(node);
			}
			//if it is a starting node
			if(this.isThreadStartNode(node)) {
				threadStarts.add(chainNode);
				continue;
			}
			//do the next level
			Iterator<CGNode> it = this.cg.getSuccNodes(node);
			while(it.hasNext()) {
				CGNode succNode = it.next();
				queue.add(succNode);
				//check if it is already added
				if(!cgNodeMap.containsKey(succNode)) {
					cgNodeMap.put(succNode, new CallChainNode(succNode, chainNode));
				}
			}
		}
		
		return threadStarts;
	}
	
	private boolean isThreadStartNode(CGNode node) {
		IMethod method = node.getMethod();
		//assume there is no override on the start method
		if(method.getSignature().trim().equals(THREAD_START_SIG)) {
			return true;
		}
		return false;
	}
}