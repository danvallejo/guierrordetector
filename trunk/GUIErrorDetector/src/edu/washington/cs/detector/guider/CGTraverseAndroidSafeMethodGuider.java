package edu.washington.cs.detector.guider;

import com.ibm.wala.ipa.callgraph.CGNode;

import edu.washington.cs.detector.util.Utils;

public class CGTraverseAndroidSafeMethodGuider implements CGTraverseGuider {
	
	String[] safeMethods = new String[] {
			"android.app.Activity.runOnUiThread(Ljava/lang/Runnable;)V",
			"android.widget.ProgressBar.refreshProgress(IIZ)V",
			"android.view.View.performClick()Z",
			"com.google.android.apps.mytracks.ChartActivity$",
			"com.google.android.apps.mytracks.MyMapsList$",
			"com.google.android.apps.mytracks.MyTracks$",
			"com.google.android.apps.mytracks.MapActivity$",
			"com.google.android.apps.mytracks.NavControls$",
			"com.google.android.apps.mytracks.MyMapsList$",
			"com.google.android.apps.mytracks.SensorStateActivity$",
			"com.google.android.apps.mytracks.DialogManager4",
			"com.google.android.apps.mytracks.StatsActivity$",
			"com.google.android.apps.mytracks.io.ModernAuthManager$",
			"com.google.android.apps.mytracks.ChartActivity.clearTrackPoints()V"
		};
	
	@Override
	public boolean traverse(CGNode src, CGNode dest) {
		String destSig = dest.getMethod().getSignature();
//		if(Utils.includedIn(destSig, safeMethods)) {
//			return false;
//		}
		if(this.contains(destSig, safeMethods)) {
			return false;
		}
		return true;
	}
	
	private boolean contains(String dest, String[] strs) {
		for(String str : strs) {
			if(dest.indexOf(str) != -1) {
				return true;
			}
		}
		return false;
	}

}
