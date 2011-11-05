package edu.washington.cs.detector;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;

import edu.washington.cs.detector.util.Files;
import edu.washington.cs.detector.util.Log;
import edu.washington.cs.detector.util.Utils;
import edu.washington.cs.detector.util.WALAUtils;

import junit.framework.TestCase;

public abstract class AbstractUITest extends TestCase {
	
	public static boolean DEBUG = false;
	
	protected abstract boolean isUIClass(IClass kclass);
	
	protected boolean isEntryClass(IClass kclass) {return isUIClass(kclass); }
	
	protected abstract String getAppPath();
	
	protected abstract String getDependentJars();
	
	public void reportAppJars() {
		TestCommons.getNonSourceNonTestsJars(getAppPath());
	}
	
	public void checkAppJarNumber(int expectedNumber) {
		assertEquals(expectedNumber, TestCommons.getNonSourceNonTestsJars(getAppPath()).size());
	}
	
	//use the default precision: 0-CFA for call graph
	//treat as every public methods of every UI class as call graph entries
	public List<AnomalyCallChain> reportUIErrors(String outputFilePath) throws IOException, ClassHierarchyException {
		return reportUIErrors(outputFilePath, null);
	}
	
	//permit user to set call graph precision options. but still treat every public
	//methods of every UI class as call graph entries
	public List<AnomalyCallChain> reportUIErrors(String outputFilePath, CGBuilder.CG opt) throws IOException, ClassHierarchyException {
		String appPath =  TestCommons.assemblyAppPath(getAppPath(), getDependentJars());
	    CGBuilder builder = new CGBuilder(appPath);
	    builder.makeScopeAndClassHierarchy();
	    
	    //find all UI classes
	    ClassHierarchy cha = builder.getClassHierarchy();
	    List<String> uiClasses = new LinkedList<String>();
	    for(IClass kclass : cha) {
	    	if(this.isUIClass(kclass)) {
	    		 uiClasses.add(WALAUtils.getJavaFullClassName(kclass));
	    	}
		}
	    
	    WALAUtils.dumpClasses(cha, "./logs/loaded_classes.txt");
	    Utils.dumpCollection(WALAUtils.getUnloadedClasses(cha, TestCommons.getNonSourceNonTestsJars(getAppPath())),
	    		"./logs/unloaded_classes.txt");
	    
	    //report UI errors
	    return reportUIErrors(outputFilePath, appPath, uiClasses, builder, opt);
	}
	
	
	//permit user to set call graph precision options, and treat every public methods of the given
	//ui class list as entries
	public List<AnomalyCallChain> reportUIErrors(String outputFilePath, List<String> uiClasses, CGBuilder.CG opt) throws IOException, ClassHierarchyException {
		String appPath =  TestCommons.assemblyAppPath(getAppPath(), getDependentJars());
	    CGBuilder builder = new CGBuilder(appPath);
	    builder.makeScopeAndClassHierarchy();
	    
	    //report UI errors
	    return reportUIErrors(outputFilePath, appPath, uiClasses, builder, opt);
	}
	
	//treat every public method in the uiClass list as call graph entry, permit user
	//to set call graph precision
	private List<AnomalyCallChain> reportUIErrors(String outputFilePath, String appPath, List<String> uiClasses, CGBuilder builder, CGBuilder.CG opt)
	    throws IOException, ClassHierarchyException {
		if(builder.getClassHierarchy() == null || builder.getAnalysisScope() == null) {
			throw new RuntimeException("Please call builder.makeScopeAndClassHierarchy first.");
		}
		//dump info
		System.out.println("Total class num: " + builder.getClassHierarchy().getNumberOfClasses());
	    System.out.println("Number of UI classes: " + uiClasses.size());
	    Utils.dumpCollection(uiClasses, "./logs/ui_classes.txt");
	    
	    Iterable<Entrypoint> entries = CGEntryManager.getAllPublicMethods(builder, uiClasses);
		int size = 0;
		for(Entrypoint entry : entries) {
			assertTrue(entry != null);
			size++;
		}
		System.out.println("Number of entries for building CG: " + size);
		
		if(opt != null) {
			builder.setCGType(opt);
		}
		builder.buildCG(entries);
		System.out.println("number of entry node in the built CG: " + builder.getCallGraph().getEntrypointNodes().size());
		System.out.println("CG node num: " + builder.getCallGraph().getNumberOfNodes());
		System.out.println("App CG node num: " + builder.getAppCallGraph().getNumberOfNodes());
		
//		Utils.dumpCollection(builder.getCallGraph().getEntrypointNodes(), "./logs/entries.txt");
		Utils.dumpCollection(entries, "./logs/entries.txt");
		
		if(DEBUG) {
		    WALAUtils.viewCallGraph(builder.getAppCallGraph());
		    Files.writeToFile(builder.getAppCallGraph().toString(), "./logs/callgraph.txt");
		}
		if(outputFilePath != null) {
		    Log.logConfig(outputFilePath);
		}
		//try to detect errors from all public methods
		UIAnomalyDetector detector = new UIAnomalyDetector(appPath);
		List<AnomalyCallChain> chains = detector.detectUIAnomaly(builder);
		System.out.println("Number of anomaly call chains: " + chains.size());
		
		return chains;
	}
	
	/** Treat every public method in the uiClass list as call graph entry, permit user
	 *  to set call graph precision
	 *  All public methods in the entry classes are used as entry nodes, but when detecting errors, only
	 *  public methods in uiClasses are used as the starting points.
	 * @throws IOException 
	 * @throws ClassHierarchyException 
	 */
	public List<AnomalyCallChain> reportUIErrorsWithEntries(String outputFilePath, CGBuilder.CG opt)
	    throws ClassHierarchyException, IOException {
		return reportUIErrorsWithEntries(outputFilePath, opt, Collections.<FilterStrategy>emptyList());
	}
	public List<AnomalyCallChain> reportUIErrorsWithEntries(String outputFilePath, CGBuilder.CG opt, List<FilterStrategy> filters)
	    throws IOException, ClassHierarchyException {
		String appPath = TestCommons.assemblyAppPath(getAppPath(), getDependentJars());
		CGBuilder builder = new CGBuilder(appPath);
	    builder.makeScopeAndClassHierarchy();
	    
		if(builder.getClassHierarchy() == null || builder.getAnalysisScope() == null) {
			throw new RuntimeException("Please call builder.makeScopeAndClassHierarchy first.");
		}
		
		//find all entry classes
	    ClassHierarchy cha = builder.getClassHierarchy();
	    List<String> uiClasses = new LinkedList<String>();
	    for(IClass kclass : cha) {
	    	if(this.isUIClass(kclass)) {
	    		 uiClasses.add(WALAUtils.getJavaFullClassName(kclass));
	    	}
		}
	    
	    List<String> entryClasses = new LinkedList<String>();
	    for(IClass kclass : cha) {
	    	if(this.isEntryClass(kclass)) {
	    		entryClasses.add(WALAUtils.getJavaFullClassName(kclass));
	    	}
		}
	    
		//dump info
		System.out.println("Total class num: " + builder.getClassHierarchy().getNumberOfClasses());
		System.out.println("Number of entry classes: " + entryClasses.size());
	    System.out.println("Number of UI classes: " + uiClasses.size());
	    Utils.dumpCollection(entryClasses, "./logs/entry_classes.txt");
	    Utils.dumpCollection(uiClasses, "./logs/ui_classes.txt");
	    
	    Iterable<Entrypoint> entries = CGEntryManager.getAllPublicMethods(builder, entryClasses);
		System.out.println("Number of entries for building CG: " + Utils.countIterable(entries));
		Utils.dumpCollection(entries, "./logs/entries.txt");
		
		if(opt != null) {
			builder.setCGType(opt);
		}
		builder.buildCG(entries);
		
		Iterable<Entrypoint> queryUIEntries = CGEntryManager.getAllPublicMethods(builder, uiClasses);
		System.out.println("Number of query UI entries: " + Utils.countIterable(queryUIEntries));
		Utils.dumpCollection(queryUIEntries, "./logs/ui_query_entries.txt");
		
		System.out.println("number of entry node in the built CG: " + builder.getCallGraph().getEntrypointNodes().size());
		System.out.println("CG node num: " + builder.getCallGraph().getNumberOfNodes());
		System.out.println("App CG node num: " + builder.getAppCallGraph().getNumberOfNodes());
		
		if(DEBUG) {
		    WALAUtils.viewCallGraph(builder.getAppCallGraph());
		    Files.writeToFile(builder.getAppCallGraph().toString(), "./logs/callgraph.txt");
		}
		if(outputFilePath != null) {
		    Log.logConfig(outputFilePath);
		}
		//try to detect errors from all public methods
		UIAnomalyDetector detector = new UIAnomalyDetector(appPath);
		
		//add filters
		detector.addFilterStrategies(filters);
		
		Collection<CGNode> uiEntryNodes = builder.getCallGraphEntryNodes(queryUIEntries);
		List<AnomalyCallChain> chains = detector.detectUIAnomaly(builder, uiEntryNodes);
		System.out.println("Number of anomaly call chains: " + chains.size());
		
		return chains;
	}
}
