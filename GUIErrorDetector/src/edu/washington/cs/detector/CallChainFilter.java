package edu.washington.cs.detector;

import java.util.LinkedList;
import java.util.List;

public class CallChainFilter {
	
	public final List<AnomalyCallChain> chains;
	
	public CallChainFilter(List<AnomalyCallChain> chains) {
		assert chains != null;
		this.chains = chains;
	}
	
	public List<AnomalyCallChain> apply(FilterStrategy...strategies) {
		List<FilterStrategy> slist = new LinkedList<FilterStrategy>();
		for(FilterStrategy strategy : strategies) {
			slist.add(strategy);
		}
		return apply(slist);
	}
	
	private List<AnomalyCallChain> apply(List<FilterStrategy> strategies) {
		assert strategies != null;
		assert strategies.size() != 0;
		
		List<AnomalyCallChain> result = this.chains;
		for(FilterStrategy strategy : strategies) {
			result = strategy.filter(result);
		}
		
		return result;
	}
	
	public static List<AnomalyCallChain> filter(List<AnomalyCallChain> chains, FilterStrategy strategy) {
		CallChainFilter filter = new CallChainFilter(chains);
		return filter.apply(strategy);
	}
	
	public static List<AnomalyCallChain> filter(List<AnomalyCallChain> chains, FilterStrategy... strategies) {
		CallChainFilter filter = new CallChainFilter(chains);
		return filter.apply(strategies);
	}
	
	public static List<AnomalyCallChain> filter(List<AnomalyCallChain> chains, List<FilterStrategy> strategies) {
		CallChainFilter filter = new CallChainFilter(chains);
		return filter.apply(strategies);
	}
}