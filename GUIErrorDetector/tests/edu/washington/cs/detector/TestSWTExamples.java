package edu.washington.cs.detector;

import java.util.List;

import junit.framework.TestCase;

public class TestSWTExamples extends TestCase {
	
    public static final String swtJar = SWTAppUIErrorMain.swtJar;
    
    public static final String swtEx = TestCommons.swtEx;
    
    //verified no errors
	private String addressbook = swtEx + "addressbook" + ";" + swtJar;
	private String accessability = swtEx + "accessibility" + ";" + swtJar;
	private String browser = swtEx + "browserexample" + ";" + swtJar;
	private String clipboard = swtEx + "clipboard" + ";" + swtJar;
	private String controlexample = swtEx + "controlexample" + ";" + swtJar;
	private String dnd = swtEx + "dnd" + ";" + swtJar;
	private String graphics = swtEx + "graphics" + ";" + swtJar;
	private String helloworldex = swtEx + "helloworld" + ";" + swtJar;
	private String hoverhelp = swtEx + "hoverhelp" + ";" + swtJar;
	private String javaviewer = swtEx + "javaviewer" + ";" + swtJar;
	private String layoutexample = swtEx + "layoutexample" + ";" + swtJar;
	private String paint = swtEx + "paint" + ";" + swtJar;
	private String texteditor = swtEx + "texteditor" + ";" + swtJar;
	
	//have no errors, but issues positivies
	String imageanalyzer = swtEx + "imageanalyzer" + ";" + swtJar;
	String fileviewer = swtEx + "fileviewer" + ";" + swtJar;
	
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
		this.checkCallChainNumber(2, imageanalyzer);
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