package edu.washington.cs.detector;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.ibm.wala.ipa.callgraph.CGNode;

import edu.washington.cs.detector.util.Globals;

//keep track the whole call chain
public class CallChainNode {
	
	public final CGNode node;
	
	private CallChainNode parent = null;
	
	public CallChainNode(CGNode node) {
		//this(node, (CGNode)null);
		assert node != null;
		this.node = node;
	}
	
	public CallChainNode(CGNode node, CGNode parent) {
		//this(node, new CallChainNode(parent));
		assert node != null;
		this.node = node;
		this.parent = new CallChainNode(parent);
	}
	
	public CallChainNode(CGNode node, CallChainNode parent) {
		assert node != null;
		assert parent != null;
		this.node = node;
		this.parent = parent;
		//compute line number
		//this.lineNum = this.node.getMethod().getLineNumber(0);
	}
	
	public CallChainNode getParent() {
		return parent;
	}
	
	public void setParent(CGNode node) {
		assert this.parent == null;
		this.parent = new CallChainNode(node);
	}
	
	public List<CGNode> getChainToRoot() {
		List<CGNode> list = new LinkedList<CGNode>();
		
		list.add(node);
		CallChainNode parentNode = parent;
		while(parentNode != null) {
			list.add(parentNode.node);
			parentNode = parentNode.getParent();
		}
		
		//reverse it
		Collections.reverse(list);
		
		return list;
	}
	
	public String getChainToRootAsStr() {
		StringBuilder sb = new StringBuilder();
		int count = 0;
		for(CGNode node : getChainToRoot()) {
            if(count != 0) {
			   sb.append(" -> ");	
			}
            count++;
			sb.append(node); //print method?, not the node; avoiding context info
			sb.append(", line: ");
			sb.append(node.getMethod().getLineNumber(0));
			sb.append(Globals.lineSep);
		}
		return sb.toString();
	}
}