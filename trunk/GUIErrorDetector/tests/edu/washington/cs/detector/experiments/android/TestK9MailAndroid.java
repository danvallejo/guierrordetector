package edu.washington.cs.detector.experiments.android;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.Collection;
import java.util.List;

import com.ibm.wala.ipa.cha.ClassHierarchyException;

import edu.washington.cs.detector.AnomalyCallChain;
import edu.washington.cs.detector.CGBuilder.CG;
import edu.washington.cs.detector.CallChainFilter;
import edu.washington.cs.detector.experiments.filters.MergeSameEntryToStartPathStrategy;
import edu.washington.cs.detector.guider.CGTraverseAndroidGuider;
import edu.washington.cs.detector.guider.CGTraverseGuider;
import edu.washington.cs.detector.guider.CGTraverseNoSystemCalls;
import edu.washington.cs.detector.guider.CGTraverseOnlyClientRunnableStrategy;
import edu.washington.cs.detector.util.AndroidUtils;
import edu.washington.cs.detector.util.ApkUtils;
import edu.washington.cs.detector.util.Globals;

public class TestK9MailAndroid extends AbstractAndroidTest {

	@Override
	protected String getAppPath() {
		String appPath = "D:\\research\\guierror\\subjects\\android-programs\\extracted\\k9-3.992-release.apk\\com"
			+ Globals.pathSep + 
			"D:\\research\\guierror\\subjects\\android-programs\\extracted\\k9-3.992-release.apk\\org"
			+ Globals.pathSep +
			"D:\\research\\guierror\\eclipsews\\GUIErrorDetector\\exp-subjects\\original-android.jar";
		
		return appPath;
	}

	@Override
	protected String getDirPath() {
		return "D:\\research\\guierror\\subjects\\android-programs\\extracted\\k9-3.992-release.apk";
	}
	
	public void testFindErrors() throws ClassHierarchyException, IOException {
		CGTraverseGuider ui2startGuider = new CGTraverseAndroidGuider();
		CGTraverseGuider start2checkGuider = new CGTraverseOnlyClientRunnableStrategy();
		try {
		    List<AnomalyCallChain> chains = super.findErrorsInAndroidApp(CG.RTA, ui2startGuider, start2checkGuider);
		    System.out.println("Before merging: " + chains.size());
		    chains = CallChainFilter.filter(chains, new MergeSameEntryToStartPathStrategy());
		    System.out.println("After merging: " + chains.size());
		    int i = 0;
		    for(AnomalyCallChain c : chains) {
		    	System.out.println("The " + i++ + "-th chain:");
			    System.out.println(c.getFullCallChainAsString());
		    }
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
	public void testUnzip() throws IOException {
		String apkToolDir = "D:\\develop-tools\\apktool";
		String apkFile = "D:\\research\\guierror\\subjects\\android-programs\\k9-3.992-release.apk";
		String extractDir = "D:\\research\\guierror\\subjects\\android-programs\\extracted\\k9-3.992-release.apk";
		super.decryptXML(apkToolDir, apkFile, extractDir);
	}

}
