package edu.washington.cs.detector;

import java.util.List;

import edu.washington.cs.detector.util.Globals;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class TestSWTExamples extends TestCase {
	
	public static Test suite() {
		return new TestSuite(TestSWTExamples.class);
	}
	
    public static final String swtJar = SWTAppUIErrorMain.swtJar;
    
    public static final String swtEx = TestCommons.swtEx;
    
    //verified no errors
	private String addressbook = swtEx + "addressbook" + Globals.pathSep + swtJar;
	private String accessability = swtEx + "accessibility" + Globals.pathSep + swtJar;
	private String browser = swtEx + "browserexample" + Globals.pathSep + swtJar;
	private String clipboard = swtEx + "clipboard" + Globals.pathSep + swtJar;
	private String controlexample = swtEx + "controlexample" +Globals.pathSep + swtJar;
	private String dnd = swtEx + "dnd" + Globals.pathSep + swtJar;
	private String graphics = swtEx + "graphics" + Globals.pathSep + swtJar;
	private String helloworldex = swtEx + "helloworld" + Globals.pathSep + swtJar;
	private String hoverhelp = swtEx + "hoverhelp" + Globals.pathSep + swtJar;
	private String javaviewer = swtEx + "javaviewer" + Globals.pathSep + swtJar;
	private String layoutexample = swtEx + "layoutexample" + Globals.pathSep + swtJar;
	private String paint = swtEx + "paint" + Globals.pathSep + swtJar;
	private String texteditor = swtEx + "texteditor" + Globals.pathSep + swtJar;
	
	//have no errors, but issues positivies
	String imageanalyzer = swtEx + "imageanalyzer" + Globals.pathSep + swtJar;
	String fileviewer = swtEx + "fileviewer" + Globals.pathSep + swtJar;
	
	public void testAddressBook() {
		this.checkCallChainNumber(0, addressbook);
	}
	
	public void testAccessability() {
		this.checkCallChainNumber(0, accessability);
	}
	
	public void testBrowser() {
		this.checkCallChainNumber(0, browser);
	}
	
	public void testClipboard() {
		this.checkCallChainNumber(0, clipboard);
	}
	
	public void testDnd() {
		this.checkCallChainNumber(0, dnd);
	}
	
	
	public void testControlexample() {
		this.checkCallChainNumber(0, controlexample);
	}
	
	public void testGraphics() {
		this.checkCallChainNumber(0, graphics);
	}
	
	public void testHelloworldex() {
		this.checkCallChainNumber(0, helloworldex);
	}
	
	public void testHoverhelp() {
		this.checkCallChainNumber(0, hoverhelp);
	}
	
	public void testJavaviewer() {
		this.checkCallChainNumber(0, javaviewer);
	}
	
	public void testLayoutexample() {
		this.checkCallChainNumber(0, layoutexample);
	}
	
	public void testPaint() {
		this.checkCallChainNumber(0, paint);
	}
	
	public void testTexteditor() {
		this.checkCallChainNumber(0, texteditor);
	}
	
	public void testImageanalyzer() {
		this.checkCallChainNumber(4, imageanalyzer);
	}
	
	public void testFileviewer() {
		this.checkCallChainNumber(2, fileviewer);
	}
	
	private void checkCallChainNumber(int expectedNum, String appPath) {
		UIAnomalyDetector detector = new UIAnomalyDetector(appPath);
		List<AnomalyCallChain> chains = detector.detectUIAnomaly();
		assertEquals("The number of expected call chain is wrong", expectedNum, chains.size());
	}
}