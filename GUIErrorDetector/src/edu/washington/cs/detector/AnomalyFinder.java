package edu.washington.cs.detector;

import java.util.List;

import edu.washington.cs.detector.guider.CGTraverseGuider;

public interface AnomalyFinder {
	public List<CallChainNode> findThreadUnsafeUINodes();
	public void setCGTraverseGuider(CGTraverseGuider guider);
}
