package edu.washington.cs.detector.experiments.filters;
import com.ibm.wala.ipa.callgraph.CGNode;

import edu.washington.cs.detector.AnomalyCallChain;

public class RemoveContainingNodeAfterStartStrategy extends RemoveContainingNodeStrategy {

	public RemoveContainingNodeAfterStartStrategy(String sig) {
		super(sig);
	}
	
	protected boolean remove(AnomalyCallChain c) {
		boolean seeThreadStart = false;
		for(CGNode node : c.getFullCallChain()) {
			if(node.getMethod().getSignature().equals(thread_start_method)) {
				seeThreadStart = true;
			}
			//remove it when a specific method get called AFTER see start
			if(node.toString().indexOf(this.sig) != -1 && seeThreadStart) {
				return true;
			}
		}
		return false;
	}
}