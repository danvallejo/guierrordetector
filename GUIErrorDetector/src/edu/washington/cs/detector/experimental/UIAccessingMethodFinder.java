package edu.washington.cs.detector.experimental;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.util.graph.Graph;

import edu.washington.cs.detector.AbstractMethodFinder;
import edu.washington.cs.detector.CallChainNode;
import edu.washington.cs.detector.MethodEvaluator;
import edu.washington.cs.detector.NativeMethodConnector;
import edu.washington.cs.detector.guider.CGTraverseGuider;
import edu.washington.cs.detector.util.Files;
import edu.washington.cs.detector.util.Log;
import edu.washington.cs.detector.util.Utils;
import edu.washington.cs.detector.util.WALAUtils;

public class UIAccessingMethodFinder extends AbstractMethodFinder {
	
	public static boolean USE_DEF = false;
	public static boolean DEBUG = false;
	private static String[] checking_methods
	    = Files.readWholeNoExp("./src/checking_methods.txt").toArray(new String[0]);
	
	private static Map<CGNode, List<CallChainNode>> cachedResult = new LinkedHashMap<CGNode, List<CallChainNode>>();	
	//bad design here, cha needs to be set when evaluator is set
	private static MethodEvaluator evaluator = null;
	/** reload all checking method for customization */
	public static String[] getCheckingMethods() {
		return checking_methods;
	}
	public static void setCheckingMethods(String fileName) {
		assert fileName != null;
		checking_methods = Files.readWholeNoExp(fileName).toArray(new String[0]);
		for(String method : checking_methods) {
			System.err.println("error checking method as sink: " + method);
		}
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
		System.err.println("Use method evaluator to identify UI-accessing method: " + evl);
		evaluator = evl;
	}
	
	private ClassHierarchy cha = null;
	public void setClassHierarchy(ClassHierarchy cha) {
		this.cha = cha;
	}
	
	private final Collection<CGNode> startNodes = new LinkedHashSet<CGNode>();
	private UIAccessingMethodFinder(Graph<CGNode> cg, Collection<CGNode> startNodes) {
		super(cg, null);
		this.startNodes.addAll(startNodes);
	}
	
	public static UIAccessingMethodFinder createInstance(ClassHierarchy cha, Graph<CGNode> cg, Collection<CGNode> startNodes, CGTraverseGuider guider) {
		return createInstance(cha, cg, startNodes, guider, null);
	}
	
	public static UIAccessingMethodFinder createInstance(ClassHierarchy cha, Graph<CGNode> cg, Collection<CGNode> startNodes,
			CGTraverseGuider guider, NativeMethodConnector connector) {
		UIAccessingMethodFinder finder = new UIAccessingMethodFinder(cg, startNodes);
		if(guider != null) {
		    finder.setCGTraverseGuider(guider);
		}
		if(cha!= null) {
			finder.setClassHierarchy(cha);
		}
		if(connector != null) {
			finder.setNativeMethodConnector(connector);
		}
		return finder;
	}
	
	public List<CallChainNode> findThreadUnsafeUINodes() {

		WALAUtils.logCallGraph(this.cg, DEBUG);
		
		List<CallChainNode> reachableUINodes = new LinkedList<CallChainNode>();
		//a map keep track of CGNode and is wrapped CallChainNode
		Map<CGNode, CallChainNode> cgNodeMap = new LinkedHashMap<CGNode, CallChainNode>();
		//a queue to keep the fringe
		List<CGNode> queue = new LinkedList<CGNode>();
		//a set to keep visited nodes in BFS
		Set<CGNode> visitedNodes = new HashSet<CGNode>();
		
		//visit the succ nodes of the start node
		for(CGNode oneStart : this.startNodes) {
			Log.logln("Visiting start: " + oneStart, DEBUG);
			Iterator<CGNode> nodeIt = this.cg.getSuccNodes(oneStart);
			while(nodeIt.hasNext()) {
				CGNode node = nodeIt.next();
				if(!super.guider.traverse(oneStart, node)) {
					Log.logln("  skip by guider after start: " + node, DEBUG);
					continue;
				}
				queue.add(node);
				Log.logln("  next node of start: " + node, DEBUG);
				assert !cgNodeMap.containsKey(node);
				cgNodeMap.put(node, new CallChainNode(node, oneStart));
			}
		}
		
		
		//perform BFS search here
		while(!queue.isEmpty()) {
			int removeIndex = 0;
			if(USE_DEF) {
				removeIndex = queue.size() - 1;
			}
			CGNode node = queue.remove(removeIndex);//queue.remove(0); //remove the last for DFS
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
			
			//check if it is a native method
			if(node.getMethod().isNative()) {
				//Log.logln("See a native: " + node);
				//Log.logln(" --  connector empty? : " + this.connector.isEmpty());
				if(succIt.hasNext()) {
					throw new RuntimeException("A native method should never call others in Java");
				}
				if(!this.connector.isEmpty()) {
					Collection<CGNode> succNodes = this.connector.getSucc(cg, node);
					//Log.logln("Get native callees from connector for method: " + node);
					for(CGNode succNode : succNodes) {
						queue.add(succNode);
						//Log.logln(" -  add native callee: " + succNode);
						if(!cgNodeMap.containsKey(succNode)) {
					        cgNodeMap.put(succNode, new CallChainNode(succNode, chainNode));
				       }
					}
				}
			}
			
			while(succIt.hasNext()) {
				CGNode succNode = succIt.next();
				
				if(!super.guider.traverse(node, succNode)) {
					Log.logln("  skip by guider: " + succNode, DEBUG);
					continue;
				}
				
				//Log.logln("  next node: " + succNode, DEBUG);
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