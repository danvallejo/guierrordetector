package edu.washington.cs.detector;

import com.ibm.wala.ipa.callgraph.CGNode;

public class CGTraverseAndroidSafeMethodGuider implements CGTraverseGuider {

	private String runOnUIThread = "android.app.Activity.runOnUiThread(Ljava/lang/Runnable;)V";
	
	@Override
	public boolean traverse(CGNode src, CGNode dest) {
		if(dest.getMethod().getSignature().equals(runOnUIThread)) {
			return false;
		}
		return true;
	}

}
