package edu.washington.cs.detector;

import com.ibm.wala.ipa.callgraph.CGNode;

public interface CGTraverseGuider {
	public boolean traverse(CGNode src, CGNode dest);
}