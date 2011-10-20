package edu.washington.cs.detector;

import java.util.LinkedList;
import java.util.List;

import edu.washington.cs.detector.util.EclipsePluginCommons;
import edu.washington.cs.detector.util.Files;
import edu.washington.cs.detector.util.Globals;


public class TestCommons {
	
	public final static String swtEx = "D:\\research\\guierror\\eclipsews\\SWTExamples\\bin\\org\\eclipse\\swt\\examples\\";
	
	public final static String testfolder = "D:\\research\\guierror\\eclipsews\\GUIErrorDetector\\bin\\test\\";
	
	public static String cdt_60_dir = "D:\\research\\guierror\\subjects\\cdt-master-6.0.0";
	
	public static String mylyn_362_dir = "D:\\research\\guierror\\subjects\\mylyn-3.6.2.v20110908-0706";
	
	public static String rse_303_dir = "D:\\research\\guierror\\subjects\\RSE-SDK-3.0.3\\eclipse";
	
	public static List<String> getNonSourceNonTestsJars(String dir) {
		List<String> files = Files.findFilesInDir(dir, null, ".jar");
		List<String> nonSrcNonTestFiles = new LinkedList<String>();
		System.out.println("---- jars ------");
		for(String f : files) {
			if(f.indexOf("source") == -1 && f.indexOf("tests") == -1) {
				String jarPath = dir + Globals.fileSep + f;
				System.out.println(jarPath);
				nonSrcNonTestFiles.add(jarPath);
			}
		}
		System.out.println("Number of jars: " + nonSrcNonTestFiles.size());
		return nonSrcNonTestFiles;
	}
	
	public static String assemblyAppPath(String dir, String additionalJars) {
		List<String> appJars = TestCommons.getNonSourceNonTestsJars(dir);
		String uiJars = additionalJars;//EclipsePluginCommons.DEPENDENT_JARS;
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < appJars.size(); i++) {
			if(i != 0) {
				sb.append(Globals.pathSep);
			}
			sb.append(appJars.get(i));
		}
		sb.append(Globals.pathSep);
		sb.append(uiJars);
		System.out.println("All assembled jar path: " + sb.toString());
		return sb.toString();
	}
}