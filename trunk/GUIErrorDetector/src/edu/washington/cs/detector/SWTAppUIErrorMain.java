package edu.washington.cs.detector;

import edu.washington.cs.detector.util.Log;

//TODO: context sensitivity, and a heuristic to fitler redundancy
public class SWTAppUIErrorMain {
	
	public static final String swtJar = "D:\\research\\guierror\\wala\\tmp\\org.eclipse.swt.win32.win32.x86_64_3.6.2.v3659c.jar";
	
	public static String default_log = "./log.txt";
	
	public static void main(String[] args) {
		
		String fileviewer = "D:\\research\\guierror\\eclipsews\\SWTExamples\\bin\\org\\eclipse\\swt\\examples\\fileviewer";
		
		//XXX a few false positives, wrapping inside a try-catch block?
		//static analysis precision
		//for fileviewer subject
	    Log.logConfig(SWTAppUIErrorMain.default_log);
	    
		String appPath = fileviewer + ";" + swtJar;
		UIAnomalyDetector detector = new UIAnomalyDetector(appPath);
		detector.detectUIAnomaly();
	    
	}

}