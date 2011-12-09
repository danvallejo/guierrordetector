package edu.washington.cs.detector;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.util.graph.Graph;

import edu.washington.cs.detector.guider.CGTraverseDefaultGuider;
import edu.washington.cs.detector.guider.CGTraverseGuider;

public abstract class AbstractMethodFinder implements AnomalyFinder {
	
    public final Graph<CGNode> cg;
	
	public final CGNode startNode;
	
	protected CGTraverseGuider guider = new CGTraverseDefaultGuider();
	
	protected NativeMethodConnector connector = NativeMethodConnector.createEmptyConnector();
	
	public AbstractMethodFinder(Graph<CGNode> cg, CGNode startNode) {
		assert cg.containsNode(startNode);
		this.cg = cg;
		this.startNode = startNode;
	}
	
	public void setCGTraverseGuider(CGTraverseGuider guider) {
		if(guider == null) {
			throw new RuntimeException("The guider can not be null.");
		}
		this.guider = guider;
	}
	
	public void setNativeMethodConnector(NativeMethodConnector connector) {
		if(connector == null) {
			throw new RuntimeException("The native connector can not be null.");
		}
		this.connector = connector;
	}
}
