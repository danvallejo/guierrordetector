package edu.washington.cs.detector;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import com.ibm.wala.ipa.callgraph.CGNode;

import edu.washington.cs.detector.util.Globals;

public class AnomalyCallChain {
	
	public final List<CGNode> ui2start = new LinkedList<CGNode>();
	private CGNode threadStart = null;
	public final List<CGNode> start2check = new LinkedList<CGNode>();
	
	public CGNode getThreadStartNode() {
		return this.threadStart;
	}
	
	public void addNodes(Collection<CGNode> ui2Start, CGNode threadStart, Collection<CGNode> start2Check) {
		assert threadStart != null;
		this.threadStart = threadStart;
		this.ui2start.addAll(ui2Start);
		this.start2check.addAll(start2Check);
	}
	
	public List<CGNode> getFullCallChain() {
		final List<CGNode> nodes = new LinkedList<CGNode>();
		nodes.addAll(ui2start);
		nodes.addAll(start2check);
		return nodes;
	}
	
	public String getFullCallChainAsString() {
		StringBuilder sb = new StringBuilder();
		int count = 0;
		for(CGNode node : getFullCallChain()) {
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