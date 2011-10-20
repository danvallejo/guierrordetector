package edu.washington.cs.detector;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.util.graph.Graph;

import edu.washington.cs.detector.util.Log;
import edu.washington.cs.detector.util.WALAUtils;

import junit.framework.TestCase;

public abstract class AbstractUITest extends TestCase {
	
	protected abstract boolean isUIClass(IClass kclass);
	
	protected abstract String getAppPath();
	
	protected abstract String getDependentJars();
	
	public void reportAppJars() {
		TestCommons.getNonSourceNonTestsJars(getAppPath());
	}
	
	public void reportUIErrors(String outputFilePath) throws IOException, ClassHierarchyException {
		String appPath =  TestCommons.assemblyAppPath(getAppPath(), getDependentJars());
	    CGBuilder builder = new CGBuilder(appPath);
	    builder.makeScopeAndClassHierarchy();
	    
	    ClassHierarchy cha = builder.getClassHierarchy();
	    List<String> uiClasses = new LinkedList<String>();
	    for(IClass kclass : cha) {
	    	if(this.isUIClass(kclass)) {
	    		 uiClasses.add(WALAUtils.getJavaFullClassName(kclass));
	    	}
		}
	    System.out.println("Total class num: " + cha.getNumberOfClasses());
	    System.out.println("Number of UI classes: " + uiClasses.size());
	    
	    Iterable<Entrypoint> entries = CGEntryManager.getAllPublicMethods(builder, uiClasses);
		int size = 0;
		for(Entrypoint entry : entries) {
			assertTrue(entry != null);
			//System.out.println("entry: " + entry);
			size++;
		}
		System.out.println("entry size is: " + size);
		builder.buildCG(entries);
		
        Log.logConfig(outputFilePath);
        
		Graph<CGNode> appCG = builder.getAppCallGraph();
		System.out.println("App CG node num: " + appCG.getNumberOfNodes());
		
		//try to detect errors from all public methods
		UIAnomalyDetector detector = new UIAnomalyDetector(appPath);
		List<AnomalyCallChain> chains = detector.detectUIAnomaly(builder);
		System.out.println("Number of anomaly call chains: " + chains.size());
	}
}
