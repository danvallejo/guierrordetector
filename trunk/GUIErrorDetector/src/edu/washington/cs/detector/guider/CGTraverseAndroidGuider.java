package edu.washington.cs.detector.guider;

import com.ibm.wala.ipa.callgraph.CGNode;

import edu.washington.cs.detector.util.Utils;

public class CGTraverseAndroidGuider implements CGTraverseGuider {

	//prune all system calls
	CGTraverseGuider systemGuider = new CGTraverseNoSystemCalls();
	
	String[] safeMethods = new String[] {
		"android.app.Dialog.show()V"
	};
	
	@Override
	public boolean traverse(CGNode src, CGNode dest) {
		if(!systemGuider.traverse(src, dest)) {
			return false;
		}
		
		String destSig = dest.getMethod().getSignature();
		if(Utils.includedIn(destSig, safeMethods)) {
			return false;
		}
		
		return false;
	}

}
