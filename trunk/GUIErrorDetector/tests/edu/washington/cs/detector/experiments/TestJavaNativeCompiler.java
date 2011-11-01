package edu.washington.cs.detector.experiments;

import java.io.IOException;
import java.util.List;

import edu.washington.cs.detector.AnomalyCallChain;
import edu.washington.cs.detector.CGBuilder;
import edu.washington.cs.detector.CallChainFilter;
import edu.washington.cs.detector.RemoveSystemCallStrategy;
import edu.washington.cs.detector.TestCommons;
import edu.washington.cs.detector.UIAnomalyDetector;
import edu.washington.cs.detector.CGBuilder.CG;
import edu.washington.cs.detector.util.Globals;
import edu.washington.cs.detector.util.Utils;
import edu.washington.cs.detector.util.WALAUtils;
import junit.framework.TestCase;

public class TestJavaNativeCompiler extends TestCase {
	
	/**
	 * Get a false positive:
	 * Application, Lorg/eclipse/swt/widgets/Display, getDefault()
	 * This method may access incorrect thread, however, it is highly unlikely. since
	 * the display will be initialized as soon as the GUI pops up.
	 * */
	public String appPath = "D:\\research\\guierror\\subjects\\javanativecompiler.jar";
	
	public String libJar =
		"D:\\research\\guierror\\eclipsews\\JavaNativeCompiler\\win\\swt.jar" +
		Globals.pathSep + "D:\\research\\guierror\\eclipsews\\JavaNativeCompiler\\data.jar" +
		Globals.pathSep + "D:\\research\\guierror\\eclipsews\\JavaNativeCompiler\\bcel-5.2.jar";
	
	public void testRunningJavaNativeCompiler() throws IOException {
		String path = appPath + Globals.pathSep + libJar;
        UIAnomalyDetector detector = new UIAnomalyDetector(path);
		
		CGBuilder builder = new CGBuilder(path);
		builder.setCGType(CG.OneCFA);
		builder.buildCG();
		
		WALAUtils.dumpClasses(builder.getClassHierarchy(), "./logs/loaded_classes.txt");
	    Utils.dumpCollection(WALAUtils.getUnloadedClasses(builder.getClassHierarchy(),
	    		TestCommons.getJarsFromPath(path)),  "./logs/unloaded_classes.txt");
		
		List<AnomalyCallChain> chains = detector.detectUIAnomaly(builder);
		System.out.println("Number of anomaly call chains: " + chains.size());
		
		CallChainFilter filter = new CallChainFilter(chains);
		chains = filter.apply(new RemoveSystemCallStrategy());
		System.out.println("No of chains after filtering system classes: " + chains.size());
		
		Utils.dumpAnomalyCallChains(chains, "./logs/javanativecompiler-anomalies.txt");
	}
	
}
