package edu.washington.cs.detector.experiments.filters;

import java.util.LinkedList;
import java.util.List;

import com.ibm.wala.ipa.callgraph.CGNode;

import edu.washington.cs.detector.AnomalyCallChain;
import edu.washington.cs.detector.FilterStrategy;

public class RemoveDoubleThreadStartStrategy extends FilterStrategy {

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
		int startNum = 0;
		for(CGNode node : c.getFullCallChain()) {
			if(node.toString().indexOf("Ljava/lang/Thread, start()V") != -1) {
				startNum++;
				if(startNum == 2) {
					return true;
				}
			}
		}
		return false;
	}
}
