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
import edu.washington.cs.detector.util.Utils;
import edu.washington.cs.detector.util.WALAUtils;
import junit.framework.TestCase;

public class TestZekr extends TestCase {
    public String dir = "D:\\research\\guierror\\subjects\\swt-subjects\\zekr-exp\\";
	
    public String appPath = dir + "zekr.jar";
    
	public String libJar = dir + "basicplayer-3.0.jar"
	   + Globals.pathSep + dir + "commons-codec-1.3.jar"
	   + Globals.pathSep + dir + "commons-collections-3.2.1.jar"
	   + Globals.pathSep + dir + "commons-configuration-1.6.jar"
	   + Globals.pathSep + dir + "commons-io-1.4.jar"
	   + Globals.pathSep + dir + "commons-lang-2.4.jar"
	   + Globals.pathSep + dir + "commons-logging-1.0.4.jar"
	   + Globals.pathSep + dir + "jlayer-1.0.1.jar"
	   + Globals.pathSep + dir + "jorbis-0.0.17.jar"
	   + Globals.pathSep + dir + "jspeex-0.9.7.jar"
	   + Globals.pathSep + dir + "log4j-1.2.8.jar"
	   + Globals.pathSep + dir + "lucene-core-3.0.0.jar"
	   + Globals.pathSep + dir + "lucene-highlighter-3.0.0.jar"
	   + Globals.pathSep + dir + "lucene-memory-3.0.0.jar"
	   + Globals.pathSep + dir + "lucene-misc-3.0.0.jar"
	   + Globals.pathSep + dir + "lucene-snowball-3.0.0.jar"
	   + Globals.pathSep + dir + "mp3spi-1.9.4.jar"
	   + Globals.pathSep + dir + "swt.jar"
	   + Globals.pathSep + dir + "tritonus-jorbis-0.3.6.jar"
	   + Globals.pathSep + dir + "tritonus-share-0.3.6.jar"
	   + Globals.pathSep + dir + "velocity-1.6.2.jar"
	   + Globals.pathSep + dir + "vorbisspi-1.0.3.jar";
	    
	
	public void testZekr() throws IOException {
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
		chains = filter.apply(new MergeSamePrefixToLibCallStrategy(new String[]{"net.sf.zekr"}));
		System.out.println("No of chains after removing common tails of lib calls: " + chains.size());
		
		Utils.dumpAnomalyCallChains(chains, "./logs/zekr-anomalies.txt");
		
	}
}
