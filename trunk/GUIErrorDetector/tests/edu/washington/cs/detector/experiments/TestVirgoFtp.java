package edu.washington.cs.detector.experiments;

import java.io.IOException;
import java.util.List;

import edu.washington.cs.detector.AnomalyCallChain;
import edu.washington.cs.detector.CGBuilder;
import edu.washington.cs.detector.CallChainFilter;
import edu.washington.cs.detector.TestCommons;
import edu.washington.cs.detector.UIAnomalyDetector;
import edu.washington.cs.detector.CGBuilder.CG;
import edu.washington.cs.detector.experiments.filters.MergeSameTailStrategy;
import edu.washington.cs.detector.experiments.filters.RemoveSystemCallStrategy;
import edu.washington.cs.detector.util.Globals;
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
		
		CallChainFilter filter = new CallChainFilter(chains);
		chains = filter.apply(new RemoveSystemCallStrategy());
		System.out.println("No of chains after filtering system classes: " + chains.size());
		
		filter = new CallChainFilter(chains);
		chains = filter.apply(new MergeSameTailStrategy());
		System.out.println("No of chains after removing common tails: " + chains.size());
		
		Utils.dumpAnomalyCallChains(chains, "./logs/virgo-ftp-anomalies.txt");
		
	}
}
