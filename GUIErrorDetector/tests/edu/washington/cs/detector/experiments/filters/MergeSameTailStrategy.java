package edu.washington.cs.detector.experiments.filters;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.ibm.wala.ipa.callgraph.CGNode;

import edu.washington.cs.detector.AnomalyCallChain;
import edu.washington.cs.detector.FilterStrategy;

public class MergeSameTailStrategy extends FilterStrategy {

	@Override
	public List<AnomalyCallChain> filter(List<AnomalyCallChain> chains) {
		//containing the same tail
		Map<String, AnomalyCallChain> tailMap = new HashMap<String, AnomalyCallChain>();
		
		List<AnomalyCallChain> result = new LinkedList<AnomalyCallChain>();
		for(AnomalyCallChain chain : chains) {
			List<CGNode> tail = chain.getStart2Check();
			String tailStr = this.nodeList2String(tail);
			if(tailMap.containsKey(tailStr)) {
				if(chain.getFullCallChain().size() < tailMap.get(tailStr).getFullCallChain().size()) {
					tailMap.put(tailStr, chain);
				}
			} else {
				tailMap.put(tailStr, chain);
			}
		}
		//add to the result list
		result.addAll(tailMap.values());
		
		//reclaim memory
		tailMap.clear();
		
		return result;
		
	}

	private String nodeList2String(List<CGNode> tail) {
		StringBuilder sb = new StringBuilder();
		for(CGNode node : tail) {
			sb.append(node.getMethod().toString());
		}
		return sb.toString();
	}
}
