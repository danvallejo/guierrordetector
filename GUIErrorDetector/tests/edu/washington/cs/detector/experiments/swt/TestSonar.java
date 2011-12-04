package edu.washington.cs.detector.experiments.swt;

import java.io.IOException;
import java.util.List;

import edu.washington.cs.detector.AnomalyCallChain;
import edu.washington.cs.detector.CGBuilder;
import edu.washington.cs.detector.CallChainFilter;
import edu.washington.cs.detector.SWTAppUIErrorMain;
import edu.washington.cs.detector.UIAnomalyDetector;
import edu.washington.cs.detector.CGBuilder.CG;
import edu.washington.cs.detector.experiments.filters.RemoveSystemCallStrategy;
import edu.washington.cs.detector.util.Globals;
import edu.washington.cs.detector.util.Utils;
import junit.framework.TestCase;

public class TestSonar extends TestCase {
	
	/**
	 * No bugs found
	 * */
	public void testSonar() throws IOException {
		String appPath = "D:\\research\\guierror\\subjects\\sonar.jar"
			+ Globals.pathSep +  SWTAppUIErrorMain.swtJar
			+ Globals.pathSep + "D:\\research\\guierror\\eclipsews\\Sonar\\mail.jar"
			+ Globals.pathSep + "D:\\research\\guierror\\eclipsews\\Sonar\\htmlparser.jar"
			+ Globals.pathSep + "D:\\research\\guierror\\eclipsews\\Sonar\\htmllexer.jar";;
		UIAnomalyDetector detector = new UIAnomalyDetector(appPath);
		
		CGBuilder builder = new CGBuilder(appPath);
		builder.setCGType(CG.OneCFA);
		builder.buildCG();
		
		List<AnomalyCallChain> chains = detector.detectUIAnomaly(builder);
		
		System.out.println("Number of anomaly call chains: " + chains.size());
		
		CallChainFilter filter = new CallChainFilter(chains);
		chains = filter.apply(new RemoveSystemCallStrategy());
		System.out.println("No of chains after filtering system classes: " + chains.size());
		
		Utils.dumpAnomalyCallChains(chains, "./logs/sonar-anomalies.txt");
		
		assertTrue(chains.size() == 0);
		
	}
}
