package edu.washington.cs.detector.experiments.android;

import java.io.IOException;
import java.util.List;

import com.ibm.wala.ipa.cha.ClassHierarchyException;

import edu.washington.cs.detector.AnomalyCallChain;
import edu.washington.cs.detector.CallChainFilter;
import edu.washington.cs.detector.CGBuilder.CG;
import edu.washington.cs.detector.experiments.filters.RemoveNoClientClassStrategy;
import edu.washington.cs.detector.experiments.filters.RemoveNonClientHeadStrategy;
import edu.washington.cs.detector.guider.CGTraverseAndroidGuider;
import edu.washington.cs.detector.guider.CGTraverseAndroidSafeMethodGuider;
import edu.washington.cs.detector.guider.CGTraverseGuider;
import edu.washington.cs.detector.util.Globals;

public class TestSGTPuzzle extends AbstractAndroidTest {

	@Override
	protected String getAppPath() {
		String appPath = 
			"D:\\research\\guierror\\subjects\\android-programs\\extracted\\SGTPuzzles-9306-7.apk\\name\\boyle\\chris\\sgtpuzzles"
			+ Globals.pathSep +
			"D:\\research\\guierror\\eclipsews\\GUIErrorDetector\\exp-subjects\\original-android.jar";
		
		return appPath;
	}

	@Override
	protected String getDirPath() {
		return "D:\\research\\guierror\\subjects\\android-programs\\extracted\\SGTPuzzles-9306-7.apk";
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
		  
		  System.out.println("Before filtering: " + chains.size());
		  
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	public void testUnzip() throws IOException {
		String apkToolDir = "D:\\develop-tools\\apktool";
		String apkFile = "D:\\research\\guierror\\subjects\\android-programs\\SGTPuzzles-9306-7.apk";
		String extractDir = "D:\\research\\guierror\\subjects\\android-programs\\extracted\\SGTPuzzles-9306-7.apk";
		super.decryptXML(apkToolDir, apkFile, extractDir);
	}
}
