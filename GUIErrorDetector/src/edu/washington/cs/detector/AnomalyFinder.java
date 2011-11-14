package edu.washington.cs.detector;

import java.util.List;

public interface AnomalyFinder {
	public List<CallChainNode> findThreadUnsafeUINodes();
	public void setCGTraverseGuider(CGTraverseGuider guider);
}
