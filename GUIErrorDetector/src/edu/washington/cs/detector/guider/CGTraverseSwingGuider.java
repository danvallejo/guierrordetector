package edu.washington.cs.detector.guider;

import com.ibm.wala.ipa.callgraph.CGNode;

public class CGTraverseSwingGuider implements CGTraverseGuider {
    CGTraverseGuider systemGuider = new CGTraverseNoSystemCalls();
	
	//Excluding the following method calls:
	String[] swingCalls = new String[] {
		"Ljavax/swing/SwingUtilities, isEventDispatchThread()Z"
	};
	
	@Override
	public boolean traverse(CGNode src, CGNode dest) {
		if(!systemGuider.traverse(src, dest)) {
			return false;
		}
		//check specific SWT rules 
		if(this.matchExcludedSWTCalls(dest)) {
			return false;
		}
		return true;
	}
	
	private boolean matchExcludedSWTCalls(CGNode dest) {
		String destStr = dest.toString();
		for(String str : swingCalls) {
			if(destStr.indexOf(str) != -1) {
				return true;
			}
		}
		return false;
	}
}
