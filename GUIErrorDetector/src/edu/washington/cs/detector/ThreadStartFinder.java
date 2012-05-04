package edu.washington.cs.detector;

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
import com.ibm.wala.util.graph.Graph;

import edu.washington.cs.detector.guider.CGTraverseDefaultGuider;
import edu.washington.cs.detector.guider.CGTraverseGuider;
import edu.washington.cs.detector.util.Log;
import edu.washington.cs.detector.util.Utils;

/**
 * Another way for comparison is to find all paths between 2 nodes
 * http://stackoverflow.com/questions/58306/graph-algorithm-to-find-all-connections-between-two-arbitrary-vertices
 * */
public class ThreadStartFinder {
	
	public static boolean USE_DEF = false;
	
	public static boolean check_find_all_starts = false;

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
		if(guider == null) {
			throw new RuntimeException("Can not set a null guider");
		}
		this.guider = guider;
	}
	
	public static ThreadStartFinder createInstance(Graph<CGNode> cg, CGNode startPoint, CGTraverseGuider guider) {
		ThreadStartFinder finder = new ThreadStartFinder(cg, startPoint);
		if(guider != null) {
		    finder.setCGTraverseGuider(guider);
		}
		return finder;
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
			int removeIndex = 0;
			if(USE_DEF) {
				removeIndex = queue.size() - 1;
			}
			CGNode node = queue.remove(removeIndex); //queue.remove(0); //change to remove the last one for dfs
			
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
		
		//just check whether the algorithm has found every start node
		//this is just for debugging purpose which let users know some starts are not reachable
		if(check_find_all_starts) {
			Collection<CGNode> allStarts = getAllThreadStartNodes(this.cg);
			Collection<CGNode> traversedStarts = new LinkedHashSet<CGNode>();
			for(CallChainNode node : threadStarts) {
				traversedStarts.add(node.getNode());
			}
			Log.logln("All starts: " + allStarts);
			Log.logln("All traversed: " + traversedStarts);
			Log.logln("There are: " + allStarts.size()
					+ ", but  only : " + traversedStarts.size() + " of them are reachable.");
			//processing other starts
			Collection<CGNode> otherStarts = new LinkedHashSet<CGNode>();
			for(CGNode start : allStarts) {
				if(!traversedStarts.contains(start)) {
					otherStarts.add(start);
				}
			}
			for(CGNode otherStart : otherStarts) {
				CallChainNode startCallChainNode = findPathByBFS(this.cg, this.startPoint, otherStart);
				if(startCallChainNode != null) {
					threadStarts.add(startCallChainNode);
					Log.logln("Add new reachable start: " + otherStart);
				}
			}
		}
		
		return threadStarts;
	}
	
	public static Collection<CGNode> getAllThreadStartNodes(Graph<CGNode> graph) {
		Collection<CGNode> set = new LinkedHashSet<CGNode>();
		for(CGNode node : graph) {
			if(isThreadStartNode(node)) {
				set.add(node);
			}
		}
		return set;
	}
	
	public static boolean isThreadStartNode(CGNode node) {
		IMethod method = node.getMethod();
		//assume there is no override on the start method
		if(method.getSignature().trim().equals(THREAD_START_SIG)) {
			return true;
		}
		return false;
	}
	
	//FIXME redundant code below
	public static CallChainNode findPathByBFS(Graph<CGNode> cg, CGNode start, CGNode dest) {
        Map<CGNode, CallChainNode> cgNodeMap = new LinkedHashMap<CGNode, CallChainNode>();
		
		//keep track of all visited cg nodes
		Set<CGNode> visited = new HashSet<CGNode>();
		//do a BFS to find all reachable start
		List<CGNode> queue = new LinkedList<CGNode>();
		
		queue.add(start);
		cgNodeMap.put(start, new CallChainNode(start));
		
		while(!queue.isEmpty()) {
			int removeIndex = 0;
			CGNode node = queue.remove(removeIndex);
			
			Utils.checkTrue(cgNodeMap.containsKey(node));
			CallChainNode chainNode = cgNodeMap.get(node);
			
			//if it is a starting node
			if(node.equals(dest)) {
				return chainNode;
			}
			
			if(visited.contains(node)) {
				continue;
			} else {
			    visited.add(node);
			}

			//do the next level
			Iterator<CGNode> it = cg.getSuccNodes(node);
			while(it.hasNext()) {
				CGNode succNode = it.next();
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
		
		//not reachable
		return null;
	}
}