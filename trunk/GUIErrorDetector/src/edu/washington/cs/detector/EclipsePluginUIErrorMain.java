package edu.washington.cs.detector;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.util.collections.HashSetFactory;

import edu.washington.cs.detector.util.EclipsePluginCommons;
import edu.washington.cs.detector.util.Files;
import edu.washington.cs.detector.util.Globals;

public class EclipsePluginUIErrorMain {

	public static String UI_CLASSES_FILE = null;
	
	public static String APP_PATH = null;
	
	public static String APP_PACKAGE = null;
	
	public static void main(String[] args) throws ClassHierarchyException, IOException {
		EclipsePluginUIErrorMain main = new EclipsePluginUIErrorMain();
		main.reportUIErrors();
	}
	
	public List<AnomalyCallChain> reportUIErrors() throws IOException, ClassHierarchyException {
		List<String> uiClasses = this.getAllUIClasses();
		if(uiClasses.isEmpty()) {
			System.err.println("There is no UI class in the given plugin.");
			System.exit(1);
		}
		System.out.println("All UI classes: " + uiClasses);
		
		//construct the call graph and analyze the errors
		String appPath = APP_PATH + Globals.pathSep + EclipsePluginCommons.DEPENDENT_JARS;
		CGBuilder builder = new CGBuilder(appPath);
		builder.makeScopeAndClassHierarchy();
		
		Iterable<Entrypoint> entries = this.getAllPublicMethods(builder, uiClasses);
		System.out.println("All entries:");
		int count = 0;
		for(Entrypoint ep : entries) {
			System.out.println(" " + (count++) + ": " + ep);
		}
		
		//start to detect UI errors
		UIAnomalyDetector detector = new UIAnomalyDetector(appPath);
		builder.buildCG(entries);
		List<AnomalyCallChain> anomalyCallChains = detector.detectUIAnomaly(builder);
		StringBuilder sb = new StringBuilder();
		sb.append("==== we found: " + anomalyCallChains.size() + " anomaly call chains");
		sb.append(Globals.lineSep);
		for(int i = 0; i < anomalyCallChains.size(); i++) {
			sb.append("#Number: " + i);
			sb.append(Globals.lineSep);
			sb.append(anomalyCallChains.get(i).getFullCallChainAsString());
			sb.append(Globals.lineSep);
		}
		
		System.out.println(sb.toString());
		return anomalyCallChains;
	}
	
	public List<String> getAllUIClasses() {
		try {
		    return Files.readWhole(UI_CLASSES_FILE);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public Iterable<Entrypoint> getAllPublicMethods(CGBuilder builder, List<String> uiClasses) {
		AnalysisScope scope = builder.getAnalysisScope();
		ClassHierarchy cha = builder.getClassHierarchy();
		
		final HashSet<Entrypoint> result = HashSetFactory.make();
		for(String methodClass : uiClasses) {
		    Iterable<Entrypoint> entries = CGEntryManager.getAppPublicMethodsByClass(scope, cha, methodClass);
		    for(Entrypoint ep : entries) {
		    	result.add(ep);
		    }
		}
		
		return new Iterable<Entrypoint>() {
			public Iterator<Entrypoint> iterator() {
				return result.iterator();
			}
		}; 
	}
}