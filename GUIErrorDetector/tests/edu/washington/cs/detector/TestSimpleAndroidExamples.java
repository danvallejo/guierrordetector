package edu.washington.cs.detector;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
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
import edu.washington.cs.detector.guider.CGTraverseNoSystemCalls;
import edu.washington.cs.detector.guider.CGTraverseOnlyClientRunnableStrategy;
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
	    List<String> entryClasses = new LinkedList<String>();
	    
	    Collection<IClass> activities = AndroidUtils.getAppActivityClasses(cha);
	    Collection<String> activityClasses = WALAUtils.convertIClassToStrings(activities);
//	    for(IClass activity : activities) {
//	    	entryClasses.add(WALAUtils.getJavaFullClassName(activity));
//	    	System.out.println("activity class: " + activity);
//	    }
	    
	    for(IClass c : cha) {
	    	String fullClassName = WALAUtils.getJavaFullClassName(c);
	    	if(AndroidUtils.isInAndroidLib(fullClassName)) {
	    		continue;
	    	}
	    	if(AndroidUtils.isCustomizedListener(cha, fullClassName)) {
	    	    entryClasses.add(fullClassName);
	    	    System.out.println("listener class: " + fullClassName);
	    	}
	    }
	    
	    Utils.removeRedundant(entryClasses);
	    
	    entryClasses.removeAll(activityClasses);
	    
	    Iterable<Entrypoint> uiEntries = CGEntryManager.getAllPublicMethods(builder, entryClasses);
	    
	    List<String> activityClassList = new LinkedList<String>();
	    activityClassList.addAll(activityClasses);
	    Iterable<Entrypoint> allActivityMethods = CGEntryManager.getAllPublicMethods(builder, activityClassList, false); /*need to use inherit?*/
	    
	    List<String> otherClasses = new LinkedList<String>();
	    otherClasses.add("android.view.ViewRoot");
	    Iterable<Entrypoint> otherClassMethods = CGEntryManager.getAllPublicMethods(builder, otherClasses, false); 
	                                             //CGEntryManager.getConstructors(builder, otherClasses);
		
		Collection<String> declaredClasses = AndroidUtils.extractAllUIs(cha, new File(dirPath));
		
		System.out.println("Number of declared UI: " + declaredClasses.size());
		System.out.println("     " + declaredClasses);
		
	    Iterable<Entrypoint> widgetConstructors = //new LinkedList<Entrypoint>(); 
	    	CGEntryManager.getConstructors(builder, declaredClasses);
	    
	    //Merge2 entries
	    Iterable<Entrypoint> entries = uiEntries;
//	    entries = CGEntryManager.mergeEntrypoints(entries, widgetConstructors);
	    entries = CGEntryManager.mergeEntrypoints(entries, allActivityMethods);
	    entries = CGEntryManager.mergeEntrypoints(entries, otherClassMethods);
	    
	    entries = Utils.returnUniqueIterable(entries);
	    /**
	     * Build a call graph, and start to detect errors below.
	     * */
	    
	    Log.logConfig("./log.txt");
	    Log.logln("Entries in building CG: ");
	    Utils.logCollection(entries);
	    
		System.out.println("Number of entries for building CG: " + Utils.countIterable(entries));
		builder.setCGType(CG.RTA);
//		builder.setCGType(CG.ZeroCFA);
//		builder.setCGType(CG.OneCFA);

		//set up the bypasslogic file
		builder.setByPassFile("samplereflection.xml");
		
		builder.buildCG(entries);
		
		System.out.println("number of entry node in the built CG: " + builder.getCallGraph().getEntrypointNodes().size());
		System.out.println("CG node num: " + builder.getCallGraph().getNumberOfNodes());
		System.out.println("App CG node num: " + builder.getAppCallGraph().getNumberOfNodes());
		
		Log.logln("Entries in the built CG: ");
		Utils.logCollection(builder.getCallGraph().getEntrypointNodes());
		
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