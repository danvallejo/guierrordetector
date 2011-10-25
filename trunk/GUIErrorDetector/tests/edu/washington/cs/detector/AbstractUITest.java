package edu.washington.cs.detector;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;

import edu.washington.cs.detector.util.Log;
import edu.washington.cs.detector.util.WALAUtils;

import junit.framework.TestCase;

public abstract class AbstractUITest extends TestCase {
	
	public static boolean DEBUG = false;
	
	protected abstract boolean isUIClass(IClass kclass);
	
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
	    
	    Iterable<Entrypoint> entries = CGEntryManager.getAllPublicMethods(builder, uiClasses);
		int size = 0;
		for(Entrypoint entry : entries) {
			assertTrue(entry != null);
			//System.out.println("entry: " + entry);
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
		
		if(DEBUG) {
		    WALAUtils.viewCallGraph(builder.getAppCallGraph());
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
}
