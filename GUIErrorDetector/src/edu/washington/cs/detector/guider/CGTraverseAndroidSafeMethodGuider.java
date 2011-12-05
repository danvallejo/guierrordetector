package edu.washington.cs.detector.guider;

import com.ibm.wala.ipa.callgraph.CGNode;

import edu.washington.cs.detector.util.Utils;

public class CGTraverseAndroidSafeMethodGuider implements CGTraverseGuider {
	
	String[] safeMethods = new String[] {
			"android.app.Activity.runOnUiThread(Ljava/lang/Runnable;)V",
			"android.widget.ProgressBar.refreshProgress(IIZ)V",
		};
	
	@Override
	public boolean traverse(CGNode src, CGNode dest) {
		String destSig = dest.getMethod().getSignature();
		if(Utils.includedIn(destSig, safeMethods)) {
			return false;
		}
		return true;
	}

}
