package edu.washington.cs.detector.experiments;

import java.io.IOException;

import com.ibm.wala.ipa.cha.ClassHierarchyException;

import edu.washington.cs.detector.CGBuilder.CG;
import edu.washington.cs.detector.guider.CGTraverseGuider;
import edu.washington.cs.detector.guider.CGTraverseNoSystemCalls;
import edu.washington.cs.detector.guider.CGTraverseOnlyClientRunnableStrategy;
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

	public void testAdobleReader() throws ClassHierarchyException, IOException {
		CGTraverseGuider ui2startGuider = new CGTraverseNoSystemCalls();
		CGTraverseGuider start2checkGuider = new CGTraverseOnlyClientRunnableStrategy();
		super.findErrorsInAndroidApp(CG.RTA, ui2startGuider, start2checkGuider);
	}
}
