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

public class UIAnomalyMethodFinder {
	
	public final Graph<CGNode> cg;
	
	//the nodes that touch UI elements
	public final Set<CGNode> uiNodes;
	
	//some methods like Display#getBounds won't touch a UI element, but call checkDevice
	//need to see the IR for more details
	private static String[] checking_methods
	    = Files.readWholeNoExp("./src/checking_methods.txt").toArray(new String[0]);
	
	//note, asyncExec just call runnable.run directly
	private static String[] safe_methods = {"org.eclipse.swt.widgets.Display.asyncExec(Ljava/lang/Runnable;)V",
			"org.eclipse.swt.widgets.Display.syncExec(Ljava/lang/Runnable;)V"};
	
	public final CGNode startNode;
	
	public UIAnomalyMethodFinder(Graph<CGNode> cg, Set<CGNode> uiNodes, CGNode startNode) {
		assert cg.containsNode(startNode);
		this.cg = cg;
		this.uiNodes = uiNodes;
		this.startNode = startNode;
	}
	
	/** reload all checking method for customization */
	public static String[] getCheckingMethods() {
		return checking_methods;
	}
	public static void setCheckingMethods(String fileName) {
		assert fileName != null;
		checking_methods = Files.readWholeNoExp(fileName).toArray(new String[0]);
	}
	public static String[] getSafeMethods() {
		return safe_methods;
	}
	public static void setSafeMethods(String fileName) {
		assert fileName != null;
		safe_methods = Files.readWholeNoExp(fileName).toArray(new String[0]);
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
	public List<CallChainNode> findUINodes() {
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
			//Skip the asyncExec / syncExec method calls
			//these two methods will call run directly
			//if(this.isSafeMethod(node)) {
			//	continue;
			//}
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
		return isElementInsideArray(methodSig, checking_methods);
	}
	
	private boolean isSafeMethod(CGNode node) {
		String methodSig = node.getMethod().getSignature();
		return isElementInsideArray(methodSig, safe_methods);
	}
	
	private boolean isElementInsideArray(String elem, String[] array) {
		for(String str : array) {
			if(str.equals(elem)) {
				return true;
			}
		}
		return false;
	}
}