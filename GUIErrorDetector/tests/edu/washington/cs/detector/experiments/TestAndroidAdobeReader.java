package edu.washington.cs.detector.experiments;

import java.io.IOException;

import com.ibm.wala.ipa.cha.ClassHierarchyException;

import edu.washington.cs.detector.util.Globals;

public class TestAndroidAdobeReader extends AbstractAndroidTest {
	public void testAdobeReader() throws ClassHierarchyException, IOException {
		String appPath = "D:\\research\\guierror\\subjects\\adobereader\\com\\adobe\\reader"
			+ Globals.pathSep + 
			"D:\\research\\guierror\\eclipsews\\GUIErrorDetector\\exp-subjects\\original-android.jar";
		
		super.checkCallChainNumber(0, appPath, "D:\\research\\guierror\\eclipsews\\TestAndroid\\res\\layout\\main.xml", new String[]{"com.adobe.reader.AdobeReader"});
	}

}
