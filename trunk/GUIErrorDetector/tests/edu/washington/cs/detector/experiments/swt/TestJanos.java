package edu.washington.cs.detector.experiments.swt;

import java.io.IOException;
import java.util.List;

import junit.framework.TestCase;
import edu.washington.cs.detector.AnomalyCallChain;
import edu.washington.cs.detector.CGBuilder;
import edu.washington.cs.detector.CallChainFilter;
import edu.washington.cs.detector.TestCommons;
import edu.washington.cs.detector.UIAnomalyDetector;
import edu.washington.cs.detector.CGBuilder.CG;
import edu.washington.cs.detector.experiments.filters.MergeSamePrefixToLibCallStrategy;
import edu.washington.cs.detector.experiments.filters.MergeSameTailStrategy;
import edu.washington.cs.detector.experiments.filters.RemoveSameEntryStrategy;
import edu.washington.cs.detector.experiments.filters.RemoveSystemCallStrategy;
import edu.washington.cs.detector.guider.CGTraverseSWTGuider;
import edu.washington.cs.detector.util.Globals;
import edu.washington.cs.detector.util.Utils;
import edu.washington.cs.detector.util.WALAUtils;

public class TestJanos extends TestCase {
    public String dir = "D:\\research\\guierror\\subjects\\swt-subjects\\janos\\janos-exp\\";
	
    public String appPath = dir + "janos.jar";
    
	public String libJar =  dir + "commons-jxpath-1.1.jar"
	     + Globals.pathSep + dir + "swt.jar"
	     + Globals.pathSep + dir + "commons-lang-2.3.jar"
	     + Globals.pathSep + dir + "commons-logging.jar"
	     + Globals.pathSep + dir + "commons-logging-api.jar"
	     + Globals.pathSep + dir + "joda-time-1.5.2.jar"
	     + Globals.pathSep + dir + "log4j-1.2.15.jar"
	     + Globals.pathSep + dir + "mx4j-impl.jar"
	     + Globals.pathSep + dir + "mx4j-jmx.jar"
	     + Globals.pathSep + dir + "mx4j-remote.jar"
	     + Globals.pathSep + dir + "mx4j-tools.jar"
	     + Globals.pathSep + dir + "sbbi-jmx-1.0.jar"
	     + Globals.pathSep + dir + "sbbi-upnplib-1.0.4.jar";

	String[] packages = new String[]{"net.sf.janos", "net.sbbi"};
	
	String logFile = "./logs/janos-anomalies.txt";
	
	public void testJanos() throws IOException {
		String path = appPath + Globals.pathSep + libJar;
        UIAnomalyDetector detector = new UIAnomalyDetector(path);
        
        detector.setThreadStartGuider(new CGTraverseSWTGuider());
        detector.setUIAnomalyGuider(new CGTraverseSWTGuider());
		
		CGBuilder builder = new CGBuilder(path);
		builder.setCGType(CG.OneCFA);
//		builder.setCGType(CG.RTA);
//		builder.setCGType(CG.FakeZeroCFA);
		
		//UIAnomalyDetector.setToUseDFS();
		
		builder.buildCG();
		
		WALAUtils.dumpClasses(builder.getClassHierarchy(), "./logs/loaded_classes.txt");
	    Utils.dumpCollection(WALAUtils.getUnloadedClasses(builder.getClassHierarchy(),
	    		TestCommons.getJarsFromPath(path)),  "./logs/unloaded_classes.txt");
		
		List<AnomalyCallChain> chains = detector.detectUIAnomaly(builder);
		System.out.println("Number of anomaly call chains: " + chains.size());
		
		CallChainFilter filter = new CallChainFilter(chains);
		chains = filter.apply(new RemoveSystemCallStrategy());
		System.out.println("No of chains after filtering system classes: " + chains.size());
		
		filter = new CallChainFilter(chains);
		chains = filter.apply(new MergeSameTailStrategy());
		System.out.println("No of chains after removing common tails: " + chains.size());
		
		filter = new CallChainFilter(chains);
		chains = filter.apply(new MergeSamePrefixToLibCallStrategy(packages));
		System.out.println("No of chains after removing common tails of lib calls: " + chains.size());
		
//		filter = new CallChainFilter(chains);
//		chains = filter.apply(new RemoveSameEntryStrategy());
//		System.out.println("No of chains after removing common heads: " + chains.size());
		
		Utils.dumpAnomalyCallChains(chains, logFile);
		
	}
}
