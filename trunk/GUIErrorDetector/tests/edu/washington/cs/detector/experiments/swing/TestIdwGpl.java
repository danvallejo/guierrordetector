package edu.washington.cs.detector.experiments.swing;

import java.io.IOException;

import com.ibm.wala.ipa.cha.ClassHierarchyException;

import edu.washington.cs.detector.UIAnomalyDetector;
import edu.washington.cs.detector.UIAnomalyMethodFinder;
import edu.washington.cs.detector.CGBuilder.CG;
import edu.washington.cs.detector.util.Log;

//a lot of thread
//suspicous: optimizeAfter, and runBatch - they are actually not thread at all
//verified, no error!
public class TestIdwGpl extends AbstractSwingTest {
	public String appPath
        = "D:\\research\\guierror\\subjects\\swing-programs\\idw-gpl-1.6.1.jar";

    public void testJReversePro() throws IOException, ClassHierarchyException {
    	//UIAnomalyMethodFinder.DEBUG = true;
    	Log.logConfig("./log.txt");
    	UIAnomalyDetector.DEBUG = true;
    	
    	super.setCGType(CG.RTA);
    	super.setCGType(CG.ZeroCFA);
    	super.setCGType(CG.OneCFA);
    	super.setNondefaultCG(true);
    	super.setAddHandlers(true);
    	
	    super.checkCallChainNumber(-1, appPath, new String[]{"net.infonode."});
    }
}
