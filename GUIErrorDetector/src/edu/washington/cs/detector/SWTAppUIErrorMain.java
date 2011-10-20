package edu.washington.cs.detector;

import edu.washington.cs.detector.util.Globals;
import edu.washington.cs.detector.util.Log;
import edu.washington.cs.detector.util.PropertyReader;

//TODO: context sensitivity, and a heuristic to fitler redundancy
public class SWTAppUIErrorMain {
	
	public static final PropertyReader reader = PropertyReader.createInstance("./src/detector.properties");
	
	public static final String swtJar = reader.getProperty("swt.jar.location");
	
	public static String default_log = reader.getProperty("default.log.file");
	
	public static void main(String[] args) {
		String appPath = getAppPath(args);
	    Log.logConfig(SWTAppUIErrorMain.default_log);
		appPath = appPath + Globals.pathSep + swtJar;
		UIAnomalyDetector detector = new UIAnomalyDetector(appPath);
		detector.detectUIAnomaly();
	    
	}

	private static String getAppPath(String[] args) {
		throw new RuntimeException("not implemented.");
	}
} 