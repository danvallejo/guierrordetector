package edu.washington.cs.detector;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.util.graph.Graph;

import edu.washington.cs.detector.util.Files;
import edu.washington.cs.detector.util.Utils;

public class UIAnomalyMethodFinder extends AbstractMethodFinder {
	
	//some methods like Display#getBounds won't touch a UI element, but call checkDevice
	//need to see the IR for more details. so we check if a thread.start method can
	//reach a few thread safety checking methods
	private static String[] checking_methods
	    = Files.readWholeNoExp("./src/checking_methods.txt").toArray(new String[0]);

	public UIAnomalyMethodFinder(Graph<CGNode> cg, CGNode startNode) {
		super(cg, startNode);
	}
	
	/** reload all checking method for customization */
	public static String[] getCheckingMethods() {
		return checking_methods;
	}
	public static void setCheckingMethods(String fileName) {
		assert fileName != null;
		checking_methods = Files.readWholeNoExp(fileName).toArray(new String[0]);
	}
	
	/** Use BFS to find all UI nodes that are reachable from {@link startNode}}
	 * NOTE this method does not need to be a recursive one. since the async/syncExec method
	 * will not fork a new thread, they just call the run method directly.
	 * 
	 * FIXME
	 * Some tweaks are needed to avoid the algorithm to miss a path. Suppose there
	 * are two paths:
	 * (1) a() -> b() -> c() -> d()
	 * (2) e() -> b() -> f()
	 * 
	 * If path (1), which may be invalid is visited first, then path (2) will not be
	 * visited anymore.
	 * */
	public List<CallChainNode> findThreadUnsafeUINodes() {
		List<CallChainNode> reachableUINodes = new LinkedList<CallChainNode>();
		
		//a map keep track of CGNode and is wrapped CallChainNode
		Map<CGNode, CallChainNode> cgNodeMap = new LinkedHashMap<CGNode, CallChainNode>();
		//a queue to keep the fringe
		List<CGNode> queue = new LinkedList<CGNode>();
		//a set to keep visited nodes in BFS
		Set<CGNode> visitedNodes = new HashSet<CGNode>();
		
		//visit the succ nodes of the start node
		Iterator<CGNode> nodeIt = this.cg.getSuccNodes(this.startNode);
		while(nodeIt.hasNext()) {
			CGNode node = nodeIt.next();
			queue.add(node);
			//add to the cg node map
			assert !cgNodeMap.containsKey(node);
			cgNodeMap.put(node, new CallChainNode(node, this.startNode));
		}
		//peform BFS search here
		while(!queue.isEmpty()) {
			CGNode node = queue.remove(0);
			assert cgNodeMap.containsKey(node);
			CallChainNode chainNode = cgNodeMap.get(node);
			if(this.isCheckingMethod(node)) {
				reachableUINodes.add(chainNode);
			}
			//skip if already visited, otherwise, add to the visited set
			if(visitedNodes.contains(node)) {
				continue;
			} else {
			    visitedNodes.add(node);
			}
			
			//add the succ nodes to the queue and continue to traverse
			Iterator<CGNode> succIt = this.cg.getSuccNodes(node);
			while(succIt.hasNext()) {
				CGNode succNode = succIt.next();
				queue.add(succNode);
				if(!cgNodeMap.containsKey(succNode)) {
					cgNodeMap.put(succNode, new CallChainNode(succNode, chainNode));
				}
			}
		}
		
		return reachableUINodes;
	}
	
	private boolean isCheckingMethod(CGNode node) {
		String methodSig = node.getMethod().getSignature();
		return Utils.<String>includedIn(methodSig, checking_methods);
	}
}