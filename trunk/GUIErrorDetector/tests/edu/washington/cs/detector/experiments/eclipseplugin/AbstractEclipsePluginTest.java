package edu.washington.cs.detector.experiments.eclipseplugin;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.util.graph.Graph;

import edu.washington.cs.detector.AnomalyCallChain;
import edu.washington.cs.detector.AnomalyFinder;
import edu.washington.cs.detector.CGBuilder;
import edu.washington.cs.detector.CGEntryManager;
import edu.washington.cs.detector.CallChainNode;
import edu.washington.cs.detector.FilterStrategy;
import edu.washington.cs.detector.TestCommons;
import edu.washington.cs.detector.ThreadStartFinder;
import edu.washington.cs.detector.UIAnomalyDetector;
import edu.washington.cs.detector.UIAnomalyMethodFinder;
import edu.washington.cs.detector.CGBuilder.CG;
import edu.washington.cs.detector.guider.CGTraverseGuider;
import edu.washington.cs.detector.util.Files;
import edu.washington.cs.detector.util.Globals;
import edu.washington.cs.detector.util.Log;
import edu.washington.cs.detector.util.Utils;
import edu.washington.cs.detector.util.WALAUtils;

import junit.framework.TestCase;

public abstract class AbstractEclipsePluginTest extends TestCase {
//	
//	public static boolean DEBUG = false;
//	
	private CGTraverseGuider threadStartGuider = null;
	private CGTraverseGuider uiAnomalyGuider = null;
	private CG type = null;
	private String completePath = null;
	
	//these four methods must be overriden
	protected abstract Iterable<Entrypoint> getEntrypoints(ClassHierarchy cha);
	protected abstract Collection<CGNode> getStartNodes(Iterable<CGNode> allNodes, ClassHierarchy cha);
	protected abstract String getAppPath();
	protected abstract String getDependentJars();

	//a few configuration options
	protected void setThreadStartGuider(CGTraverseGuider guider) {
		this.threadStartGuider = guider;
	}
	protected void setUIAnomalyGuider(CGTraverseGuider guider) {
		this.uiAnomalyGuider = guider;
	}
	protected void setCGType(CG type) {
		this.type = type;
	}
	protected void setCompletePath(String path) {
		this.completePath = path;
	}
	
	public List<AnomalyCallChain> reportUIErrors(String outputFilePath, CGBuilder.CG opt) throws IOException, ClassHierarchyException {
		String appPath = null;
		if(this.completePath != null) {
			appPath = this.completePath;
		} else {
		    appPath =  TestCommons.assemblyAppPath(getAppPath() /*just a path*/, getDependentJars() /*all jars*/);
		}
	    CGBuilder builder = new CGBuilder(appPath);
	    builder.makeScopeAndClassHierarchy();
	    
	    //see all loaded classes, it is possible some classes are not loaded due to dependence
	    ClassHierarchy cha = builder.getClassHierarchy();
	    System.out.println("Total class loaded: " + cha.getNumberOfClasses());
	    
	    //for debugging
	    WALAUtils.dumpClasses(cha, "./logs/loaded_classes.txt");
	    Utils.dumpCollection(WALAUtils.getUnloadedClasses(cha, TestCommons.getNonSourceNonTestsJars(getAppPath())),
	    		"./logs/unloaded_classes.txt");
	    
	    Iterable<Entrypoint> entries = this.getEntrypoints(cha);
		System.out.println("Number of entries for building CG: " + Utils.countIterable(entries));
		Log.logln("Number of entries for building CG: " + Utils.countIterable(entries));
		for(Entrypoint  entry : entries) {
			Log.logln("  " + entry);
		}
		
		//if user specified CG built types
		if(this.type != null) {
			builder.setCGType(opt);
		}
		builder.buildCG(entries);
		
		Collection<CGNode> startNodes = this.getStartNodes(builder.getAppCallGraph(), cha);
		System.out.println("Number of starting nodes in the built callg graph: " + startNodes.size());
		Log.logln("Number of starting nodes in the built callg graph: " + startNodes.size());
		for(CGNode node : startNodes) {
			Log.logln("    " + node);
		}
		
		UIAnomalyDetector detector = new UIAnomalyDetector(appPath);
		List<AnomalyCallChain> chains = detector.detectUIAnomaly(builder, startNodes);
		
		System.out.println("Number of anomaly call chains: " + chains.size());
		
		return chains;
	}
}
