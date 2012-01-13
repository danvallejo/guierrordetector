package edu.washington.cs.detector.guider;

import com.ibm.wala.ipa.callgraph.CGNode;


public class CGTraverseSwingGuider implements CGTraverseGuider {
    CGTraverseGuider systemGuider = new CGTraverseNoSystemCalls();
	
	//Excluding the following method calls:
	String[] swingCalls = new String[] {
		"Ljavax/swing/SwingUtilities, isEventDispatchThread()Z",
		"Ljava/awt/Window, dispose()V"
	};
	
	@Override
	public boolean traverse(CGNode src, CGNode dest) {
		if(!systemGuider.traverse(src, dest)) {
			return false;
		}
		//check specific SWT rules 
		if(this.matchCalls(dest, swingCalls)) {
			return false;
		}
		
		return true;
	}
	
	private boolean matchCalls(CGNode dest, String[] calls) {
		String destStr = dest.toString();
		for(String str : calls) {
			if(destStr.indexOf(str) != -1) {
				return true;
			}
		}
		return false;
	}
}
