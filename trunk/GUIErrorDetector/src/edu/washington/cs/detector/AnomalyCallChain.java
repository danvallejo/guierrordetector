package edu.washington.cs.detector;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import com.ibm.wala.ipa.callgraph.CGNode;

import edu.washington.cs.detector.util.Globals;

public class AnomalyCallChain {
	
	public final List<CGNode> nodes = new LinkedList<CGNode>();
	
	public void addChainNodes(Collection<CallChainNode> nodes) {
		for(CallChainNode node : nodes) {
			this.nodes.add(node.node);
		}
	}
	
	public void addCGNodes(Collection<CGNode> nodes) {
		this.nodes.addAll(nodes);
	}
	
	public List<CGNode> getFullCallChain() {
		return nodes;
	}
	
	public String getFullCallChainAsString() {
		StringBuilder sb = new StringBuilder();
		int count = 0;
		for(CGNode node : nodes) {
            if(count != 0) {
			   sb.append(" -> ");	
			}
            count++;
			sb.append(node);
			sb.append(", line: ");
			sb.append(node.getMethod().getLineNumber(0));
			sb.append(Globals.lineSep);
		}
		return sb.toString();
	}

}