package edu.washington.cs.detector.experiments.android;

import java.io.IOException;

import com.ibm.wala.ipa.cha.ClassHierarchyException;

import edu.washington.cs.detector.CGBuilder.CG;
import edu.washington.cs.detector.guider.CGTraverseAndroidGuider;
import edu.washington.cs.detector.guider.CGTraverseGuider;
import edu.washington.cs.detector.guider.CGTraverseOnlyClientRunnableStrategy;
import edu.washington.cs.detector.util.Globals;

public class TestAdobePDFViewer extends AbstractAndroidTest {

	@Override
	protected String getAppPath() {
		String appPath = "D:\\research\\guierror\\subjects\\android-programs\\extracted\\apv-0.3.1dev10.apk\\cx\\hell\\android"
			+ Globals.pathSep + 
			"D:\\research\\guierror\\eclipsews\\GUIErrorDetector\\exp-subjects\\original-android.jar";
		
		return appPath;
	}

	@Override
	protected String getDirPath() {
		return "D:\\research\\guierror\\subjects\\android-programs\\extracted\\apv-0.3.1dev10.apk";
	}
	
	/**
	 * Has thread, but nothing found
	 * */
	public void testAdoblePDFViewer() throws ClassHierarchyException, IOException {
		CGTraverseGuider ui2startGuider = new CGTraverseAndroidGuider();
		CGTraverseGuider start2checkGuider = new CGTraverseOnlyClientRunnableStrategy();
		super.findErrorsInAndroidApp(CG.RTA, ui2startGuider, null);
	}
	
	public void testUnzip() throws IOException {
		String apkToolDir = "D:\\develop-tools\\apktool";
		String apkFile = "D:\\research\\guierror\\subjects\\android-programs\\apv-0.3.1dev10.apk";
		String extractDir = "D:\\research\\guierror\\subjects\\android-programs\\extracted\\apv-0.3.1dev10.apk";
		super.decryptXML(apkToolDir, apkFile, extractDir);
	}
}
