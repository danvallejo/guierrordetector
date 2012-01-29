package edu.washington.cs.detector.experiments.filters;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import edu.washington.cs.detector.AnomalyCallChain;
import edu.washington.cs.detector.FilterStrategy;

//this is incomplete, may filter good anomaly call chains
public class MergeSameEntryToStartPathStrategy extends FilterStrategy {

	@Override
	public List<AnomalyCallChain> filter(List<AnomalyCallChain> chains) {
		List<AnomalyCallChain> result = new LinkedList<AnomalyCallChain>();
		
		Set<String> uniqueEntryToStartPath = new HashSet<String>();
		
		for(AnomalyCallChain chain : chains) {
//			String str = AnomalyCallChain.flatCGNodeList(chain.getUI2Start());
			String str = AnomalyCallChain.flatCGNodeListWithoutContext(chain.getUI2Start());
			if(!uniqueEntryToStartPath.contains(str)) {
				uniqueEntryToStartPath.add(str);
				result.add(chain);
			}
		}
		
		return result;
	}

}
