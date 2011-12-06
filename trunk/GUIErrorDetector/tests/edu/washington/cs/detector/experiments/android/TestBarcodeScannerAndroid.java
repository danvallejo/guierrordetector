package edu.washington.cs.detector.experiments.android;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.util.Collection;
import java.util.List;

import com.ibm.wala.ipa.cha.ClassHierarchyException;

import edu.washington.cs.detector.AnomalyCallChain;
import edu.washington.cs.detector.CGBuilder.CG;
import edu.washington.cs.detector.CallChainFilter;
import edu.washington.cs.detector.experiments.filters.RemoveContainingNodeAfterStartStrategy;
import edu.washington.cs.detector.experiments.filters.RemoveContainingNodeBeforeStartStrategy;
import edu.washington.cs.detector.experiments.filters.RemoveContainingNodeStrategy;
import edu.washington.cs.detector.guider.CGTraverseAndroidGuider;
import edu.washington.cs.detector.guider.CGTraverseAndroidSafeMethodGuider;
import edu.washington.cs.detector.guider.CGTraverseGuider;
import edu.washington.cs.detector.guider.CGTraverseNoSystemCalls;
import edu.washington.cs.detector.guider.CGTraverseOnlyClientRunnableStrategy;
import edu.washington.cs.detector.util.AndroidUtils;
import edu.washington.cs.detector.util.ApkUtils;
import edu.washington.cs.detector.util.Globals;

public class TestBarcodeScannerAndroid extends AbstractAndroidTest {

	@Override
	protected String getAppPath() {
		String appPath = "D:\\research\\guierror\\subjects\\android-programs\\extracted\\zxingcore.jar"
			+ Globals.pathSep +
			"D:\\research\\guierror\\subjects\\android-programs\\extracted\\zxingandroid.jar"
			+ Globals.pathSep +
			"D:\\research\\guierror\\eclipsews\\GUIErrorDetector\\exp-subjects\\original-android.jar";
		
		return appPath;
	}

	@Override
	protected String getDirPath() {
		return "D:\\research\\guierror\\subjects\\android-programs\\extracted\\BarcodeScanner3.72.apk";
	}
	
	public void testFindErrors() throws ClassHierarchyException, IOException {
		CGTraverseGuider ui2startGuider = new CGTraverseAndroidGuider();
		CGTraverseGuider start2checkGuider = new CGTraverseAndroidSafeMethodGuider();
//		    new CGTraverseOnlyClientRunnableStrategy();
		try {
		  List<AnomalyCallChain> chains = super.findErrorsInAndroidApp(CG.RTA, ui2startGuider, start2checkGuider);
		  int count = 0;
		  for(AnomalyCallChain chain : chains) {
			 System.out.println("The " + count++ + "-th call chain:");
			 System.out.println(chain.getFullCallChainAsString());
		  }
		  chains = CallChainFilter.filter(chains, new RemoveContainingNodeStrategy("Lcom/google/zxing/client/android/DecodeThread, run()V"));
		  chains = CallChainFilter.filter(chains, new RemoveContainingNodeBeforeStartStrategy("Lcom/google/zxing/client/android/book/SearchBookContentsActivity, launchSearch()V"));
		  
		  System.out.println("size after removing DecodeThread.run() / SearchBookContentsActivity, launchSearch: " + chains.size());
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
	public void testUnzip() throws IOException {
		String apkToolDir = "D:\\develop-tools\\apktool";
		String apkFile = "D:\\research\\guierror\\subjects\\android-programs\\BarcodeScanner3.72.apk";
		String extractDir = "D:\\research\\guierror\\subjects\\android-programs\\extracted\\BarcodeScanner3.72.apk";
		super.decryptXML(apkToolDir, apkFile, extractDir);
	}

}
