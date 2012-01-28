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

abstract public class ExhaustiveSearcher {
	
	public final Graph<CGNode> graph;
	
	public final CGNode startNode;
	
	CGTraverseGuider guider = new CGTraverseDefaultGuider();
	
	protected NativeMethodConnector connector = NativeMethodConnector.createEmptyConnector();
	
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
	
	public Collection<List<CGNode>> breadFirst(Graph<CGNode> graph, LinkedList<CGNode> visited) {
		Collection<List<CGNode>> returnChains = new LinkedHashSet<List<CGNode>>();
		
		CGNode lastNode = visited.getLast();
		LinkedList<CGNode> nodes = this.getAdjacentNodes(graph, lastNode);// graph.adjacentNodes(visited.getLast());
		if(lastNode.getMethod().isNative()) {
			if(!nodes.isEmpty()) {
				throw new RuntimeException("Must be empty.");
			}
			if(!this.connector.isEmpty()) {
				nodes.addAll(this.connector.getSucc(graph, lastNode));
			}
		}
		
        // examine adjacent nodes
        for (CGNode node : nodes) {
            if (visited.contains(node)) {
                continue;
            }
            if(!this.shouldVisit(lastNode, node)) {
            	continue;
            }
            if (this.isDestNode(node)) {
                visited.add(node);
//                printPath(visited);
                List<CGNode> chain = this.generatePath(visited);
                returnChains.add(chain);
                
                visited.removeLast();
                break;
            }
        }
        // in breadth-first, recursion needs to come after visiting adjacent nodes
        for (CGNode node : nodes) {
            if (visited.contains(node) || this.isDestNode(node)) {
                continue;
            }
            visited.addLast(node);
//            breadthFirst(graph, visited);
            returnChains.addAll(breadFirst(graph, visited));
            
            visited.removeLast();
        }
        
        return returnChains;
	}
	
	private LinkedList<CGNode> getAdjacentNodes(Graph<CGNode> graph, CGNode node) {
		LinkedList<CGNode> l = new LinkedList<CGNode>();
		Iterator<CGNode> it = graph.getSuccNodes(node);
		while(it.hasNext()) {
			l.add(it.next());
		}
		return l;
	}
	
	abstract public boolean isDestNode(CGNode node);
	
	public boolean shouldVisit(CGNode srcNode, CGNode destNode) {
		return guider.traverse(srcNode, destNode);
	}
	
	private List<CGNode> generatePath(LinkedList<CGNode> visited) {
		List<CGNode> returned = new LinkedList<CGNode>();
		returned.addAll(visited);
		return returned;
	}
	
}