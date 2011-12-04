package edu.washington.cs.detector.experiments.swt;

import java.io.IOException;
import java.util.List;

import edu.washington.cs.detector.AnomalyCallChain;
import edu.washington.cs.detector.CGBuilder;
import edu.washington.cs.detector.TestCommons;
import edu.washington.cs.detector.UIAnomalyDetector;
import edu.washington.cs.detector.CGBuilder.CG;
import edu.washington.cs.detector.util.Globals;
import edu.washington.cs.detector.util.Utils;
import edu.washington.cs.detector.util.WALAUtils;
import junit.framework.TestCase;

public class TestSoLVE extends TestCase {
	/**
	 * This subject does not use thread. Its source forge project page:
	 * http://sourceforge.net/projects/solve/
	 * */
    public String appPath = "D:\\research\\guierror\\subjects\\solve.jar";
	
	public String libJar = "D:\\research\\guierror\\eclipsews\\SoLVE\\runtime.jar" +
	    Globals.pathSep + "D:\\research\\guierror\\eclipsews\\SoLVE\\jface.jar" +
	    Globals.pathSep + "D:\\research\\guierror\\eclipsews\\SoLVE\\win\\swt.jar";
	
	public void testRunningSoLVE() throws IOException {
		String path = appPath + Globals.pathSep + libJar;
        UIAnomalyDetector detector = new UIAnomalyDetector(path);
		
		CGBuilder builder = new CGBuilder(path);
		builder.setCGType(CG.OneCFA);
		builder.buildCG();
		
		WALAUtils.dumpClasses(builder.getClassHierarchy(), "./logs/loaded_classes.txt");
	    Utils.dumpCollection(WALAUtils.getUnloadedClasses(builder.getClassHierarchy(),
	    		TestCommons.getJarsFromPath(path)),  "./logs/unloaded_classes.txt");
		
		List<AnomalyCallChain> chains = detector.detectUIAnomaly(builder);
		System.out.println("Number of anomaly call chains: " + chains.size());
		
		Utils.dumpAnomalyCallChains(chains, "./logs/SoLVE-anomalies.txt");
		
		assertTrue(chains.size() == 0);
	}
}
