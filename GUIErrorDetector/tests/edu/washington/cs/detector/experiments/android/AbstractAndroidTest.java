package edu.washington.cs.detector.experiments.android;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.core.tests.callGraph.CallGraphTestUtil;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;

import edu.washington.cs.detector.AnomalyCallChain;
import edu.washington.cs.detector.CGBuilder;
import edu.washington.cs.detector.CGEntryManager;
import edu.washington.cs.detector.CallChainFilter;
import edu.washington.cs.detector.UIAnomalyDetector;
import edu.washington.cs.detector.UIAnomalyMethodFinder;
import edu.washington.cs.detector.CGBuilder.CG;
import edu.washington.cs.detector.experiments.filters.RemoveSubsumedChainStrategy;
import edu.washington.cs.detector.guider.CGTraverseGuider;
import edu.washington.cs.detector.guider.CGTraverseNoSystemCalls;
import edu.washington.cs.detector.guider.CGTraverseOnlyClientRunnableStrategy;
import edu.washington.cs.detector.util.AndroidUtils;
import edu.washington.cs.detector.util.ApkUtils;
import edu.washington.cs.detector.util.Files;
import edu.washington.cs.detector.util.Log;
import edu.washington.cs.detector.util.Utils;
import edu.washington.cs.detector.util.WALAUtils;
import junit.framework.TestCase;

public abstract class AbstractAndroidTest extends TestCase {
	
	abstract protected String getAppPath();
	
	abstract protected String getDirPath();
	
	public List<AnomalyCallChain> findErrorsInAndroidApp(CG type, CGTraverseGuider ui2startGuider, CGTraverseGuider start2checkGuider)
	    throws IOException, ClassHierarchyException {
		CGBuilder builder = new CGBuilder(getAppPath(),CallGraphTestUtil.REGRESSION_EXCLUSIONS);
	    builder.makeScopeAndClassHierarchy();
	    
	    ClassHierarchy cha = builder.getClassHierarchy();

		Log.logConfig("./log.txt");
	    
	    Collection<IClass> runnableInApp = WALAUtils.getRunnablesInApp(cha);
	    Collection<IClass> runnableInClient = new LinkedHashSet<IClass>();
	    List<String> allAndroidClasses = Files.readWhole("./tests/edu/washington/cs/detector/util/androidclasses.txt");
	    for(IClass c : runnableInApp) {
	    	String fullNameC = WALAUtils.getJavaFullClassName(c);
	    	if(allAndroidClasses.contains(fullNameC)) {
	    		continue;
	    	}
	    	runnableInClient.add(c);
	    }
	    
	    System.out.println("Number of runnable in App: " + runnableInApp.size());
	    System.out.println(runnableInApp);
	    System.out.println();
	    
	    Log.logln("Number of runnable loaded in app: " + runnableInApp.size());
	    Log.logln("Number of runnable in client code: " + runnableInClient.size());
	    for(IClass c : runnableInClient) {
	    	Log.logln("   " + c.toString());
	    }
	    
	    List<String> uiClasses = new LinkedList<String>();
	    //get all activity classes
	    Collection<IClass> activities = AndroidUtils.getAppActivityClasses(cha);
	    for(IClass activity : activities) {
	    	uiClasses.add(WALAUtils.getJavaFullClassName(activity));
	    	System.out.println("activity class: " + activity);
	    }
	    //get all listener classess
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
	    //remove all redundance
	    Utils.removeRedundant(uiClasses);
	    Iterable<Entrypoint> uiEntries = CGEntryManager.getAllPublicMethods(builder, uiClasses);
		
	    //get all declared UI classes
		Collection<String> declaredClasses = AndroidUtils.extractAllUIs(cha, new File(getDirPath()));
		System.out.println("Number of declared UI: " + declaredClasses.size());
		System.out.println("     " + declaredClasses);
		
		//add all UI constructors as the entry point
	    Iterable<Entrypoint> widgetConstructors = //new LinkedList<Entrypoint>(); 
	    	CGEntryManager.getConstructors(builder, declaredClasses);
	    
	    //Merge2 entries
	    Iterable<Entrypoint> entries = CGEntryManager.mergeEntrypoints(uiEntries, widgetConstructors);
	    
		System.out.println("Number of entries for building CG: " + Utils.countIterable(entries));
		builder.setCGType(type);
		builder.buildCG(entries);
		
		System.out.println("number of entry node in the built CG: " + builder.getCallGraph().getEntrypointNodes().size());
		System.out.println("CG node num: " + builder.getCallGraph().getNumberOfNodes());
		System.out.println("App CG node num: " + builder.getAppCallGraph().getNumberOfNodes());
		
		
		//set up the anomaly detection
		UIAnomalyDetector detector = new UIAnomalyDetector(getAppPath());
		detector.configureCheckingMethods("./tests/edu/washington/cs/detector/checkingmethods_for_android.txt");
		detector.setThreadStartGuider(ui2startGuider);
		detector.setUIAnomalyGuider(start2checkGuider);
		UIAnomalyDetector.DEBUG = true;
		//UIAnomalyMethodFinder.DEBUG = true;
		
		//detect the anomaly chain
		List<AnomalyCallChain> chains = detector.detectUIAnomaly(builder, builder.getCallGraphEntryNodes(uiEntries));
		
		System.out.println("Number of chains: " + chains.size());
		chains = CallChainFilter.filter(chains, new RemoveSubsumedChainStrategy());
		System.out.println("Number of chains after removing subsumption: " + chains.size());
		
		
		//set it back
		UIAnomalyMethodFinder.setCheckingMethods("./src/checking_methods.txt");
		UIAnomalyDetector.DEBUG = false;
		
		return chains;
	}

	public void decryptXML(String apkToolDir, String apkFile, String extractDir) throws IOException {
        ApkUtils.setApkToolDir(apkToolDir);
        
		String resultDir = ApkUtils.decryptXMFiles(apkFile, extractDir);
		assertEquals(resultDir, extractDir);
		
		List<Reader> readers = AndroidUtils.getAllLayoutXMLFromDir(new File(extractDir));
		Collection<String> uis = AndroidUtils.extractAndroidUIs(readers);
		
		System.out.println(uis);
		
		ApkUtils.restoreToDefault();
	}
}
