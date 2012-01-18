package edu.washington.cs.detector.experiments.filters;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import edu.washington.cs.detector.AnomalyCallChain;
import edu.washington.cs.detector.FilterStrategy;

public class RemoveSameEntryStrategy extends FilterStrategy {

	@Override
	public List<AnomalyCallChain> filter(List<AnomalyCallChain> chains) {
		
		Map<String, AnomalyCallChain> entryMap = new HashMap<String, AnomalyCallChain>();
		
		List<AnomalyCallChain> result = new LinkedList<AnomalyCallChain>();
		for(AnomalyCallChain chain : chains) {
//			String entry = chain.getUI2Start().get(0).getMethod().toString();
			String entry = chain.getFullCallChain().get(0).getMethod().toString();
			if(!entryMap.containsKey(entry)) {
				entryMap.put(entry, chain);
			} else {
				if(chain.getFullCallChain().size() < entryMap.get(entry).getFullCallChain().size()) {
					entryMap.put(entry, chain);
				}
			}
		}
		
		result.addAll(entryMap.values());
		return result;
	}

}
