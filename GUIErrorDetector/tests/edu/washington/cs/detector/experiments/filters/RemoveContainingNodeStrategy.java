package edu.washington.cs.detector.experiments.filters;

import java.util.LinkedList;
import java.util.List;

import com.ibm.wala.ipa.callgraph.CGNode;

import edu.washington.cs.detector.AnomalyCallChain;
import edu.washington.cs.detector.FilterStrategy;

public class RemoveContainingNodeStrategy extends FilterStrategy {
	public final String sig;
	public RemoveContainingNodeStrategy(String sig) {
		this.sig = sig;
	}
	
	@Override
	public List<AnomalyCallChain> filter(List<AnomalyCallChain> chains) {
		List<AnomalyCallChain> result = new LinkedList<AnomalyCallChain>();
		for(AnomalyCallChain c : chains) {
			if(!remove(c)) {
			    result.add(c);
			}
		}
		return result;
	}
	
	protected boolean remove(AnomalyCallChain c) {
		for(CGNode node : c.getFullCallChain()) {
			if(node.toString().indexOf(this.sig) != -1) {
				return true;
			}
		}
		return false;
	}
}