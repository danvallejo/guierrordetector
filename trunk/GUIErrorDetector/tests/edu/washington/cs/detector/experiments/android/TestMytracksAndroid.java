package edu.washington.cs.detector.experiments.android;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.cha.ClassHierarchy;

import edu.washington.cs.detector.AnomalyCallChain;
import edu.washington.cs.detector.CGBuilder;
import edu.washington.cs.detector.CGEntryManager;
import edu.washington.cs.detector.CallChainFilter;
import edu.washington.cs.detector.UIAnomalyDetector;
import edu.washington.cs.detector.UIAnomalyMethodFinder;
import edu.washington.cs.detector.CGBuilder.CG;
import edu.washington.cs.detector.experiments.filters.MergeSameEntryToStartPathStrategy;
import edu.washington.cs.detector.guider.CGTraverseAndroidGuider;
import edu.washington.cs.detector.guider.CGTraverseExploreClientRunnableStrategy;
import edu.washington.cs.detector.guider.CGTraverseGuider;
import edu.washington.cs.detector.guider.CGTraverseOnlyClientRunnableStrategy;
import edu.washington.cs.detector.util.Globals;
import edu.washington.cs.detector.util.Utils;

/**
 * the bug: http://code.google.com/p/mytracks/issues/detail?id=424
 * */
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
	
	@Override
	protected Collection<Entrypoint> getExtraEntrypoints(CGBuilder builder) {
		if(builder.getClassHierarchy() == null) {
			throw new RuntimeException("Should can makeClassHiearchyAndScope first!");
		}
		ClassHierarchy cha = builder.getClassHierarchy();
		List<String> otherClasses = new LinkedList<String>();
	    otherClasses.add("android.view.ViewRoot");
	    Iterable<Entrypoint> otherClassMethods = CGEntryManager.getAllPublicMethods(builder, otherClasses, false);
	    //the extra entrypoint collection
	    Collection<Entrypoint> extraEntrypoints = new HashSet<Entrypoint>();
	    for(Entrypoint ep : otherClassMethods) {
	    	extraEntrypoints.add(ep);
	    }
	    return extraEntrypoints;
	}
	
//	@Override
//	protected Iterable<Entrypoint> getQuerypoints(Iterable<Entrypoint> entries) {
//		Collection<Entrypoint> result = new HashSet<Entrypoint>();
//		
//		for(Entrypoint ep : entries) {
//			if(ep.toString().indexOf("com/google/android/apps/mytracks/StatsActivity, onSelectedTrackChanged") != -1) {
//				result.add(ep);
//			}
//		}
//		
//		System.err.println("Number of entry points: " + result.size());
//		Utils.logCollection(result);
//		
//		return result;
//	}
	
	public void testFindErrors() {
		CGTraverseGuider ui2startGuider = new CGTraverseAndroidGuider();
		String[] guiderArray = 
//			new String[]{"android.os.HandlerThread"};//
		    new String[]{//"com.google.android.apps.mytracks.content.TrackDataHub$6",
				"android.os.HandlerThread"
				};
		
		CGTraverseExploreClientRunnableStrategy start2checkGuider = new CGTraverseExploreClientRunnableStrategy(guiderArray); 
			//new CGTraverseOnlyClientRunnableStrategy();
		//start2checkGuider.addMethodGuidance("java.lang.Thread.start", "android.os.HandlerThread.run");
		start2checkGuider.addMethodGuidance("android.os.Handler.dispatchMessage", "android.os.Handler.handleCallback");
		start2checkGuider.addMethodGuidance("android.os.Handler.handleCallback", "com.google.android.apps.mytracks.content");
//		start2checkGuider.addMethodGuidance("android.os.Handler.handleCallback", "com.google.android.apps.mytracks.content.TrackDataHub$6");
//		UIAnomalyMethodFinder.DEBUG = true;
		
		try {
//			super.setRunnaiveApproach(true);
			super.setPackageNames(new String[]{"com.google"});
			super.setByfileName("mytracks.xml");
			
			CG type = CG.RTA;
			type = CG.FakeZeroCFA;
			type = CG.OneCFA;
			
			UIAnomalyDetector.setToUseDFS();
			
		    List<AnomalyCallChain> chains = super.findErrorsInAndroidApp(type, ui2startGuider, start2checkGuider);
//		    List<AnomalyCallChain> chains = super.findErrorsInAndroidApp(CG.OneCFA, ui2startGuider, start2checkGuider);
//		    List<AnomalyCallChain> chains = super.findErrorsInAndroidApp(CG.FakeZeroCFA, ui2startGuider, start2checkGuider);
		    
//		    int i = 0;
//		    for(AnomalyCallChain c : chains) {
//		    	System.out.println("The " + i++ + "-th chain:");
//			    System.out.println(c.getFullCallChainAsString());
//		    }
//		    Utils.dumpAnomalyCallChains(chains, "./output_chains.txt");
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
	
//	Node: < Application, Lcom/google/android/apps/mytracks/StatsActivity, onSelectedTrackChanged(Lcom/google/android/apps/mytracks/content/Track;Z)V > Context: CallStringContext: [ fakeRootMethod@357 ], line: 244
//	 -> Node: synthetic < Primordial, Ljava/lang/Thread, start()V > Context: CallStringContext: [ onSelectedTrackChanged@55 ], line: -1
//	 -> Node: < Application, Landroid/os/HandlerThread, run()V > Context: CallStringContext: [ start@1 ], line: -1
//	 -> Node: < Application, Landroid/os/Looper, loop()V > Context: CallStringContext: [ run@36 ], line: -1
//	 -> Node: < Application, Landroid/os/Handler, dispatchMessage(Landroid/os/Message;)V > Context: CallStringContext: [ loop@93 ], line: -1
//	 -> Node: < Application, Landroid/os/Handler, handleCallback(Landroid/os/Message;)V > Context: CallStringContext: [ dispatchMessage@9 ], line: -1
//	 -> Node: < Application, Lcom/google/android/apps/mytracks/content/TrackDataHub$6, run()V > Context: CallStringContext: [ handleCallback@4 ], line: 658
//	 -> Node: < Application, Lcom/google/android/apps/mytracks/StatsActivity, onCurrentLocationChanged(Landroid/location/Location;)V > Context: CallStringContext: [ run@28 ], line: 257
//	 -> Node: < Application, Lcom/google/android/apps/mytracks/StatsActivity, showLocation(Landroid/location/Location;)V > Context: CallStringContext: [ onCurrentLocationChanged@12 ], line: 221
//	 -> Node: < Application, Lcom/google/android/apps/mytracks/StatsUtilities, setLatLong(ID)V > Context: CallStringContext: [ showLocation@25 ], line: 98
//	 -> Node: < Application, Landroid/widget/TextView, setText(Ljava/lang/CharSequence;)V > Context: CallStringContext: [ setLatLong@22 ], line: -1
//	 -> Node: < Application, Landroid/widget/TextView, setText(Ljava/lang/CharSequence;Landroid/widget/TextView$BufferType;)V > Context: CallStringContext: [ setText@6 ], line: -1
//	 -> Node: < Application, Landroid/widget/TextView, setText(Ljava/lang/CharSequence;Landroid/widget/TextView$BufferType;ZI)V > Context: CallStringContext: [ setText@5 ], line: -1
//	 -> Node: < Application, Lcom/android/internal/view/menu/IconMenuItemView, onTextChanged(Ljava/lang/CharSequence;III)V > Context: CallStringContext: [ setText@689 ], line: -1
//	 -> Node: < Application, Landroid/view/View, setLayoutParams(Landroid/view/ViewGroup$LayoutParams;)V > Context: CallStringContext: [ onTextChanged@14 ], line: -1
//	 -> Node: < Application, Landroid/view/View, requestLayout()V > Context: CallStringContext: [ setLayoutParams@21 ], line: -1
//	 -> Node: < Application, Landroid/view/ViewRoot, requestLayout()V > Context: CallStringContext: [ requestLayout@35 ], line: -1
//	 -> Node: < Application, Landroid/view/ViewRoot, checkThread()V > Context: CallStringContext: [ requestLayout@1 ], line: -1

//	Number of chains: 1176
//	Number of chains after removing subsumption: 441
//	Number of chains after removing redundant: 441
//	size of chains after removing system calls: 441
//	size of chains after removing same entry nodes to lib: 63
//	size of chains after merging the same tails: 1
//	Final output chains: 1

}
