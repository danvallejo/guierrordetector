package edu.washington.cs.detector.experiments.swt;

import java.io.IOException;
import java.util.List;

import com.ibm.wala.ipa.callgraph.CGNode;

import junit.framework.TestCase;

import edu.washington.cs.detector.AnomalyCallChain;
import edu.washington.cs.detector.CGBuilder;
import edu.washington.cs.detector.CallChainFilter;
import edu.washington.cs.detector.TestCommons;
import edu.washington.cs.detector.UIAnomalyDetector;
import edu.washington.cs.detector.CGBuilder.CG;
import edu.washington.cs.detector.experiments.filters.MergeSamePrefixStrategy;
import edu.washington.cs.detector.experiments.filters.MergeSamePrefixToLibCallStrategy;
import edu.washington.cs.detector.experiments.filters.MergeSameTailStrategy;
import edu.washington.cs.detector.experiments.filters.RemoveSameEntryStrategy;
import edu.washington.cs.detector.experiments.filters.RemoveSystemCallStrategy;
import edu.washington.cs.detector.guider.CGTraverseSWTGuider;
import edu.washington.cs.detector.util.Globals;
import edu.washington.cs.detector.util.Utils;
import edu.washington.cs.detector.util.WALAUtils;

public class TestJvimf extends TestCase {
public String dir = "D:\\research\\guierror\\subjects\\swt-subjects\\jvifm-src-0.12b\\";
	
    public String appPath = dir + "jvifm.jar";
    
	public String libJar =  dir + "apache-tool.jar"
	     + Globals.pathSep + dir + "swt.jar"
	     + Globals.pathSep + dir + "commons-cli-1.1.jar"
	     + Globals.pathSep + dir + "commons-io-1.4.jar"
	     + Globals.pathSep + dir + "commons-logging.jar"
	     + Globals.pathSep + dir + "commons-vfs-20070730.jar"
	     + Globals.pathSep + dir + "dom4j.jar"
	     + Globals.pathSep + dir + "jaxen-1.1-beta-9.jar"
	     + Globals.pathSep + dir + "jintellitype-1.3.1.jar"
	     + Globals.pathSep + dir + "jxgrabkey.jar";

	String[] packages = new String[]{"net.sf.jvifm"};
	
	String logFile = "./logs/jvimf-anomalies.txt";
	
	public void testJvimf() throws IOException {
		String path = appPath + Globals.pathSep + libJar;
        UIAnomalyDetector detector = new UIAnomalyDetector(path);
        
        detector.setThreadStartGuider(new CGTraverseSWTGuider());
        detector.setUIAnomalyGuider(new JvimFTraverseGuider(new String[]{"Lnet/sf/jvifm/control/MetaCommand, execute()V",
        		"Lnet/sf/jvifm/control/MiscFileCommand, execute()V"}));
		
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
		
		filter = new CallChainFilter(chains);
		chains = filter.apply(new RemoveSameEntryStrategy());
		System.out.println("No of chains after removing common heads: " + chains.size());
		
		Utils.dumpAnomalyCallChains(chains, logFile);
		
	}
}

class JvimFTraverseGuider extends CGTraverseSWTGuider {
	private String[] notVisited;
	public JvimFTraverseGuider(String[] notVisited) {
		this.notVisited = notVisited;
	}
	@Override
	public boolean traverse(CGNode src, CGNode dest) {
		if(!super.traverse(src, dest)) {
			return false;
		}
		if(this.matchExcludedSWTCalls(dest)) {
			return false;
		}
		return true;
	}
	protected boolean matchExcludedSWTCalls(CGNode dest) {
		String destStr = dest.toString();
		for(String str : notVisited) {
			if(destStr.indexOf(str) != -1) {
				return true;
			}
		}
		return false;
	}
}
