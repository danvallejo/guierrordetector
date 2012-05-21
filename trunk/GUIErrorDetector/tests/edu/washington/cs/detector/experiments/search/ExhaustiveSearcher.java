package edu.washington.cs.detector.experiments.search;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.util.graph.Graph;

import edu.washington.cs.detector.NativeMethodConnector;
import edu.washington.cs.detector.guider.CGTraverseDefaultGuider;
import edu.washington.cs.detector.guider.CGTraverseGuider;
import edu.washington.cs.detector.util.Log;

/**
 * The basic algorithm to perform exhaustive search of all
 * non-cyclic paths on a graph.
 * */
abstract public class ExhaustiveSearcher {
	
	/**
	 * If set, stop searching when a certain amount of time reaches
	 * also see class SimpleClock for reference
	 * */
	public static boolean USE_CLOCK = false;
	
	/**
	 * The graph on which the search is performed
	 * */
	public final Graph<CGNode> graph;
	
	/**
	 * The starting node for a search
	 * */
	public final CGNode startNode;
	
	/**
	 * Customize graph search strategies if needed, e.g., which succ
	 * nodes to search first. By default, it does nothing.
	 * */
	CGTraverseGuider guider = new CGTraverseDefaultGuider();
	
	/**
	 * This connector represents the calling relationship between a native method
	 * and a Java method. It is initialized by reading the CalledByNativeMethods
	 * annotations for programs using native methods.
	 * By default, it does nothing.
	 * */
	protected NativeMethodConnector connector = NativeMethodConnector.createEmptyConnector();
	
	/**
	 * Constructors, and several simple setters
	 * */
	public ExhaustiveSearcher(Graph<CGNode> graph, CGNode startNode) {
		this.graph = graph;
		this.startNode = startNode;
	}
	public void setTraverseGuider(CGTraverseGuider guider) {
		this.guider = guider;
	}
	public void setNativeMethodConnector(NativeMethodConnector connector) {
		if(connector == null) {
			throw new RuntimeException("The native connector can not be null.");
		}
		this.connector = connector;
	}
	
	/**
	 * A method checking whether the current node is a target node or not.
	 * It must be overriden by subclasses. For example, when searching all
	 * paths from starting node to all thread-spawning node. This method
	 * returns true if node is "Thread.start()".
	 * */
	abstract protected boolean isDestNode(CGNode node);
	
	/**
	 * Performs breadth-first exhaustive search. It returns a collection of
	 * paths (called chains below). Here a path is a list of CGNode.
	 * 
	 * Note that, this is a recursive algorithm. Initially, clients use:
	 * 
	 *   breadFirst(graph, new LinkedList<CGNode>());  //empty visited nodes at the beginning
	 *   
	 * */
	private static long pathNum = 0;
	public Collection<List<CGNode>> breadFirst(Graph<CGNode> graph, LinkedList<CGNode> visited) {
		Collection<List<CGNode>> returnChains = new LinkedHashSet<List<CGNode>>();
		//return the current visited chains if time is up
		if(USE_CLOCK) {
        	if(SimpleClock.finish()) {
        		return returnChains;
        	}
        }
		//fetch one from the visited list, and get all adjacent nodes.
		CGNode lastNode = visited.getLast();
		pathNum++;
		if(pathNum % 10000000 == 0) {
			Log.logln("The current number of explored paths: " + pathNum);
		}
		LinkedList<CGNode> ajacentNodes = this.getAdjacentNodes(graph, lastNode);
		//check whether that it is a native method or not
		//if it is native method, check whether this native method is annotated to be called
		//by java methods
		if(lastNode.getMethod().isNative()) {
			if(!ajacentNodes.isEmpty()) {
				throw new RuntimeException("The visited nodes can not be empty. A native method" +
						" can not be called directly by client.");
			}
			//check the CalledByNativeMethods annotations
			if(!this.connector.isEmpty()) {
				ajacentNodes.addAll(this.connector.getSucc(graph, lastNode));
			}
		}
        // examine each ajacentNode one by one
		// note the following code does NOT add node to visited, it just checks
        for (CGNode node : ajacentNodes) {
        	//skip if already visited
            if (visited.contains(node)) {
                continue;
            }
            //a customized searching strategy. do nothing at default,
            //for debugging purpose.
            if(!this.shouldVisit(lastNode, node)) {
            	continue;
            }
            //if it is the target node
            if (this.isDestNode(node)) {
                visited.add(node);
                //create the path
                List<CGNode> chain = this.generatePath(visited);
                returnChains.add(chain);
//                if(returnChains.size() % 100 == 0) {
//                	System.out.println("chain num: " + returnChains.size());
//                }
                //return if time up
                if(USE_CLOCK) {
                	if(SimpleClock.finish()) {
                		return returnChains;
                	}
                }
                visited.removeLast();
                break;
            }
        }
        /**
         * The following is the critical recursion code.
         * Doing recursion AFTER visiting adjacent nodes
         * */
        for (CGNode node : ajacentNodes) {
            if (visited.contains(node) || this.isDestNode(node)) {
                continue;
            }
            if(USE_CLOCK) {
            	if(SimpleClock.finish()) {
            		return returnChains;
            	}
            }
            visited.addLast(node);
            //do recursive below
            returnChains.addAll(breadFirst(graph, visited));
            visited.removeLast();
        }
        
        return returnChains;
	}
	
	/**
	 * Gets all adjacent nodes in a graph
	 * */
	private LinkedList<CGNode> getAdjacentNodes(Graph<CGNode> graph, CGNode node) {
		LinkedList<CGNode> l = new LinkedList<CGNode>();
		Iterator<CGNode> it = graph.getSuccNodes(node);
		while(it.hasNext()) {
			l.add(it.next());
		}
		return l;
	}
	
	/**
	 * Check if the edge srcNode -> destNode should be visited or not.
	 * By default, it always returns true, i.e., no effect of pruning.
	 * */
	private boolean shouldVisit(CGNode srcNode, CGNode destNode) {
		return guider.traverse(srcNode, destNode);
	}
	
	/**
	 * Dumps the visited nodes in sequence and returns a path
	 * */
	private List<CGNode> generatePath(LinkedList<CGNode> visited) {
		List<CGNode> returned = new LinkedList<CGNode>();
		returned.addAll(visited);
		return returned;
	}
}