package edu.washington.cs.detector;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import com.ibm.wala.classLoader.IClass;

import edu.washington.cs.detector.util.Files;
import edu.washington.cs.detector.util.Globals;
import edu.washington.cs.detector.util.PropertyReader;
import edu.washington.cs.detector.util.Utils;

public class TestCommons extends TestCase {
	
	public final static PropertyReader reader = PropertyReader.createInstance("./tests/tests.properties"); 
	
	public final static String swtEx = reader.getProperty("swt.example.folder");
	public final static String testfolder = reader.getProperty("project.test.folder");
	public static String cdt_60_dir = reader.getProperty("cdt.60.dir");
	public static String mylyn_362_dir = reader.getProperty("mylyn.362.dir");
	public static String rse_303_dir = reader.getProperty("rse.303.dir");
	public static String pde_eclipseplugin_dir = reader.getProperty("pde.eclipseplugin.dir");
	public static String plugintest_bin_dir = reader.getProperty("plugin.test.bin.dir");
	public static String subeclipse_1_6 = reader.getProperty("subeclipse.1.6.dir");
	
	public static boolean isConcreteAccessibleClass(IClass kclass) {
		return !kclass.isAbstract() && !kclass.isInterface() && kclass.isPublic()
		    && kclass.getName().getClassName().toString().indexOf("$") == -1  ;
	}
	
	public static Collection<String> getPluginExposedClasses(String pluginDir) throws IOException {
        return getPluginExposedClasses(pluginDir, null);
	}
	
	public static Collection<String> getPluginExposedClasses(String pluginDir, String outputFilePath) throws IOException {
        Set<String> allClasses = new LinkedHashSet<String>();
		List<String> jarFiles = getNonSourceNonTestsJars(pluginDir);
		for(String jarFile : jarFiles) {
			allClasses.addAll(Utils.extractClassFromPluginXML(jarFile));
		}
		if(outputFilePath != null) {
			Utils.dumpCollection(allClasses, outputFilePath);
		}
		return allClasses;
	}
	
	public static List<String> getNonSourceNonTestsJars(String dir) {
		List<String> files = Files.findFilesInDir(dir, null, ".jar");
		List<String> nonSrcNonTestFiles = new LinkedList<String>();
		System.out.println("Loaded app jars: ");
		for(String f : files) {
			//if(f.indexOf("source") == -1 && f.indexOf("tests") == -1) {
				String jarPath = dir + Globals.fileSep + f;
				System.out.println(jarPath);
				nonSrcNonTestFiles.add(jarPath);
			//}
		}
		System.out.println("Number of loaded app jars: " + nonSrcNonTestFiles.size());
		return nonSrcNonTestFiles;
	}
	
	public static List<String> getJarsFromPath(String path) {
		String[] jars = path.split(Globals.pathSep);
		List<String> jarList = new LinkedList<String>();
		for(String jar : jars) {
			if(jar.trim().endsWith(".jar")) {
			    jarList.add(jar);
			}
		}
		return jarList;
		
	}
	
	public static String assemblyAppPath(String dir, String additionalJars) {
		List<String> appJars = TestCommons.getNonSourceNonTestsJars(dir);
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < appJars.size(); i++) {
			if(i != 0) {
				sb.append(Globals.pathSep);
			}
			sb.append(appJars.get(i));
		}
		if(additionalJars != null) {
		    sb.append(Globals.pathSep);
		    sb.append(additionalJars);
		}
		System.out.println("All assembled jar path: " + sb.toString());
		return sb.toString();
	}
}