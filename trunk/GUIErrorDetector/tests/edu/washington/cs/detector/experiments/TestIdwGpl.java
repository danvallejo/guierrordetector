package edu.washington.cs.detector.experiments;

import edu.washington.cs.detector.UIAnomalyMethodFinder;
import edu.washington.cs.detector.util.Log;

public class TestIdwGpl extends AbstractSwingTest {
	public String appPath
        = "D:\\research\\guierror\\subjects\\swing-programs\\idw-gpl-1.6.1.jar";

    public void testJReversePro() {
    	//UIAnomalyMethodFinder.DEBUG = true;
    	//Log.logConfig("./log.txt");
	    super.checkCallChainNumber(-1, appPath, new String[]{"net.infonode."});
    }
}
