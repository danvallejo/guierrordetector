package edu.washington.cs.detector;

import com.ibm.wala.ipa.callgraph.CGNode;

public class CGTraverseAndroidGuider implements CGTraverseGuider {

	//prune all system calls
	CGTraverseGuider systemGuider = new CGTraverseNoSystemCalls();
	
	@Override
	public boolean traverse(CGNode src, CGNode dest) {
		if(!systemGuider.traverse(src, dest)) {
			return false;
		}
		
		return false;
	}

}
