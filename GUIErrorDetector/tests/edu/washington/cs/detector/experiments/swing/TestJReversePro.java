package edu.washington.cs.detector.experiments.swing;

import java.io.IOException;

import com.ibm.wala.ipa.cha.ClassHierarchyException;

import edu.washington.cs.detector.UIAnomalyDetector;
import edu.washington.cs.detector.CGBuilder.CG;
import edu.washington.cs.detector.util.Log;

//do not waste time, it is single threaded
public class TestJReversePro extends AbstractSwingTest {
    public String appPath
        = "D:\\research\\guierror\\subjects\\swing-programs\\jreversepro.jar";
	
	public void testJReversePro() throws IOException, ClassHierarchyException {
		Log.logConfig("./log.txt");
    	UIAnomalyDetector.DEBUG = true;
    	
    	super.setCGType(CG.RTA);
    	
    	super.setNondefaultCG(true);
    	super.setAddHandlers(true);
    	
		super.checkCallChainNumber(0, appPath, new String[]{"jreversepro"});
	}
}
