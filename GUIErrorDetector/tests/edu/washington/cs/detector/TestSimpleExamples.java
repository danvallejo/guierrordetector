package edu.washington.cs.detector;

import java.util.List;

import edu.washington.cs.detector.util.Log;

import junit.framework.TestCase;

public class TestSimpleExamples extends TestCase {
	
	public void testThreadError() {
		String appPath = TestCommons.testfolder + "threaderror" + ";" +  SWTAppUIErrorMain.swtJar;
		this.checkCallChainNumber(1, appPath);
	}
	
	public void testSyncNoError() {
		String appPath = TestCommons.testfolder + "syncnoerror" + ";" +  SWTAppUIErrorMain.swtJar;
		this.checkCallChainNumber(0, appPath);
	}
	
	public void testTimerError() {
		String appPath = TestCommons.testfolder + "timererror" + ";" +  SWTAppUIErrorMain.swtJar;
		this.checkCallChainNumber(1, appPath);
	}
	
	//XXX FIXME see the report, there is one redundant
	//should figure out how to remove redundant!
	public void testThreadInsideAsync() {
		Log.logConfig("./sampleprogram/test/threadinasync/report.txt");
		String appPath = TestCommons.testfolder + "threadinasync" + ";" +  SWTAppUIErrorMain.swtJar;
		this.checkCallChainNumber(2, appPath);
	}

	private void checkCallChainNumber(int expectedNum, String appPath) {
		UIAnomalyDetector detector = new UIAnomalyDetector(appPath);
		List<AnomalyCallChain> chains = detector.detectUIAnomaly();
		assertEquals("The number of expected call chain is wrong", expectedNum, chains.size());
	}
	
}