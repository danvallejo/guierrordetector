package edu.washington.cs.detector.experiments.android;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import com.ibm.wala.ipa.cha.ClassHierarchyException;

import edu.washington.cs.detector.AnomalyCallChain;
import edu.washington.cs.detector.CallChainFilter;
import edu.washington.cs.detector.CGBuilder.CG;
import edu.washington.cs.detector.NativeMethodConnector;
import edu.washington.cs.detector.experiments.filters.RemoveNoClientClassStrategy;
import edu.washington.cs.detector.experiments.filters.RemoveNonClientHeadStrategy;
import edu.washington.cs.detector.guider.CGTraverseAndroidGuider;
import edu.washington.cs.detector.guider.CGTraverseAndroidSafeMethodGuider;
import edu.washington.cs.detector.guider.CGTraverseExploreClientRunnableStrategy;
import edu.washington.cs.detector.guider.CGTraverseGuider;
import edu.washington.cs.detector.util.Globals;
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
	
	public void testFindErrors() throws ClassHierarchyException, IOException {
		CGTraverseGuider ui2startGuider = new CGTraverseAndroidGuider();
		CGTraverseExploreClientRunnableStrategy start2checkGuider = new  CGTraverseExploreClientRunnableStrategy(new String[]{"name.boyle.chris.sgtpuzzles"});
		start2checkGuider.addMethodGuidance("java.lang.Thread.start", "name.boyle.chris.sgtpuzzles.SGTPuzzles$9");
			//new CGTraverseAndroidSafeMethodGuider();
//		    new CGTraverseOnlyClientRunnableStrategy();
		
		NativeMethodConnector connector = new NativeMethodConnector();
		connector.addNativeMethodMapping("name.boyle.chris.sgtpuzzles.SGTPuzzles.init",
				"name.boyle.chris.sgtpuzzles.SGTPuzzles.changedState");
		
		try {
		  List<AnomalyCallChain> chains = super.findErrorsInAndroidApp(CG.RTA, ui2startGuider, start2checkGuider, connector);
		  
		  int count = 0;
		  for(AnomalyCallChain chain : chains) {
			 System.out.println("The " + count++ + "-th call chain:");
			 System.out.println(chain.getFullCallChainAsString());
		  }
		  
		  System.out.println("Before filtering: " + chains.size());
		  
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
}
