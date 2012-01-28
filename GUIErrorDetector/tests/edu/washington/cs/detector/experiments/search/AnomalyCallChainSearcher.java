package edu.washington.cs.detector.experiments.search;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.util.graph.Graph;

import edu.washington.cs.detector.MethodEvaluator;
import edu.washington.cs.detector.NativeMethodConnector;
import edu.washington.cs.detector.guider.CGTraverseGuider;

public class AnomalyCallChainSearcher {
	public final CGNode startNode;
	public final Graph<CGNode> graph;
	
	private CGTraverseGuider startGuider = null;
	private CGTraverseGuider uiGuider = null;
	private NativeMethodConnector connector = null;
	private ClassHierarchy cha = null;
	private String[] checkings = null;
	private MethodEvaluator evaluator = null;
	
	public AnomalyCallChainSearcher(Graph<CGNode> graph, CGNode startNode) {
		this.startNode = startNode;
		this.graph = graph;
	}
	
	public Collection<List<CGNode>> findAllAnomalyCallChains() {
		Collection<List<CGNode>> result = new LinkedHashSet<List<CGNode>>();
		
		ThreadStartSearcher startSearcher = new ThreadStartSearcher(this.graph, this.startNode);
		if(startGuider != null) {startSearcher.setTraverseGuider(startGuider); }
		
		LinkedList<CGNode> visited = new LinkedList<CGNode>();
		Collection<List<CGNode>> list2starts = startSearcher.breadFirst(this.graph, visited);
		
		for(List<CGNode> list : list2starts) {
			CGNode lastNode = list.get(list.size() - 1);
			if(!startSearcher.isDestNode(lastNode)) {
				throw new RuntimeException("lastnode is not destnode: " + lastNode);
			}
			
			UIAnomalySearcher uiSearcher = new UIAnomalySearcher(this.graph, lastNode);
			if(this.uiGuider != null) {uiSearcher.setTraverseGuider(uiGuider); }
			if(this.connector != null) {uiSearcher.setNativeMethodConnector(connector); }
			if(this.checkings != null) {uiSearcher.setCheckingMethods(checkings);}
			if(this.cha != null) {uiSearcher.setClassHierarchy(cha);}
			if(this.evaluator != null) {uiSearcher.setMethodEvaluator(evaluator); }
			
			LinkedList<CGNode> uiVisited = new LinkedList<CGNode>();
			Collection<List<CGNode>> ui2checks = uiSearcher.breadFirst(this.graph, uiVisited);
			
			for(List<CGNode> ui2check : ui2checks) {
				List<CGNode> full = new LinkedList<CGNode>();
				//assembly the list
				full.addAll(list);
				full.addAll(ui2check);
				result.add(full);
			}
		}
		
		return result;
	}
	
	/******
	 * All setters below
	 * *****/
	public void setMethodEvaluator(MethodEvaluator evaluator ) {
		this.evaluator = evaluator;
	}
	public void setStartGuider(CGTraverseGuider startGuider) {
		this.startGuider = startGuider;
	}
	public void setUiGuider(CGTraverseGuider uiGuider) {
		this.uiGuider = uiGuider;
	}
	public void setConnector(NativeMethodConnector connector) {
		this.connector = connector;
	}
	public void setCha(ClassHierarchy cha) {
		this.cha = cha;
	}
	public void setCheckings(String[] checkings) {
		this.checkings = checkings;
	}
}