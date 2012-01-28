package edu.washington.cs.detector.experiments.android;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.classLoader.ShrikeCTMethod;
import com.ibm.wala.core.tests.callGraph.CallGraphTestUtil;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.shrikeCT.InvalidClassFileException;
import com.ibm.wala.util.graph.Graph;

import edu.washington.cs.detector.AnomalyCallChain;
import edu.washington.cs.detector.CGBuilder;
import edu.washington.cs.detector.CGEntryManager;
import edu.washington.cs.detector.CallChainFilter;
import edu.washington.cs.detector.NativeMethodConnector;
import edu.washington.cs.detector.UIAnomalyDetector;
import edu.washington.cs.detector.UIAnomalyMethodFinder;
import edu.washington.cs.detector.CGBuilder.CG;
import edu.washington.cs.detector.experiments.filters.MergeSameEntryToStartPathStrategy;
import edu.washington.cs.detector.experiments.filters.MergeSamePrefixToLibCallStrategy;
import edu.washington.cs.detector.experiments.filters.MergeSameTailStrategy;
import edu.washington.cs.detector.experiments.filters.RemoveSubsumedChainStrategy;
import edu.washington.cs.detector.experiments.filters.RemoveSystemCallStrategy;
import edu.washington.cs.detector.experiments.straightforward.TestAndroids;
import edu.washington.cs.detector.guider.CGTraverseGuider;
import edu.washington.cs.detector.guider.CGTraverseNoSystemCalls;
import edu.washington.cs.detector.guider.CGTraverseOnlyClientRunnableStrategy;
import edu.washington.cs.detector.util.AndroidLogicByPassFileCreator;
import edu.washington.cs.detector.util.AndroidUtils;
import edu.washington.cs.detector.util.ApkUtils;
import edu.washington.cs.detector.util.Files;
import edu.washington.cs.detector.util.Log;
import edu.washington.cs.detector.util.Utils;
import edu.washington.cs.detector.util.WALAUtils;
import junit.framework.TestCase;

public abstract class AbstractAndroidTest extends TestCase {
	
	public static String bypassfiledir = "dat";
	
	private boolean runnaiveapproach = false;
	private String[] packageNames = null;
	private String bypassFileName = null;
	private String androidcheckingfile = null;
	
	public void setRunnaiveApproach(boolean app) {
		this.runnaiveapproach = app;
	}
	public void setPackageNames(String[] packages) {
		this.packageNames = packages;
	}
	public void setByfileName(String fileName) {
		this.bypassFileName = fileName;
	}
	public void setAndroidCheckingFile(String file) {
		this.androidcheckingfile = file;
	}
	
	abstract protected String getAppPath();
	abstract protected String getDirPath();
	
	static Graph<CGNode> appCallGraph = null;
	static ClassHierarchy builtCHA = null;
	
	protected Collection<Entrypoint> getExtraEntrypoints(CGBuilder builder) {
		return Collections.EMPTY_SET;
	}
	
	protected Iterable<Entrypoint> getQuerypoints(CGBuilder builder, Iterable<Entrypoint> points) {
		return points;
	}
	
	
	protected Iterable<Entrypoint> removeRedundantEntries(Iterable<Entrypoint> points) {
		Set<String> sigs = new HashSet<String>();
		Collection<Entrypoint> nonRedundant = new LinkedList<Entrypoint>();
		for(Entrypoint point : points) {
			String methodSig = point.getMethod().getSignature();
			if(!sigs.contains(methodSig)) {
				nonRedundant.add(point);
				sigs.add(methodSig);
			}
		}
		return nonRedundant;
	}
	
	public List<AnomalyCallChain> findErrorsInAndroidApp(CG type, CGTraverseGuider ui2startGuider, CGTraverseGuider start2checkGuider)
	    throws ClassHierarchyException, IOException {
		return findErrorsInAndroidApp(type, ui2startGuider, start2checkGuider, null);
	}
	
	public List<AnomalyCallChain> findErrorsInAndroidApp(CG type, CGTraverseGuider ui2startGuider, CGTraverseGuider start2checkGuider,
			NativeMethodConnector connector)
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
	    
	    Log.logln("Number of runnable loaded in app: " + runnableInApp.size());
	    Log.logln("Number of runnable in client code: " + runnableInClient.size());
	    for(IClass runnableC : runnableInClient) {
	    	Log.logln("   runnable in client: " + runnableC.toString());
	    }
	    
	    List<String> entryClasses = new LinkedList<String>();
	    
	    List<String> activityClasses = new LinkedList<String>();
	    //get all activity classes
	    Collection<IClass> activities = AndroidUtils.getAppActivityClasses(cha);
	    for(IClass activity : activities) {
	    	entryClasses.add(WALAUtils.getJavaFullClassName(activity));
	    	activityClasses.add(WALAUtils.getJavaFullClassName(activity));
	    	Log.logln("Activity class: " + activity);
	    	System.out.println("activity class: " + activity);
	    }
	    //get all listener classess
	    for(IClass c : cha) {
	    	String fullClassName = WALAUtils.getJavaFullClassName(c);
	    	if(AndroidUtils.isInAndroidLib(fullClassName)) {
	    		continue;
	    	}
	    	if(AndroidUtils.isCustomizedListener(cha, fullClassName)) {
	    	    entryClasses.add(fullClassName);
	    	    Log.logln("Listener class: " + fullClassName);
	    	    System.out.println("listener class: " + fullClassName);
	    	}
	    }
	    //get all user defined view (that is potentially been reflectively called by UI)
	    Collection<IClass> appViewClasses = AndroidUtils.getAppViewClasses(cha);
	    List<String> appViews = new LinkedList<String>();
	    for(IClass c : appViewClasses) {
	    	String fullClassName = WALAUtils.getJavaFullClassName(c);
	    	Log.logln("User defined view class: " + fullClassName);
	    	System.out.println("User defined view class: " + fullClassName);
	    	appViews.add(fullClassName);
	    }
	    
	    //remove all redundance
	    Utils.removeRedundant(activityClasses);
	    Utils.removeRedundant(entryClasses);
	    Utils.removeRedundant(appViews);
	    //the entryPoint here is for querying
	    Iterable<Entrypoint> queryPoints = CGEntryManager.getAllPublicMethods(builder, entryClasses);
	    //get all activity call back methods
	    Iterable<Entrypoint> activityCallbacks = CGEntryManager.getAndroidActivityCallBackMethods(builder, activityClasses);
	    //merge these two as the query points
	    queryPoints = CGEntryManager.mergeEntrypoints(queryPoints, activityCallbacks);
	    
	    //get all user defined 
	    Iterable<Entrypoint> appViewEntries = CGEntryManager.getAllPublicMethods(builder, appViews);
	    queryPoints = CGEntryManager.mergeEntrypoints(queryPoints, appViewEntries);
		
	    //get all declared UI classes
		Collection<String> declaredClasses = AndroidUtils.extractAllUIs(cha, new File(getDirPath()));
		System.out.println("Number of declared UI: " + declaredClasses.size());
		System.out.println("     " + declaredClasses);
		
		//add all UI constructors as the entry point
	    Iterable<Entrypoint> widgetConstructors =
	    	new LinkedList<Entrypoint>(); //TODO comment it out
//	    	CGEntryManager.getConstructors(builder, declaredClasses);
	    
	    Iterable<Entrypoint> activityConstructors =
	    	CGEntryManager.getConstructors(builder, activityClasses);
	    
	    //Merge 2 entries
	    Iterable<Entrypoint> entries = CGEntryManager.mergeEntrypoints(queryPoints, widgetConstructors);
	    
	    //merge with activity constructors
	    entries = CGEntryManager.mergeEntrypoints(entries, activityConstructors);
	    
		System.out.println("Number of entries for building CG: " + Utils.countIterable(entries));
		Log.logln("The number of entries for building cg: " + Utils.countIterable(entries));
		for(Entrypoint ep : entries) {
			Log.logln("    - entry point: " + ep);
		}
		
		builder.setCGType(type);
		
		if(this.bypassFileName != null) {
			System.err.println("creating by pass file: " + bypassfiledir + "/" + this.bypassFileName);
			AndroidLogicByPassFileCreator creator = new AndroidLogicByPassFileCreator(null, cha);
			creator.setDefaultDir(bypassfiledir);
			creator.createByPassLogicFile(this.bypassFileName, declaredClasses);
			builder.setByPassFile(this.bypassFileName);
		}
		
		Collection<Entrypoint> extraEntrypoints = this.getExtraEntrypoints(builder);
		if(!extraEntrypoints.isEmpty()) {
			System.err.println("Adding: " + extraEntrypoints.size() + " entrypoints!");
			for(Entrypoint ep : extraEntrypoints) {
				System.err.println("  --  " + ep);
			}
			entries = CGEntryManager.mergeEntrypoints(entries, extraEntrypoints);
			Log.logln("After adding: " + extraEntrypoints.size() + ", we have: " + Utils.countIterable(entries) + " entrypoints in total.");
		}
		
		Log.logln("All entrypoint in building the call graph:");
		for(Entrypoint ep : entries) {
			Log.logln("    " + ep);
		}
		entries = removeRedundantEntries(entries);
		
		Log.logln("After removing all redundant, number of entries: " + Utils.countIterable(entries));
		for(Entrypoint ep : entries) {
			Log.logln("    " + ep);
		}
		
		builder.buildCG(entries);
		
		if(this.runnaiveapproach) {
			TestAndroids.seeNaiveResult(builder.getAppCallGraph(), builder.getClassHierarchy(), packageNames);
			System.err.println("Exit in running naive approach.");
			System.exit(1);
		}
		
		System.out.println("number of entry node in the built CG: " + builder.getCallGraph().getEntrypointNodes().size());
		System.out.println("CG node num: " + builder.getCallGraph().getNumberOfNodes());
		System.out.println("App CG node num: " + builder.getAppCallGraph().getNumberOfNodes());
		
		appCallGraph = builder.getAppCallGraph();
		builtCHA = builder.getClassHierarchy();
		
		Log.logln("app call graph node num: " + appCallGraph.getNumberOfNodes());
		
		
		//set up the anomaly detection
		UIAnomalyDetector detector = new UIAnomalyDetector(getAppPath());
		detector.configureCheckingMethods("./tests/edu/washington/cs/detector/checkingmethods_for_android.txt");
		if(this.androidcheckingfile != null) {
			System.err.println("Use : " + this.androidcheckingfile + " as checking file.");
			detector.configureCheckingMethods(this.androidcheckingfile);
		}
		detector.setThreadStartGuider(ui2startGuider);
		detector.setUIAnomalyGuider(start2checkGuider);
		detector.setNativeMethodConnector(connector);
		UIAnomalyDetector.DEBUG = true;
		//UIAnomalyMethodFinder.DEBUG = true;
		
		queryPoints = getQuerypoints(builder, queryPoints);
		
		//detect the anomaly chain
		List<AnomalyCallChain> chains = detector.detectUIAnomaly(builder, builder.getCallGraphEntryNodes(queryPoints));
		
		System.out.println("Number of chains: " + chains.size());
		chains = CallChainFilter.filter(chains, new RemoveSubsumedChainStrategy());
		System.out.println("Number of chains after removing subsumption: " + chains.size());
		
		chains = Utils.removeRedundantAnomalyCallChains(chains);
		System.out.println("Number of chains after removing redundant: " + chains.size());
		
		chains = CallChainFilter.filter(chains, new RemoveSystemCallStrategy());
		System.out.println("size of chains after removing system calls: " + chains.size());
		
		chains = CallChainFilter.filter(chains, new MergeSameEntryToStartPathStrategy() );
		
		if(packageNames != null) {
			  chains = CallChainFilter.filter(chains, new MergeSamePrefixToLibCallStrategy(packageNames));
			  System.out.println("size of chains after removing same entry nodes to lib: " + chains.size());
		}
		
		chains = CallChainFilter.filter(chains, new MergeSameTailStrategy());
		System.out.println("size of chains after merging the same tails: " + chains.size());
		
		Log.logln("Number of chains: " + chains.size());
		int count = 0;
		for(AnomalyCallChain chain : chains) {
			Log.logln("The " + count++ + "-th call chain:");
			Log.logln(chain.getFullCallChainAsString());
		}
		
		System.out.println("Final output chains: " + chains.size());
		
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
