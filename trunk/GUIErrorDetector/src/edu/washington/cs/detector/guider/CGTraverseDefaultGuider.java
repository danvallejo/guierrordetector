package edu.washington.cs.detector.guider;

import com.ibm.wala.ipa.callgraph.CGNode;

public class CGTraverseDefaultGuider implements CGTraverseGuider {
	@Override
	public boolean traverse(CGNode src, CGNode dest) {
		return true;
	}
}
