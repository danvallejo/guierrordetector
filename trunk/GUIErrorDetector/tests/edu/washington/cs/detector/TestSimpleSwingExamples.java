package edu.washington.cs.detector;

import java.io.IOException;
import java.util.List;

import edu.washington.cs.detector.CGBuilder.CG;
import edu.washington.cs.detector.experiments.filters.RemoveNoClientClassStrategy;
import edu.washington.cs.detector.experiments.filters.RemoveSystemCallStrategy;
import edu.washington.cs.detector.util.Log;
import edu.washington.cs.detector.util.PDFViewer;
import edu.washington.cs.detector.util.Utils;
import edu.washington.cs.detector.util.WALAUtils;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class TestSimpleSwingExamples extends TestCase {

	public static Test suite() {
		return new TestSuite(TestSimpleSwingExamples.class);
	}
	
	public void testSwingError() throws IOException {
		String appPath = TestCommons.testfolder + "swingerror";
//		Log.logConfig("./log.txt");
//		UIAnomalyMethodFinder.DEBUG = true;
		this.checkCallChainNumber(2, appPath);
	}
	
	public void testSwingNoError() throws IOException {
		String appPath = TestCommons.testfolder + "swingnoerror";
//		Log.logConfig("./log.txt");
//		UIAnomalyMethodFinder.DEBUG = true;
		this.checkCallChainNumber(0, appPath);
	}
	
	private void checkCallChainNumber(int expectedNum, String appPath) {
		UIAnomalyDetector detector = new UIAnomalyDetector(appPath);
		
		/** a few configurations to configure the tool for swing*/
//		detector.setDefaultCGType(CG.OneCFA);
		detector.setExclusionFile(UIAnomalyDetector.EXCLUSION_FILE_SWING);
		detector.configureCheckingMethods("./tests/edu/washington/cs/detector/checkingmethods_for_swing.txt");
		detector.setAnomalyMethodEvaluator(new SwingUIMethodEvaluator());
		
		/** detect UI anomaly */
		List<AnomalyCallChain> chains = detector.detectUIAnomaly();
		
		System.out.println("size of chains before removing redundancy: " + chains.size());
		chains = Utils.removeRedundantAnomalyCallChains(chains);
		System.out.println("size of chains after removing redundancy: " + chains.size());
		chains = CallChainFilter.filter(chains, new RemoveSystemCallStrategy());
		System.out.println("size of chains after removing system calls: " + chains.size());
		chains = CallChainFilter.filter(chains, new RemoveNoClientClassStrategy(new String[]{"test."}, true /*only look after*/));
		System.out.println("size of chains after removing non client class chains: " + chains.size());
		
		int count = 0;
		for(AnomalyCallChain chain : chains) {
			System.out.println("The " + (count++) + "-th chain");
			System.out.println(chain.getFullCallChainAsString());
		}
		assertEquals("The number of expected call chain is wrong", expectedNum, chains.size());
	}
}
