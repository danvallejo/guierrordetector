package edu.washington.cs.detector.experiments.filters;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ibm.wala.ipa.callgraph.CGNode;

import edu.washington.cs.detector.AnomalyCallChain;
import edu.washington.cs.detector.FilterStrategy;

public class RemoveSubsumedChainStrategy extends FilterStrategy {
	
	private Collection<CGNode> startingNodes = new HashSet<CGNode>();
	
	public RemoveSubsumedChainStrategy() {}
	
	public RemoveSubsumedChainStrategy(Collection<CGNode> startingNodes) {this.startingNodes.addAll(startingNodes);}

	@Override
	public List<AnomalyCallChain> filter(List<AnomalyCallChain> chains) {
		
		if(!this.startingNodes.isEmpty()) {
			return filterByStartingPoint(chains);
		}
		
		Map<AnomalyCallChain, String> map = new LinkedHashMap<AnomalyCallChain, String>();
		
		for(AnomalyCallChain chain : chains) {
			//the flat string of the anomaly call chain
			String chainStr = AnomalyCallChain.flatCGNodeList(chain.getFullCallChain());
			
			boolean shouldAdd = true;
			List<AnomalyCallChain> shouldRemoveList = new LinkedList<AnomalyCallChain>();
			for(AnomalyCallChain c : map.keySet()) {
				String existStr = map.get(c);
				//the existing chain subsume the new one
				if(existStr.indexOf(chainStr) != -1) {
					shouldRemoveList.add(c); 
				}
				//the new one subsume an existing one
				if(chainStr.indexOf(existStr) != -1 && !chainStr.equals(existStr)) {
					shouldAdd = false;
				}
			}
			
			//add new chain
			if(shouldAdd) {
				map.put(chain, chainStr);
			}
			//remove existing chains
			for(AnomalyCallChain removing : shouldRemoveList ) {
				map.remove(removing);
			}
			//the following case is impossible
			if(!shouldAdd && !shouldRemoveList.isEmpty()) {
				throw new RuntimeException("Impossible that a longer chain exists, " +
						"but still need to remove chain been subsumed by the new chain.");
				
			}
			
		}
		

		List<AnomalyCallChain> result = new LinkedList<AnomalyCallChain>();
		result.addAll(map.keySet());
		
		//reclaim memory
		map.clear();
		
		return result;
	}
	
	public List<AnomalyCallChain> filterByStartingPoint(List<AnomalyCallChain> chains) {
		Set<String> startingMethodsSigs = new HashSet<String>();
		for(CGNode node : this.startingNodes) {
			startingMethodsSigs.add(node.getMethod().getSignature());
		}
		List<AnomalyCallChain> result = new LinkedList<AnomalyCallChain>();
		
		for(AnomalyCallChain chain : chains) {
			boolean shouldKeep = true;
			List<CGNode> nodes = chain.getUI2Start();
			if(nodes.size() < 3) {
				shouldKeep = true;
			} else {
			    for(int i = 1 /*note it is not 0*/; i < nodes.size() - 1; i++) {
				    CGNode node = nodes.get(i);
				    if(startingMethodsSigs.contains(node.getMethod().getSignature())) {
				    	shouldKeep = false;
				    	break;
				    }
			    }
			}
			if(shouldKeep) {
				result.add(chain);
			}
		}
		
		return result;
		
	}
}