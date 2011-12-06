package edu.washington.cs.detector.guider;

import com.ibm.wala.ipa.callgraph.CGNode;

import edu.washington.cs.detector.util.Utils;

public class CGTraverseAndroidGuider implements CGTraverseGuider {

	//prune all system calls
	CGTraverseGuider systemGuider = new CGTraverseNoSystemCalls();
	
	String[] safeMethods = new String[] {
		"android.app.Dialog.show()V",
		"android.app.Dialog.dismiss()V",
		"android.webkit.CacheManager.removeAllCacheFiles()Z",
		"android.webkit.WebView.requestFormData(Ljava/lang/String;I)V",
		"android.webkit.WebSyncManager.<init>(Landroid/content/Context;Ljava/lang/String;)V",
		"android.database.sqlite.SQLiteCursor.fillWindow(I)V",
		"com.google.zxing.client.android.CaptureActivity.initCamera(Landroid/view/SurfaceHolder;)V",
		"android.webkit.WebViewWorker.getHandler()Landroid/webkit/WebViewWorker;",
		"android.widget.Filter.filter(Ljava/lang/CharSequence;Landroid/widget/Filter$FilterListener;)V",
		"android.net.http.IdleCache.cacheConnection(Lorg/apache/http/HttpHost;Landroid/net/http/Connection;)Z",
		"android.net.http.RequestQueue$ActivePool.startup()V",
		"android.webkit.CookieManager.removeExpiredCookie()V"
	};
	
	@Override
	public boolean traverse(CGNode src, CGNode dest) {
		if(!systemGuider.traverse(src, dest)) {
			return false;
		}
		
		String destSig = dest.getMethod().getSignature();
//		if(destSig.indexOf("WebSyncManager") != -1) {
//		    System.out.println(destSig);
//		}
		if(Utils.includedIn(destSig, safeMethods)) {
			return false;
		}
		
		return true;
	}

}
