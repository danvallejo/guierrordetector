package edu.washington.cs.detector.experiments.swt;

import java.io.IOException;
import java.util.List;

import edu.washington.cs.detector.AnomalyCallChain;
import edu.washington.cs.detector.CGBuilder;
import edu.washington.cs.detector.CallChainFilter;
import edu.washington.cs.detector.TestCommons;
import edu.washington.cs.detector.ThreadStartFinder;
import edu.washington.cs.detector.UIAnomalyDetector;
import edu.washington.cs.detector.CGBuilder.CG;
import edu.washington.cs.detector.experimental.InvalidThreadAccessDetector;
import edu.washington.cs.detector.experiments.filters.MergeSameEntryToStartPathStrategy;
import edu.washington.cs.detector.experiments.filters.MergeSamePrefixToLibCallStrategy;
import edu.washington.cs.detector.experiments.filters.MergeSameTailStrategy;
import edu.washington.cs.detector.experiments.filters.RemoveSubsumedChainStrategy;
import edu.washington.cs.detector.experiments.filters.RemoveSystemCallStrategy;
import edu.washington.cs.detector.guider.CGTraverseSWTGuider;
import edu.washington.cs.detector.util.Globals;
import edu.washington.cs.detector.util.Log;
import edu.washington.cs.detector.util.Utils;
import edu.washington.cs.detector.util.WALAUtils;
import junit.framework.TestCase;

public class TestVirgoFtp extends TestCase {
	
	//it generates 3 false positive:, all on methods:
	//AbstractFileTransferThread#prepareForTransfer:
	// ftp = ttt.ftpEngine.clones(new LoggerHandle(null,taskLog));
	
    public String appPath = "D:\\research\\guierror\\subjects\\virgoftp-1.3.5.jar";
	
	public String libJar = "D:\\research\\guierror\\eclipsews\\virgoftp-1.3.5\\swt.jar" +
	    Globals.pathSep + "D:\\research\\guierror\\eclipsews\\virgoftp-1.3.5\\xerces.jar";
	
	public void testRunVirgoFtp() throws IOException {
		long start = System.currentTimeMillis();
		String path = appPath + Globals.pathSep + libJar;
		
		Log.logConfig("./log.txt");
		
        UIAnomalyDetector detector = new UIAnomalyDetector(path);
		
		//the worklist based
//        InvalidThreadAccessDetector detector = new InvalidThreadAccessDetector(path);
        
        detector.setThreadStartGuider(new CGTraverseSWTGuider());
        detector.setUIAnomalyGuider(new CGTraverseSWTGuider());
		
		CGBuilder builder = new CGBuilder(path);
		builder.setCGType(CG.OneCFA);
		builder.setCGType(CG.RTA);
		builder.setCGType(CG.TempZeroCFA);
		
//		ThreadStartFinder.check_find_all_starts = true;
		
		//UIAnomalyDetector.setToUseDFS();
		
		long cgStart = System.currentTimeMillis();
		builder.buildCG();
		long cgEnd = System.currentTimeMillis();
		System.out.println("Building cg for virgo: " + (cgEnd - cgStart));
//		System.exit(1);
		
		WALAUtils.dumpClasses(builder.getClassHierarchy(), "./logs/loaded_classes.txt");
	    Utils.dumpCollection(WALAUtils.getUnloadedClasses(builder.getClassHierarchy(),
	    		TestCommons.getJarsFromPath(path)),  "./logs/unloaded_classes.txt");
		
		List<AnomalyCallChain> chains = detector.detectUIAnomaly(builder);
		System.out.println("Number of anomaly call chains: " + chains.size());
		
		CallChainFilter filter = new CallChainFilter(chains);
		chains = filter.apply(new RemoveSubsumedChainStrategy());
		System.out.println("No of chains after remvoing subsumption: " + chains.size());
		
		filter = new CallChainFilter(chains);
		chains = filter.apply(new RemoveSystemCallStrategy());
		System.out.println("No of chains after filtering system classes: " + chains.size());
		
		filter = new CallChainFilter(chains);
		chains = filter.apply(new MergeSameTailStrategy());
		System.out.println("No of chains after removing common tails: " + chains.size());
		
		filter = new CallChainFilter(chains);
		chains = filter.apply(new MergeSamePrefixToLibCallStrategy(new String[]{"edu.sysu.virgoftp"}));
		System.out.println("No of chains after removing common tails of lib calls: " + chains.size());
		
		long end = System.currentTimeMillis();
		
		System.out.println("The total time: " + (end - start));
		
		Utils.dumpAnomalyCallChains(chains, "./logs/virgo-ftp-anomalies.txt");
		
	}
}
