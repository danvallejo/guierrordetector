package edu.washington.cs.detector.experiments;

import edu.washington.cs.detector.util.Globals;

public class TestAndroidAdobeReader extends AbstractAndroidTest {

	@Override
	protected String getAppPath() {
		String appPath = "D:\\research\\guierror\\subjects\\adobereader\\com\\adobe\\reader"
			+ Globals.pathSep + 
			"D:\\research\\guierror\\eclipsews\\GUIErrorDetector\\exp-subjects\\original-android.jar";
		
		return appPath;
	}

	@Override
	protected String getDirPath() {
		return "D:\\research\\guierror\\eclipsews\\TestAndroid\\";
	}

}
