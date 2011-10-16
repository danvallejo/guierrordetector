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

public class UIAnomalyMethodFinder {
	
	public final Graph<CGNode> cg;
	
	//the nodes that touch UI elements
	public final Set<CGNode> uiNodes;
	
	//some methods like Display#getBounds won't touch a UI element, but call checkDevice
	//need to see the IR for more details
	public final String[] checking_methods = {"org.eclipse.swt.widgets.Widget.checkWidget()V"
			,"org.eclipse.swt.widgets.Display.checkDevice()V"
			};
	
	//note, asyncExec just call runnable.run directly
	public final String[] safe_methods = {"org.eclipse.swt.widgets.Display.asyncExec(Ljava/lang/Runnable;)V",
			"org.eclipse.swt.widgets.Display.syncExec(Ljava/lang/Runnable;)V"};
	
	public final CGNode startNode;
	
	public UIAnomalyMethodFinder(Graph<CGNode> cg, Set<CGNode> uiNodes, CGNode startNode) {
		assert cg.containsNode(startNode);
		this.cg = cg;
		this.uiNodes = uiNodes;
		this.startNode = startNode;
	}
	
	/** Use BFS to find all UI nodes that are reachable from {@link startNode}}
	 * TODO this method should be a recursive one
	 * */
	public List<CallChainNode> findUINodes() {
		List<CallChainNode> reachableUINodes = new LinkedList<CallChainNode>();
		
		//a map keep track of CGNode and is wrapped CallChainNode
		Map<CGNode, CallChainNode> cgNodeMap = new LinkedHashMap<CGNode, CallChainNode>();
		
		//a queue and visited node set for BFS
		List<CGNode> queue = new LinkedList<CGNode>();
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
		//do BFS here
		while(!queue.isEmpty()) {
			CGNode node = queue.remove(0);
			assert cgNodeMap.containsKey(node);
			CallChainNode chainNode = cgNodeMap.get(node);
			//check if it is an UI node
//			if(this.uiNodes.contains(node)) {
//				reachableUINodes.add(chainNode);
//			} else 
			{
				//XXX add the checkDevice, checkWidget methods
				//since some UI method does not necessary touch UI elements
				if(this.isCheckingMethod(node)) {
					reachableUINodes.add(chainNode);
				}
			}
			//skip if already visited, otherwise, add to the visited set
			if(visitedNodes.contains(node)) {
				continue;
			} else {
			    visitedNodes.add(node);
			}
			//skip the asyncExec / syncExec method calls
			if(this.isSafeMethod(node)) {
				//XXX need recursively check
				continue;
			}
			
			//add its succ nodes
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
		return isElementInsideArray(methodSig, this.checking_methods);
	}
	
	private boolean isSafeMethod(CGNode node) {
		String methodSig = node.getMethod().getSignature();
		return isElementInsideArray(methodSig, this.safe_methods);
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