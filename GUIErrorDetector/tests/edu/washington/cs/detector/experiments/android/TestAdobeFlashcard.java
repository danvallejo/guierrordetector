package edu.washington.cs.detector.experiments.android;

import java.io.IOException;

import com.ibm.wala.ipa.cha.ClassHierarchyException;

import edu.washington.cs.detector.CGBuilder.CG;
import edu.washington.cs.detector.guider.CGTraverseAndroidGuider;
import edu.washington.cs.detector.guider.CGTraverseGuider;
import edu.washington.cs.detector.guider.CGTraverseOnlyClientRunnableStrategy;
import edu.washington.cs.detector.util.Globals;

/**
 * This can not be decompiled, throws StringIndexOutOfBoundsException, but
 * seems all the layout files have been extracted out yet
 * */
public class TestAdobeFlashcard extends AbstractAndroidTest {

	@Override
	protected String getAppPath() {
		String appPath = "D:\\research\\guierror\\subjects\\android-programs\\extracted\\AnkiDroid-1.1alpha6.apk\\com"
			+ Globals.pathSep +
			"D:\\research\\guierror\\subjects\\android-programs\\extracted\\AnkiDroid-1.1alpha6.apk\\org"
			+ Globals.pathSep +
			"D:\\research\\guierror\\eclipsews\\GUIErrorDetector\\exp-subjects\\original-android.jar";
		
		return appPath;
	}

	@Override
	protected String getDirPath() {
		return "D:\\research\\guierror\\subjects\\android-programs\\extracted\\AnkiDroid-1.1alpha6.apk";
	}
	
	/**
	 * FIXME Ded also fails
	 * */
	public void testAdobeFlashcard() throws ClassHierarchyException, IOException {
		CGTraverseGuider ui2startGuider = new CGTraverseAndroidGuider();
		CGTraverseGuider start2checkGuider = new CGTraverseOnlyClientRunnableStrategy();
		super.findErrorsInAndroidApp(CG.RTA, ui2startGuider, start2checkGuider);
	}

	public void testUnzip() throws IOException {
		String apkToolDir = "D:\\develop-tools\\apktool";
		String apkFile = "D:\\research\\guierror\\subjects\\android-programs\\AnkiDroid-1.1alpha6.apk";
		String extractDir = "D:\\research\\guierror\\subjects\\android-programs\\extracted\\AnkiDroid-1.1alpha6.apk";
		super.decryptXML(apkToolDir, apkFile, extractDir);
	}
}