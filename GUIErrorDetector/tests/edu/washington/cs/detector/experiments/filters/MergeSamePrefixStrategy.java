package edu.washington.cs.detector.experiments.filters;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.ibm.wala.ipa.callgraph.CGNode;

import edu.washington.cs.detector.AnomalyCallChain;
import edu.washington.cs.detector.FilterStrategy;

public class MergeSamePrefixStrategy extends FilterStrategy {
	
	private final int same_prefix_num;
	
	public MergeSamePrefixStrategy(int same_prefix_num) {
		if(same_prefix_num < 1) {
			throw new RuntimeException("The input should >= 1");
		}
		this.same_prefix_num = same_prefix_num;
	}

	@Override
	public List<AnomalyCallChain> filter(List<AnomalyCallChain> chains) {
		//first classify each chains according to the path from UI to start
		//FIXME this part may be slow
		Map<String, List<AnomalyCallChain>> map = new HashMap<String, List<AnomalyCallChain>>();
		for(AnomalyCallChain chain : chains) {
			String ui2start = AnomalyCallChain.flatCGNodeList(chain.getUI2Start());
			if(map.containsKey(ui2start)) {
				map.get(ui2start).add(chain);
			} else {
				List<AnomalyCallChain> l = new LinkedList<AnomalyCallChain>();
				l.add(chain);
				map.put(ui2start, l);
			}
		}

		List<AnomalyCallChain> result = new LinkedList<AnomalyCallChain>();
		//then merge changes with the same prefix but only differing in the some tail nodes
		for(List<AnomalyCallChain> classifiedChains : map.values()) {
			result.addAll(this.mergeSimilarTails(classifiedChains));
		}
		
		//reclaim the memory
		map.clear();
		
		return result;
	}

	private List<AnomalyCallChain> mergeSimilarTails(List<AnomalyCallChain> list) {
		Map<String, AnomalyCallChain> mergedChains = new HashMap<String, AnomalyCallChain>();
		
		for(AnomalyCallChain chain : list) {
			if(chain.getStart2Check().size() < this.same_prefix_num) {
				mergedChains.put(chain.getFullCallChainAsString(), chain);
			} else {
				List<CGNode> prefixList = chain.getStart2Check().subList(0, this.same_prefix_num);
				String prefixStr = AnomalyCallChain.flatCGNodeList(prefixList);
				if(mergedChains.containsKey(prefixStr)) {
					if(chain.size() < mergedChains.get(prefixStr).size()) {
						mergedChains.put(prefixStr, chain); //replace the original one
					}
				} else {
					mergedChains.put(prefixStr, chain);
				}
			}
		}
		
		List<AnomalyCallChain> result = new LinkedList<AnomalyCallChain>();
		result.addAll(mergedChains.values());
		
		return result;
	}
}
