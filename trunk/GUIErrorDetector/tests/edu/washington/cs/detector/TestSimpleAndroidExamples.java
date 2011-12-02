package edu.washington.cs.detector;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.util.graph.Graph;

import edu.washington.cs.detector.CGBuilder.CG;
import edu.washington.cs.detector.experiments.filters.RemoveSubsumedChainStrategy;
import edu.washington.cs.detector.util.AndroidUtils;
import edu.washington.cs.detector.util.Files;
import edu.washington.cs.detector.util.Globals;
import edu.washington.cs.detector.util.Log;
import edu.washington.cs.detector.util.Utils;
import edu.washington.cs.detector.util.WALAUtils;
import junit.framework.TestCase;

public class TestSimpleAndroidExamples extends TestCase {
	
	public String appPath =
		//"D:\\research\\guierror\\eclipsews\\TestAndroid\\bin\\classes\\"
		Utils.concatenate(Utils.getClassesRecursive("D:\\research\\guierror\\eclipsews\\TestAndroid\\bin"), Globals.pathSep) 
		+ Globals.pathSep +
		"D:\\research\\guierror\\eclipsews\\GUIErrorDetector\\exp-subjects\\original-android.jar";
	
	public String dirPath = "D:\\research\\guierror\\eclipsews\\TestAndroid";
	
	public void testAndroidApp() throws IOException, ClassHierarchyException {
		CGBuilder builder = new CGBuilder(appPath,UIAnomalyDetector.EXCLUSION_FILE_SWING);
	    builder.makeScopeAndClassHierarchy();
	    
	    //find all UI classes
	    ClassHierarchy cha = builder.getClassHierarchy();
	    List<String> uiClasses = new LinkedList<String>();
	    
	    Collection<IClass> activities = AndroidUtils.getAppActivityClasses(cha);
	    for(IClass activity : activities) {
	    	uiClasses.add(WALAUtils.getJavaFullClassName(activity));
	    	System.out.println("activity class: " + activity);
	    }
	    
	    for(IClass c : cha) {
	    	String fullClassName = WALAUtils.getJavaFullClassName(c);
	    	if(AndroidUtils.isInAndroidLib(fullClassName)) {
	    		continue;
	    	}
	    	if(AndroidUtils.isCustomizedListener(cha, fullClassName)) {
	    	    uiClasses.add(fullClassName);
	    	    System.out.println("listener class: " + fullClassName);
	    	}
	    }
	    
	    Utils.removeRedundant(uiClasses);
	    
	    Iterable<Entrypoint> uiEntries = CGEntryManager.getAllPublicMethods(builder, uiClasses);
		
		Collection<String> declaredClasses = AndroidUtils.extractAllUIs(cha, new File(dirPath));
		
		System.out.println("Number of declared UI: " + declaredClasses.size());
		System.out.println("     " + declaredClasses);
		
	    Iterable<Entrypoint> widgetConstructors = //new LinkedList<Entrypoint>(); 
	    	CGEntryManager.getConstructors(builder, declaredClasses);
	    
	    //Merge2 entries
	    Iterable<Entrypoint> entries = CGEntryManager.mergeEntrypoints(uiEntries, widgetConstructors);
	    
		System.out.println("Number of entries for building CG: " + Utils.countIterable(entries));
		builder.setCGType(CG.RTA);
		builder.buildCG(entries);
		
		System.out.println("number of entry node in the built CG: " + builder.getCallGraph().getEntrypointNodes().size());
		System.out.println("CG node num: " + builder.getCallGraph().getNumberOfNodes());
		System.out.println("App CG node num: " + builder.getAppCallGraph().getNumberOfNodes());
		
//		Graph<CGNode> graph = builder.getAppCallGraph();
		Log.logConfig("./log.txt");
//		WALAUtils.logCallGraph(graph);
		
		//set up the anomaly detection
		UIAnomalyDetector detector = new UIAnomalyDetector(appPath);
		detector.configureCheckingMethods("./tests/edu/washington/cs/detector/checkingmethods_for_android.txt");
		detector.setThreadStartGuider(new CGTraverseNoSystemCalls());
		detector.setUIAnomalyGuider(new CGTraverseOnlyClientRunnableStrategy());
		UIAnomalyDetector.DEBUG = true;
		//UIAnomalyMethodFinder.DEBUG = true;
		
		List<AnomalyCallChain> chains = detector.detectUIAnomaly(builder, builder.getCallGraphEntryNodes(uiEntries));
		
		System.out.println("Number of chains: " + chains.size());
		chains = CallChainFilter.filter(chains, new RemoveSubsumedChainStrategy());
		System.out.println("Number of chains after removing subsumption: " + chains.size());
		
		for(AnomalyCallChain c : chains) {
			System.out.println(c.getFullCallChainAsString());
		}
		
		//set it back
		UIAnomalyMethodFinder.setCheckingMethods("./src/checking_methods.txt");
		UIAnomalyDetector.DEBUG = false;
	}

}