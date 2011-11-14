package edu.washington.cs.detector;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.util.graph.Graph;

public abstract class AbstractMethodFinder implements AnomalyFinder {
	
    public final Graph<CGNode> cg;
	
	public final CGNode startNode;
	
	protected CGTraverseGuider guider = new CGTraverseDefaultGuider();
	
	public AbstractMethodFinder(Graph<CGNode> cg, CGNode startNode) {
		assert cg.containsNode(startNode);
		this.cg = cg;
		this.startNode = startNode;
	}
	
	public void setCGTraverseGuider(CGTraverseGuider guider) {
		assert guider != null;
		this.guider = guider;
	}
}
