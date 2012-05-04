package edu.washington.cs.detector.experiments.android;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;

import edu.washington.cs.detector.AnomalyCallChain;
import edu.washington.cs.detector.CGBuilder;
import edu.washington.cs.detector.CGEntryManager;
import edu.washington.cs.detector.CallChainFilter;
import edu.washington.cs.detector.CGBuilder.CG;
import edu.washington.cs.detector.NativeMethodConnector;
import edu.washington.cs.detector.ThreadStartFinder;
import edu.washington.cs.detector.UIAnomalyDetector;
import edu.washington.cs.detector.experiments.filters.RemoveNoClientClassStrategy;
import edu.washington.cs.detector.experiments.filters.RemoveNonClientHeadStrategy;
import edu.washington.cs.detector.guider.CGTraverseAndroidGuider;
import edu.washington.cs.detector.guider.CGTraverseAndroidSafeMethodGuider;
import edu.washington.cs.detector.guider.CGTraverseExploreClientRunnableStrategy;
import edu.washington.cs.detector.guider.CGTraverseGuider;
import edu.washington.cs.detector.util.Globals;
import edu.washington.cs.detector.util.NativeAnnotationProcessor;
import edu.washington.cs.detector.util.Utils;
import edu.washington.cs.detector.util.WALAUtils;

/**
 * Here is how the bug happens:
 * 
 * (checkthread, and then lead to the bug)
 * 
 * android.inputmethodservice.KeyboardView.setShifted(KeyboardView.java:435)
 * name.boyle.chris.sgtpuzzles.SmallKeyboard$KeyboardModel.setUndoRedoEnabled(SmallKeyboard.java:129)
 * name.boyle.chris.sgtpuzzles.SmallKeyboard.setUndoRedoEnabled(SmallKeyboard.java:189)
 * name.boyle.chris.sgtpuzzles.SGTPuzzles.changedState(SGTPuzzles.java:955)
 * 
 * (native method: native void init(GameView _gameView, int whichGame, String gameState);)
 * 
 * init(gameView, which, savedGame)
 * SGTPuzzles$9.run();
 * thread.start()
 * SGTPuzzles#startGame(final int which, final String savedGame)
 * SGTPuzzles#onNewIntent()
 * */

public class TestSGTPuzzle extends AbstractAndroidTest {

	private String puzzleJar = "D:\\research\\guierror\\subjects\\android-programs\\extracted\\sgtpuzzles.jar";
	
	@Override
	protected String getAppPath() {
		String appPath = 
			puzzleJar + Globals.pathSep +
			"D:\\research\\guierror\\eclipsews\\GUIErrorDetector\\exp-subjects\\original-android.jar";
		
		return appPath;
	}

	@Override
	protected String getDirPath() {
		return "D:\\research\\guierror\\subjects\\android-programs\\extracted\\SGTPuzzles-9306-7.apk";
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
	
	public void testFindErrors() throws ClassHierarchyException, IOException {
		CGTraverseGuider ui2startGuider = new CGTraverseAndroidGuider();
		CGTraverseExploreClientRunnableStrategy start2checkGuider
		= new  CGTraverseExploreClientRunnableStrategy(new String[]{});
		//"name.boyle.chris.sgtpuzzles"
//		start2checkGuider.addMethodGuidance("java.lang.Thread.start", "name.boyle.chris.sgtpuzzles.SGTPuzzles$9");
		start2checkGuider.addExclusionGuidance("android.webkit");
		start2checkGuider.addExclusionGuidance("android.os.HandlerThread");
		
		ThreadStartFinder.check_find_all_starts = true;
		
		//parse the native annotation
		NativeMethodConnector connector = new NativeMethodConnector();
		Collection<String[]> pairs = getAllPairs();
		for(String[] pair : pairs) {
			connector.addNativeMethodMapping(pair[0], pair[1]);
		}
		
		//only this rule is useful, the remaining is for completeness
		//the following annotation information is actually read from the @CalledByNativeMethods
		//for debugging purpose in this experiment, I hard code these parts, 7 annotations in total
//		connector.addNativeMethodMapping(
//				"name.boyle.chris.sgtpuzzles.SGTPuzzles.init",
//				"name.boyle.chris.sgtpuzzles.SGTPuzzles.changedState");
//		connector.addNativeMethodMapping(
//				"name.boyle.chris.sgtpuzzles.SGTPuzzles.init",
//				"name.boyle.chris.sgtpuzzles.SGTPuzzles.gameStarted");
//		connector.addNativeMethodMapping(
//				"name.boyle.chris.sgtpuzzles.SGTPuzzles.init",
//				"name.boyle.chris.sgtpuzzles.SGTPuzzles.addTypeItem");
//		connector.addNativeMethodMapping(
//				"name.boyle.chris.sgtpuzzles.SGTPuzzles.configEvent",
//				"name.boyle.chris.sgtpuzzles.SGTPuzzles.dialogAdd");
//		connector.addNativeMethodMapping(
//				"name.boyle.chris.sgtpuzzles.SGTPuzzles.configEvent",
//				"name.boyle.chris.sgtpuzzles.SGTPuzzles.dialogShow");
//		connector.addNativeMethodMapping(
//				"name.boyle.chris.sgtpuzzles.SGTPuzzles.solveEvent",
//				"name.boyle.chris.sgtpuzzles.SGTPuzzles.messageBox");
//		connector.addNativeMethodMapping(
//				"name.boyle.chris.sgtpuzzles.SGTPuzzles.presetEvent",
//				"name.boyle.chris.sgtpuzzles.SGTPuzzles.tickTypeItem");
		
		try {
//		  super.setRunnaiveApproach(true);
		  super.setPackageNames(new String[]{"name.boyle"});
			//this finds bugs
//		  List<AnomalyCallChain> chains = super.findErrorsInAndroidApp(CG.RTA, ui2startGuider, start2checkGuider, connector);
		  CG type = CG.TempZeroCFA;
//		  type = CG.OneCFA;
//		  type = CG.RTA;
		  
//		  super.setExhaustiveSearch(true);
//		  UIAnomalyDetector.setToUseDFS();
		
		  super.setAndroidCheckingFile("./tests/edu/washington/cs/detector/checkingmethods_for_android_extra.txt");
		  super.setByfileName("sgtpuzzle.xml");
		  
		  long startT = System.currentTimeMillis();
		  List<AnomalyCallChain> chains = super.findErrorsInAndroidApp(type, ui2startGuider, start2checkGuider, connector);
		  long endT = System.currentTimeMillis();
		  
		  System.out.println("The total time cost: " + (endT - startT));
		  
		  int i = 0;
		    for(AnomalyCallChain c : chains) {
		    	System.out.println("The " + i++ + "-th chain:");
			    System.out.println(c.getFullCallChainAsString());
		    }
		    System.out.println("Number of chains: " + chains.size());
		    Utils.dumpAnomalyCallChains(chains, "./output_chains.txt");
		  
		} catch (Throwable e) {
			e.printStackTrace();
		}
		
		//see the unloaded class
		Set<String> unloadedClasses = WALAUtils.getUnloadedClasses(builtCHA, puzzleJar);
		System.out.println("Number of unloaded classes: " + unloadedClasses.size());
		for(String unloadedClass : unloadedClasses) {
			System.out.println("  - " + unloadedClass);
		}
	}

	public void testUnzip() throws IOException {
		String apkToolDir = "D:\\develop-tools\\apktool";
		String apkFile = "D:\\research\\guierror\\subjects\\android-programs\\SGTPuzzles-9306-7.apk";
		String extractDir = "D:\\research\\guierror\\subjects\\android-programs\\extracted\\SGTPuzzles-9306-7.apk";
		super.decryptXML(apkToolDir, apkFile, extractDir);
	}
	
	private Collection<String[]> getAllPairs() {
		String jarFileName = "D:\\research\\guierror\\subjects\\android-programs\\extracted\\sgtpuzzles-annotation.jar";
		
		String lib = "D:\\research\\guierror\\eclipsews\\GUIErrorDetector\\exp-subjects\\original-android.jar";
		try {
			Collection<String[]> pairs = NativeAnnotationProcessor.getAllCallingPairsByNativeAnnotations(jarFileName, lib);
			return pairs;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
