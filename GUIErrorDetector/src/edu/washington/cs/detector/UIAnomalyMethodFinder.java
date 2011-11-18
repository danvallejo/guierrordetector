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
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.util.graph.Graph;

import edu.washington.cs.detector.util.Files;
import edu.washington.cs.detector.util.Log;
import edu.washington.cs.detector.util.Utils;
import edu.washington.cs.detector.util.WALAUtils;

public class UIAnomalyMethodFinder extends AbstractMethodFinder {
	
	public static boolean DEBUG = false;
	
	//some methods like Display#getBounds won't touch a UI element, but call checkDevice
	//need to see the IR for more details. so we check if a thread.start method can
	//reach a few thread safety checking methods
	private static String[] checking_methods
	    = Files.readWholeNoExp("./src/checking_methods.txt").toArray(new String[0]);
	
	private static Map<CGNode, List<CallChainNode>> cachedResult = new LinkedHashMap<CGNode, List<CallChainNode>>();
	
	//bad design here
	private static MethodEvaluator evaluator = null;
	
	private ClassHierarchy cha = null;
	
	/** reload all checking method for customization */
	public static String[] getCheckingMethods() {
		return checking_methods;
	}
	public static void setCheckingMethods(String fileName) {
		assert fileName != null;
		checking_methods = Files.readWholeNoExp(fileName).toArray(new String[0]);
	}
	public static Map<CGNode, List<CallChainNode>> getCachedResult() {
		return cachedResult;
	}
	/** clear the cache*/
	public static void clearCachedResult() {
		cachedResult.clear();
	}
	
	/**Set the evaluator, evaluating whether a method is thread safe or not*/
	public static void setMethodEvaluator(MethodEvaluator evl) {
		evaluator = evl;
	}
	
	public void setClassHierarchy(ClassHierarchy cha) {
		this.cha = cha;
	}

	private UIAnomalyMethodFinder(Graph<CGNode> cg, CGNode startNode) {
		super(cg, startNode);
	}
	
	public static UIAnomalyMethodFinder createInstance(ClassHierarchy cha, Graph<CGNode> cg, CGNode startNode, CGTraverseGuider guider) {
		UIAnomalyMethodFinder finder = new UIAnomalyMethodFinder(cg, startNode);
		if(guider != null) {
		    finder.setCGTraverseGuider(guider);
		}
		if(cha!= null) {
			finder.setClassHierarchy(cha);
		}
		return finder;
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

		WALAUtils.viewCallGraph(this.cg, DEBUG);
		
		//first check the cache
		if(cachedResult.containsKey(this.startNode)) {
			System.out.println("Find: " + this.startNode + " in cache.");
			return cachedResult.get(this.startNode);
		} else {
			System.out.println("Start to find reachable UI nodes: " + this.startNode + " in cache.");
		}
		
		List<CallChainNode> reachableUINodes = new LinkedList<CallChainNode>();
		//a map keep track of CGNode and is wrapped CallChainNode
		Map<CGNode, CallChainNode> cgNodeMap = new LinkedHashMap<CGNode, CallChainNode>();
		//a queue to keep the fringe
		List<CGNode> queue = new LinkedList<CGNode>();
		//a set to keep visited nodes in BFS
		Set<CGNode> visitedNodes = new HashSet<CGNode>();
		
		//visit the succ nodes of the start node
		Log.logln("Visiting start: " + this.startNode, DEBUG);
		
		Iterator<CGNode> nodeIt = this.cg.getSuccNodes(this.startNode);
		while(nodeIt.hasNext()) {
			CGNode node = nodeIt.next();
			queue.add(node);
			
			Log.logln("  next node of start: " + node, DEBUG);
			
			assert !cgNodeMap.containsKey(node);
			cgNodeMap.put(node, new CallChainNode(node, this.startNode));
		}
		//perform BFS search here
		while(!queue.isEmpty()) {
			CGNode node = queue.remove(0);
			Log.logln("Visiting node: " + node, DEBUG);
			
			assert cgNodeMap.containsKey(node);
			CallChainNode chainNode = cgNodeMap.get(node);
			
			if(this.isCheckingMethod(node)) {
				reachableUINodes.add(chainNode);
				continue;
			}
			
			//skip if already visited, otherwise, add to the visited set
			if(visitedNodes.contains(node)) {
				Log.logln("  -- skip node: " + node, DEBUG);
				continue;
			} else {
			    visitedNodes.add(node);
			}
			//add the succ nodes to the queue and continue to traverse
			Iterator<CGNode> succIt = this.cg.getSuccNodes(node);
			while(succIt.hasNext()) {
				CGNode succNode = succIt.next();
				
				if(!super.guider.traverse(node, succNode)) {
					Log.logln("  skip by guider: " + succNode, DEBUG);
					continue;
				}
				
				Log.logln("  next node: " + succNode, DEBUG);
				queue.add(succNode);
				if(!cgNodeMap.containsKey(succNode) //|| this.isCheckingMethod(succNode) 
						) {
					cgNodeMap.put(succNode, new CallChainNode(succNode, chainNode));
				}
			}
		}
		
		//remove repetition
		Log.logln("Finding UI anomaly method, before removing repetition: " + reachableUINodes.size());
		reachableUINodes = Utils.removeRedundantCallChains(reachableUINodes);
		Log.logln("    finding UI anomaly method, after removing repetition: " + reachableUINodes.size());
		
		//put into cache
		cachedResult.put(startNode, reachableUINodes);
		
		return reachableUINodes;
	}
	
	private boolean isCheckingMethod(CGNode node) {
		IMethod m = node.getMethod();
		String methodSig = m.getSignature();
		//System.out.println(methodSig);
		if(Utils.<String>includedIn(methodSig, checking_methods) ) {
			return true;
		} else if(evaluator != null) {
			if(this.cha == null) {
				throw new RuntimeException("If the evaluator is set, ClassHierarchy can not be null.");
			}
			return evaluator.isThreadUnsafeMethod(this.cha, m);
		} else {
			return false;
		}
	}
}