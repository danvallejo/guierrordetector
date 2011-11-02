package edu.washington.cs.detector;

import java.util.List;

public abstract class FilterStrategy {
	protected final String thread_start_method = "Ljava/lang/Thread, start()V";
	public abstract List<AnomalyCallChain> filter(List<AnomalyCallChain> chains);
}