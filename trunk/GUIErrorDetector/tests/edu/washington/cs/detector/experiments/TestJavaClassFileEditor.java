package edu.washington.cs.detector.experiments;

import edu.washington.cs.detector.UIAnomalyDetector;
import edu.washington.cs.detector.util.Log;

public class TestJavaClassFileEditor extends AbstractSwingTest {

	//when searching the code, there is no Runnable, but there is a suspicious
    public String appPath = "D:\\research\\guierror\\subjects\\swing-programs\\javaclassfileeditor.jar";
	
	public void testJavaClassFileEditor() {
		Log.logConfig("./log.txt");
		UIAnomalyDetector.DEBUG = true;
		super.checkCallChainNumber(-1, appPath, new String[]{"classfile.", "gui.", "guihelper"
				, "visitors."});
	}
}