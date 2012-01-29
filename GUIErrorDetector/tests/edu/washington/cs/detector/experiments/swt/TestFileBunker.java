package edu.washington.cs.detector.experiments.swt;

import java.io.IOException;
import java.util.List;

import edu.washington.cs.detector.AnomalyCallChain;
import edu.washington.cs.detector.CGBuilder;
import edu.washington.cs.detector.CallChainFilter;
import edu.washington.cs.detector.TestCommons;
import edu.washington.cs.detector.UIAnomalyDetector;
import edu.washington.cs.detector.CGBuilder.CG;
import edu.washington.cs.detector.experiments.filters.MergeSameEntryToStartPathStrategy;
import edu.washington.cs.detector.experiments.filters.MergeSameTailStrategy;
import edu.washington.cs.detector.experiments.filters.RemoveContainingNodeStrategy;
import edu.washington.cs.detector.experiments.filters.RemoveSystemCallStrategy;
import edu.washington.cs.detector.guider.CGTraverseSWTGuider;
import edu.washington.cs.detector.util.Globals;
import edu.washington.cs.detector.util.Log;
import edu.washington.cs.detector.util.Utils;
import edu.washington.cs.detector.util.WALAUtils;
import junit.framework.TestCase;

public class TestFileBunker extends TestCase {

	/**
	 * Few possible bugs:
	 * BackupController#new ProgressMonitorDialog(shell).run(true, true, performBackup);
	 * 
	 * The filtering heuristic is quite effective:
	 *  - Number of anomaly call chains: 44550
     *  - No of chains after filtering system classes: 4063
     *  - No of chains after removing common tails: 489
     *  - No of chains after filtering dispose(): 188
     *  - No of chains after removing same entry to start path: 1
	 * */
	public void testFileBunker() throws IOException {
		String path = Utils.conToPath(Utils.getJars("C:\\Users\\szhang\\Downloads\\FileBunker-1.1.2-win32\\FileBunker-1.1.2-win32\\Resources\\lib"))
		    + Globals.pathSep + "C:\\Users\\szhang\\Downloads\\FileBunker-1.1.2-win32\\FileBunker-1.1.2-win32\\Resources\\lib\\win32\\swt.jar";
		
		Log.logConfig("./log.txt");
		
		UIAnomalyDetector.DEBUG = true;
		
		//initialize a UI anomaly detector
        UIAnomalyDetector detector = new UIAnomalyDetector(path);
        
        detector.setThreadStartGuider(new CGTraverseSWTGuider());
        detector.setUIAnomalyGuider(new CGTraverseSWTGuider());
        
		//configure the call graph builder, use 1-CFA as default
		CGBuilder builder = new CGBuilder(path);
		builder.setCGType(CG.OneCFA);
//		builder.setCGType(CG.RTA);
//		builder.setCGType(CG.ZeroCFA);
//		builder.setCGType(CG.FakeZeroCFA);
		
		UIAnomalyDetector.setToUseDFS();
		
		builder.buildCG();
		//dump debugging information
		WALAUtils.dumpClasses(builder.getClassHierarchy(), "./logs/loaded_classes.txt");
	    Utils.dumpCollection(WALAUtils.getUnloadedClasses(builder.getClassHierarchy(),
	    		TestCommons.getJarsFromPath(path)),  "./logs/unloaded_classes.txt");
		//finding UI anomaly chain
		List<AnomalyCallChain> chains = detector.detectUIAnomaly(builder);
		System.out.println("Number of anomaly call chains: " + chains.size());
		
		CallChainFilter filter = new CallChainFilter(chains);
		chains = filter.apply(new RemoveContainingNodeStrategy("Lorg/eclipse/swt/graphics/Device, dispose()V"));
		System.out.println("No of chains after filtering dispose(): " + chains.size());
		
		 filter = new CallChainFilter(chains);
		chains = filter.apply(new RemoveSystemCallStrategy());
		System.out.println("No of chains after filtering system classes: " + chains.size());
		
		filter = new CallChainFilter(chains);
		chains = filter.apply(new MergeSameTailStrategy());
		System.out.println("No of chains after removing common tails: " + chains.size());
//		
//		filter = new CallChainFilter(chains);
//		chains = filter.apply(new RemoveContainingNodeStrategy("Lorg/eclipse/swt/graphics/Device, dispose()V"));
//		System.out.println("No of chains after filtering dispose(): " + chains.size());
		
		filter = new CallChainFilter(chains);
		chains = filter.apply(new MergeSameEntryToStartPathStrategy());
		System.out.println("No of chains after removing same entry to start path: " + chains.size());
		
		Utils.dumpAnomalyCallChains(chains, "./logs/file-bunker-anomalies.txt");
	}
	
}