package edu.washington.cs.detector.experiments;

import java.util.List;

import edu.washington.cs.detector.AnomalyCallChain;
import edu.washington.cs.detector.CallChainFilter;
import edu.washington.cs.detector.SwingUIMethodEvaluator;
import edu.washington.cs.detector.UIAnomalyDetector;
import edu.washington.cs.detector.UIAnomalyMethodFinder;
import edu.washington.cs.detector.experiments.filters.RemoveNoClientClassStrategy;
import edu.washington.cs.detector.experiments.filters.RemoveSystemCallStrategy;
import edu.washington.cs.detector.util.Utils;
import junit.framework.TestCase;

public abstract class AbstractSwingTest extends TestCase {
	
	protected void checkCallChainNumber(int expectedNum, String appPath, String[] packages) {
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
		if(packages != null) {
		    chains = CallChainFilter.filter(chains, new RemoveNoClientClassStrategy(packages, true /*only look after*/));
		}
		System.out.println("size of chains after removing non client class chains: " + chains.size());
		
		int count = 0;
		for(AnomalyCallChain chain : chains) {
			System.out.println("The " + (count++) + "-th chain");
			System.out.println(chain.getFullCallChainAsString());
		}
		if(expectedNum > -1) {
		    assertEquals("The number of expected call chain is wrong", expectedNum, chains.size());
		}
		
		//resort the state to move dependent tests
		UIAnomalyMethodFinder.setMethodEvaluator(null);
		UIAnomalyMethodFinder.setCheckingMethods("./src/checking_methods.txt");
	}
}
