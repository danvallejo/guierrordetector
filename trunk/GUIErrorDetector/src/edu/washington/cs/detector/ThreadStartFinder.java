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

import edu.washington.cs.detector.util.Log;

public class ThreadStartFinder {

	public final Graph<CGNode> cg;

	public final CGNode startPoint;
	
	public final static String THREAD_START_SIG = "java.lang.Thread.start()V";
	
	private CGTraverseGuider guider = new CGTraverseDefaultGuider();
	
	public ThreadStartFinder(Graph<CGNode> cg, CGNode startPoint) {
		this.cg = cg;
		this.startPoint = startPoint;
		assert this.cg.containsNode(startPoint);
		assert !this.startPoint.getMethod().getSignature().equals(THREAD_START_SIG);
	}
	
	public void setCGTraverseGuider(CGTraverseGuider guider) {
		assert guider != null;
		this.guider = guider;
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
			//System.out.println("visited node: ");
			//System.out.println(visited);
			CGNode node = queue.remove(0);
			
			assert cgNodeMap.containsKey(node);
			CallChainNode chainNode = cgNodeMap.get(node);
			
			//if it is a starting node
			if(isThreadStartNode(node)) {
				threadStarts.add(chainNode);
				continue;
			}
			
			if(visited.contains(node)) {
				continue;
			} else {
			    visited.add(node);
			}

			//do the next level
			Iterator<CGNode> it = this.cg.getSuccNodes(node);
			while(it.hasNext()) {
				CGNode succNode = it.next();
				
				//should traverse or not
				if(!this.guider.traverse(node, succNode)) {
					//Log.logln("Skip traversing: " + succNode);
					continue;
				}
				
				//keep traversing
				queue.add(succNode);
				//check if it is already added
				//It should be removed, other wise it will miss a few paths
				// (1)  a() ->b() -> start()
				// (2)  a() ->c() -> start()
				//the start() method should correspond to two different callchainnode objects
				//if(!cgNodeMap.containsKey(succNode)) {
					cgNodeMap.put(succNode, new CallChainNode(succNode, chainNode));
				//}
			}
		}
		
		return threadStarts;
	}
	
	public static boolean isThreadStartNode(CGNode node) {
		IMethod method = node.getMethod();
		//assume there is no override on the start method
		if(method.getSignature().trim().equals(THREAD_START_SIG)) {
			return true;
		}
		return false;
	}
}