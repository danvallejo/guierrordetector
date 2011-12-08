package edu.washington.cs.detector.experiments.android;

import java.io.IOException;
import java.util.List;

import edu.washington.cs.detector.AnomalyCallChain;
import edu.washington.cs.detector.CallChainFilter;
import edu.washington.cs.detector.CGBuilder.CG;
import edu.washington.cs.detector.experiments.filters.MergeSameEntryToStartPathStrategy;
import edu.washington.cs.detector.guider.CGTraverseAndroidGuider;
import edu.washington.cs.detector.guider.CGTraverseExploreClientRunnableStrategy;
import edu.washington.cs.detector.guider.CGTraverseGuider;
import edu.washington.cs.detector.guider.CGTraverseOnlyClientRunnableStrategy;
import edu.washington.cs.detector.util.Globals;
import edu.washington.cs.detector.util.Utils;

public class TestMytracksAndroid extends AbstractAndroidTest {

	@Override
	protected String getAppPath() {
		String appPath = "D:\\research\\guierror\\subjects\\android-programs\\sourcecode\\mytracks-april-29\\mytracks-april-29.jar"
			+ Globals.pathSep +
			"D:\\research\\guierror\\subjects\\android-programs\\sourcecode\\mytracks-april-29\\mytrackslib-april-29.jar"
			+ Globals.pathSep +
			"D:\\research\\guierror\\subjects\\android-programs\\sourcecode\\mytracks-april-29\\MyTracks\\libs\\accounts.jar"
		    + Globals.pathSep +
		    "D:\\research\\guierror\\subjects\\android-programs\\sourcecode\\mytracks-april-29\\MyTracks\\libs\\google-api-client-1.2.2-alpha.jar"
		    + Globals.pathSep +
		    "D:\\research\\guierror\\subjects\\android-programs\\sourcecode\\mytracks-april-29\\MyTracks\\libs\\google-common.jar"
		    + Globals.pathSep +
		    "D:\\research\\guierror\\subjects\\android-programs\\sourcecode\\mytracks-april-29\\MyTracks\\libs\\googleloginclient-helper.jar"
		    +Globals.pathSep +
		    "D:\\research\\guierror\\subjects\\android-programs\\sourcecode\\mytracks-april-29\\MyTracksLib\\libs\\protobuf-java-2.3.0-lite.jar"
		    + Globals.pathSep + 
			"D:\\research\\guierror\\eclipsews\\GUIErrorDetector\\exp-subjects\\original-android.jar"
		    + Globals.pathSep +
		    "D:\\Java\\android-sdk-windows\\add-ons\\addon-google_apis-google_inc_-8\\libs\\maps.jar";
		return appPath;
	}

	@Override
	protected String getDirPath() {
		return "D:\\research\\guierror\\subjects\\android-programs\\sourcecode\\mytracks-april-29\\MyTracks";
	}
	
	public void testFindErrors() {
		CGTraverseGuider ui2startGuider = new CGTraverseAndroidGuider();
		CGTraverseGuider start2checkGuider = new CGTraverseExploreClientRunnableStrategy(new String[]{"com.google.android.apps.mytracks.content.TrackDataHub$6"}); 
			//new CGTraverseOnlyClientRunnableStrategy();
		try {
		    List<AnomalyCallChain> chains = super.findErrorsInAndroidApp(CG.RTA, ui2startGuider, start2checkGuider);
		    int i = 0;
		    for(AnomalyCallChain c : chains) {
		    	System.out.println("The " + i++ + "-th chain:");
			    System.out.println(c.getFullCallChainAsString());
		    }
		    Utils.dumpAnomalyCallChains(chains, "./output_chains.txt");
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
	public void testUnzip() throws IOException {
		String apkToolDir = "D:\\develop-tools\\apktool";
		String apkFile = "D:\\research\\guierror\\subjects\\android-programs\\MyTracks-1.1.11.rc1.apk";
		String extractDir = "D:\\research\\guierror\\subjects\\android-programs\\extracted\\MyTracks-1.1.11.rc1.apk";
		super.decryptXML(apkToolDir, apkFile, extractDir);
	}

}
