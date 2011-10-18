package edu.washington.cs.detector;

import java.util.List;

public abstract class FilterStrategy {
	public abstract List<AnomalyCallChain> filter(List<AnomalyCallChain> chains);
}