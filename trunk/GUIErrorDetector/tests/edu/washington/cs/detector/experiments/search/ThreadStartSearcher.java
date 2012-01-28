package edu.washington.cs.detector.experiments.search;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.util.graph.Graph;

import edu.washington.cs.detector.ThreadStartFinder;

public class ThreadStartSearcher extends ExhaustiveSearcher {
	
	public ThreadStartSearcher(Graph<CGNode> graph, CGNode startNode) {
		super(graph, startNode);
	}

	@Override
	public boolean isDestNode(CGNode node) {
		return ThreadStartFinder.isThreadStartNode(node);
	}
	
}
