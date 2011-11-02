package edu.washington.cs.detector.experiments;

import java.io.IOException;
import java.util.List;

import edu.washington.cs.detector.AnomalyCallChain;
import edu.washington.cs.detector.CGBuilder;
import edu.washington.cs.detector.CallChainFilter;
import edu.washington.cs.detector.RemoveSystemCallStrategy;
import edu.washington.cs.detector.TestCommons;
import edu.washington.cs.detector.UIAnomalyDetector;
import edu.washington.cs.detector.CGBuilder.CG;
import edu.washington.cs.detector.util.Globals;
import edu.washington.cs.detector.util.Utils;
import edu.washington.cs.detector.util.WALAUtils;
import junit.framework.TestCase;

/**
 * No error found in this subject
 * */
public class TestRipSaw extends TestCase {

	public void testRipSaw() throws IOException {
		String path = 
			"D:\\research\\guierror\\subjects\\ripsaw.jar" + Globals.pathSep
			+ Utils
				.conToPath(Utils
						.getJars("C:\\Users\\szhang\\Downloads\\RSBeta93src\\Ripsaw\\lib"));

		// initialize a UI anomaly detector
		UIAnomalyDetector detector = new UIAnomalyDetector(path);
		// configure the call graph builder, use 1-CFA as default
		CGBuilder builder = new CGBuilder(path);
		builder.setCGType(CG.OneCFA);
		builder.buildCG();
		// dump debugging information
		WALAUtils.dumpClasses(builder.getClassHierarchy(),
				"./logs/loaded_classes.txt");
		Utils.dumpCollection(
				WALAUtils.getUnloadedClasses(builder.getClassHierarchy(),
						TestCommons.getJarsFromPath(path)),
				"./logs/unloaded_classes.txt");
		// finding UI anomaly chain
		List<AnomalyCallChain> chains = detector.detectUIAnomaly(builder);
		System.out.println("Number of anomaly call chains: " + chains.size());
		
		CallChainFilter filter = new CallChainFilter(chains);
		chains = filter.apply(new RemoveSystemCallStrategy());
		System.out.println("No of chains after filtering system classes: " + chains.size());
		
		Utils.dumpAnomalyCallChains(chains, "./logs/ripsaw-anomalies.txt");
		
		assertTrue(chains.size() == 0);
	}

}
