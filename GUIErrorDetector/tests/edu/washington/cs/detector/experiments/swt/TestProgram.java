package edu.washington.cs.detector.experiments.swt;

import java.io.IOException;
import java.util.List;

import edu.washington.cs.detector.AnomalyCallChain;
import edu.washington.cs.detector.CGBuilder;
import edu.washington.cs.detector.CallChainFilter;
import edu.washington.cs.detector.TestCommons;
import edu.washington.cs.detector.UIAnomalyDetector;
import edu.washington.cs.detector.CGBuilder.CG;
import edu.washington.cs.detector.experiments.filters.MergeSamePrefixToLibCallStrategy;
import edu.washington.cs.detector.experiments.filters.MergeSameTailStrategy;
import edu.washington.cs.detector.experiments.filters.RemoveSystemCallStrategy;
import edu.washington.cs.detector.guider.CGTraverseSWTGuider;
import edu.washington.cs.detector.util.Globals;
import edu.washington.cs.detector.util.Log;
import edu.washington.cs.detector.util.Utils;
import edu.washington.cs.detector.util.WALAUtils;
import junit.framework.TestCase;

public class TestProgram extends TestCase {

public String appPath = "D:\\research\\guierror\\subjects\\swt-subjects\\oast\\oast.jar";

public String dir
    = "D:\\research\\guierror\\eclipsews\\crazyguyonbike\\libs\\";

//String swtdir = "D:\\develop-tools\\eclipse\\eclipse\\plugins\\org.eclipse.swt.win32.win32.x86_64.source_3.6.2.v3659c.jar";

public String libJar =
	"D:\\research\\guierror\\eclipsews\\oast\\commons-lang-2.6.jar"
	+ Globals.pathSep
	+ "D:\\research\\guierror\\eclipsews\\oast\\swt-3.6.1-win32-win32-x86_64.jar";



String[] packages = new String[]{"checks", "com.maxmind", "lm", "oast"};

String logFile = "./logs/anomalies.txt";

public void testFindingErrors() throws IOException {
	Log.logConfig("./log.txt");
	String path = appPath + Globals.pathSep + libJar;
    UIAnomalyDetector detector = new UIAnomalyDetector(path);
    
    detector.setThreadStartGuider(new CGTraverseSWTGuider());
    detector.setUIAnomalyGuider(new CGTraverseSWTGuider());
	
	CGBuilder builder = new CGBuilder(path);
	builder.setCGType(CG.OneCFA);
//	builder.setCGType(CG.ZeroCFA);
//	builder.setCGType(CG.RTA);
//	builder.setCGType(CG.FakeZeroCFA);
	
	//UIAnomalyDetector.setToUseDFS();
	UIAnomalyDetector.DEBUG = true;
	
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
	
//	filter = new CallChainFilter(chains);
//	chains = filter.apply(new RemoveSameEntryStrategy());
//	System.out.println("No of chains after removing common heads: " + chains.size());
	
	Utils.dumpAnomalyCallChains(chains, logFile);
	
}

}
