package edu.washington.cs.detector.experiments.eclipseplugin;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;

import edu.washington.cs.detector.AnomalyCallChain;
import edu.washington.cs.detector.CGBuilder;
import edu.washington.cs.detector.CallChainFilter;
import edu.washington.cs.detector.CallChainNode;
import edu.washington.cs.detector.FilterStrategy;
import edu.washington.cs.detector.TestCommons;
import edu.washington.cs.detector.ThreadStartFinder;
import edu.washington.cs.detector.UIAccessRunnableFinder;
import edu.washington.cs.detector.UIAnomalyDetector;
import edu.washington.cs.detector.UIAnomalyMethodFinder;
import edu.washington.cs.detector.CGBuilder.CG;
import edu.washington.cs.detector.experiments.filters.MergeSamePrefixToLibCallStrategy;
import edu.washington.cs.detector.experiments.filters.MergeSameTailStrategy;
import edu.washington.cs.detector.experiments.filters.RemoveNoClientClassStrategy;
import edu.washington.cs.detector.experiments.filters.RemoveSameEntryStrategy;
import edu.washington.cs.detector.experiments.filters.RemoveSubsumedChainStrategy;
import edu.washington.cs.detector.experiments.filters.RemoveSystemCallStrategy;
import edu.washington.cs.detector.experiments.search.AnomalyCallChainSearcher;
import edu.washington.cs.detector.experiments.search.ExhaustiveSearcher;
import edu.washington.cs.detector.experiments.search.SimpleClock;
import edu.washington.cs.detector.experiments.straightforward.TestEclipsePlugins;
import edu.washington.cs.detector.guider.CGTraverseGuider;
import edu.washington.cs.detector.guider.CGTraverseNoSystemCalls;
import edu.washington.cs.detector.guider.CGTraverseSwingGuider;
import edu.washington.cs.detector.guider.CGTraverseSwingUIAccessGuider;
import edu.washington.cs.detector.util.EclipsePluginUtils;
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
	private String[] packages = null;
	private boolean see_ui_access_runnable = false;
	private boolean runnaiveapproach = false;
	private boolean addrunnable = true;
	private boolean useexhaustivesearch = false;
	
	//these four methods must be overriden
	protected abstract Iterable<Entrypoint> getEntrypoints(ClassHierarchy cha);
	protected abstract Collection<CGNode> getStartNodes(Iterable<CGNode> allNodes, ClassHierarchy cha);
	protected abstract String getAppPath();
	protected abstract String getDependentJars();
	
	protected String getExtraJars() {
		return null;
	}

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
	protected void setPackages(String[] packages) {
		this.packages = packages;
	}
	public final String[] getPackages() {
		return this.packages;
	}
	protected void setSeeUIAccessRunnable(boolean see) {
		this.see_ui_access_runnable = see;
	}
	
	protected void setRunNaiveApproach(boolean runapproach) {
		this.runnaiveapproach = runapproach;
	}
	
	protected void setAddRunnable(boolean runnable) {
		this.addrunnable = runnable;
	}
	
	protected void setExhaustiveSearch(boolean exhaust) {
		this.useexhaustivesearch = exhaust;
	}
	
	public Collection<AnomalyCallChain> reportUIErrors() throws IOException, ClassHierarchyException {
		String appPath = null;
		if(this.completePath != null) {
			appPath = this.completePath;
		} else {
		    appPath =  TestCommons.assemblyAppPath(getAppPath() /*just a path*/, getDependentJars() /*all jars*/);
		    if(this.getExtraJars() != null) {
		    	System.err.println("Extra path: " + this.getExtraJars());
		    	appPath = appPath + Globals.pathSep + this.getExtraJars();
		    }
		}
		System.out.println("App path:\n  " + appPath);
		
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
			builder.setCGType(this.type);
		}
		long cgStart = System.currentTimeMillis();
		builder.buildCG(entries);
		long cgEnd = System.currentTimeMillis();
		System.out.println("CG building time: " + (cgEnd - cgStart));
//		System.exit(1);
		
		if(this.runnaiveapproach) {
			TestEclipsePlugins.seeNaiveResult(builder.getAppCallGraph(), cha, this.getPackages());
			System.err.println("Exit in running the naive approach");
			System.exit(1);
		}
		
		Collection<CGNode> startNodes = this.getStartNodes(builder.getAppCallGraph(), cha);
		System.out.println("Number of starting nodes in the built callg graph: " + startNodes.size());
		Log.logln("Number of starting nodes in the built callg graph: " + startNodes.size());
		for(CGNode node : startNodes) {
			Log.logln("    " + node);
		}
		
		UIAnomalyDetector detector = new UIAnomalyDetector(appPath);
		detector.setThreadStartGuider(new CGTraverseNoSystemCalls());
		detector.setUIAnomalyGuider(new CGTraverseSwingUIAccessGuider()); //XXX
		
		if(this.threadStartGuider != null) {
			detector.setThreadStartGuider(this.threadStartGuider);
		}
		if(this.uiAnomalyGuider != null) {
			detector.setUIAnomalyGuider(this.uiAnomalyGuider);
		}
		
		//note that, the execution will return from here
		List<AnomalyCallChain> chains  = new LinkedList<AnomalyCallChain>();
		
		Collection<CGNode> eventNodes = new LinkedHashSet<CGNode>();
		
		if(this.see_ui_access_runnable) {
			
			//log the call graph
			WALAUtils.logCallGraph(builder.getAppCallGraph());
			
			long startTime = System.currentTimeMillis();
			
			Log.logln("-----All thread runnable ------");
			UIAccessRunnableFinder finder = new UIAccessRunnableFinder(builder.getAppCallGraph(), cha, this.uiAnomalyGuider,
					null, this.getPackages());
			Collection<CGNode> runnables = UIAccessRunnableFinder.getAllAppRunMethods(builder.getAppCallGraph(), cha,
					this.getPackages());
			Utils.logCollection(runnables);
			Collection<AnomalyCallChain> chains1 = finder.findAllUIAccessingRunnables(runnables);
			Log.logln("Number of chains: " + chains1.size());

			Log.logln("--------- job runs -----------");
			Collection<CGNode> jobRuns = EclipsePluginUtils.getAllJobRunMethods(builder.getAppCallGraph(), cha,
					this.getPackages());
			Utils.logCollection(jobRuns);
			Collection<AnomalyCallChain> chains2 = finder.findAllUIAccessingRunnables(jobRuns);
			Log.logln("Number of chains: " + chains2.size());
			
			Log.logln("--------- job public methods -----");
			Collection<CGNode> jobMethods = EclipsePluginUtils.getAllJobPublicProtectMethods(builder.getAppCallGraph(), cha,
					this.getPackages());
			Utils.logCollection(jobMethods);
			Collection<AnomalyCallChain> chains3 = finder.findAllUIAccessingRunnables(jobMethods);
			Log.logln("Number of chains: " + chains3.size());
			
			Log.logln("--------- progress monitor -------");
			Collection<CGNode> monitorMethods = EclipsePluginUtils.getAllMonitorMethods(builder.getAppCallGraph(), cha,
					this.getPackages());
			Utils.logCollection(monitorMethods);
			Collection<AnomalyCallChain> chains4 = finder.findAllUIAccessingRunnables(monitorMethods);
			Log.logln("Number of chains: " + chains4.size());
			
			Log.logln("--------- Resource change methods -------");
			Collection<CGNode> resourceChangeMethods = EclipsePluginUtils.getAllResourceChangeMethods(builder.getAppCallGraph(),
					cha, this.getPackages());
			Utils.logCollection(resourceChangeMethods);
			Collection<AnomalyCallChain> chains5 = finder.findAllUIAccessingRunnables(resourceChangeMethods);
			Log.logln("Number of chains: " + chains5.size());
			
			Log.logln("--------- Action run number -------");
			System.out.println("Action run number");
			Collection<CGNode> actionMethods = EclipsePluginUtils.getAllActionRunMethods(builder.getAppCallGraph(), cha,
					this.getPackages());
			Utils.logCollection(actionMethods);
			Collection<AnomalyCallChain> chains6 = finder.findAllUIAccessingRunnables(actionMethods);
			
			Log.logln("--------- Job change methods --------");
			Collection<CGNode> jobChangeMethods = EclipsePluginUtils.getAllJobChangeMethods(builder.getAppCallGraph(), cha,
					this.getPackages());
			Utils.logCollection(jobChangeMethods);
			Collection<AnomalyCallChain> chains7 = finder.findAllUIAccessingRunnables(jobChangeMethods);
			
			Log.logln("--------- Launch config listeners -----");
			Collection<CGNode> configMethods = EclipsePluginUtils.getAllLaunchConfigMethods(builder.getAppCallGraph(), cha,
					this.getPackages());
			Collection<AnomalyCallChain> chains8 = finder.findAllUIAccessingRunnables(configMethods);
			
			long endTime = System.currentTimeMillis();
			
			System.out.println("Time cost in detecting anomalies in normal search: " + (endTime - startTime) + " millis");
			Log.logln("Time cost in detecting anomalies in normal search: " + (endTime - startTime) + " millis");
			
			if(this.addrunnable) {
			    chains.addAll(chains1);
			}
			chains.addAll(chains2);
			chains.addAll(chains3);
			chains.addAll(chains4);
			chains.addAll(chains5);
			//chains.addAll(chains6);
			chains.addAll(chains7);
			chains.addAll(chains8);
			
			//keep a copy of event nodes
//			eventNodes.addAll(runnables);
			eventNodes.addAll(jobRuns);
			eventNodes.addAll(jobMethods);
			eventNodes.addAll(monitorMethods);
			eventNodes.addAll(resourceChangeMethods);
			eventNodes.addAll(actionMethods);
			eventNodes.addAll(jobChangeMethods);
			eventNodes.addAll(configMethods);
		} else {
		    chains = detector.detectUIAnomaly(builder, startNodes);
		}
		
		System.out.println("Number of anomaly call chains: " + chains.size());
		
		chains = this.filterCallChains(chains, startNodes);
		
		if(this.useexhaustivesearch) {
			System.out.println("starting exhaustive search....");
			WALAUtils.showGraphStats(builder.getAppCallGraph());
			
			AnomalyCallChainSearcher searcher = new AnomalyCallChainSearcher(builder.getAppCallGraph(), eventNodes);
			searcher.setUiGuider(this.uiAnomalyGuider);
			
			long searchStart = System.currentTimeMillis();
			
			ExhaustiveSearcher.USE_CLOCK = true;
			SimpleClock.start();
			SimpleClock.setBudget(1200000*3); //20 mins
			
			Collection<List<CGNode>> result = searcher.findUIAnomalyCallChains();
			
			SimpleClock.reset();
			ExhaustiveSearcher.USE_CLOCK = false;
			
			long searchEnd = System.currentTimeMillis();
			
			System.out.println("Number of anomaly call chains in exhaustive search: " + result.size());
			System.out.println("Search time: " + (searchEnd - searchStart) + " mills");
			Log.logln("Search time: " + (searchEnd - searchStart) + " mills");
			
			List<AnomalyCallChain> initialChains = new LinkedList<AnomalyCallChain>();
			for(List<CGNode> list : result) {
				AnomalyCallChain c = new AnomalyCallChain();
				c.addNodes(new LinkedList<CGNode>(), list.get(0), list);
				initialChains.add(c);
			}
			System.out.println("---------- filtering all chains from exhaustive search -------");
			chains = this.filterCallChains(initialChains, startNodes);
		}
		
		return chains;
	}
	
	private List<AnomalyCallChain> filterCallChains(List<AnomalyCallChain> chains, Collection<CGNode> startNodes) {
		chains = CallChainFilter.filter(chains, new RemoveSubsumedChainStrategy(startNodes));
		
		chains = Utils.removeRedundantAnomalyCallChains(chains);
//		System.out.println("size of chains after removing redundancy: " + chains.size());
		System.out.println("size of chains after removing subsumed calls: " + chains.size());
		
		chains = CallChainFilter.filter(chains, new RemoveSystemCallStrategy());
		System.out.println("size of chains after removing system calls: " + chains.size());
		
		
		if(packages != null) {
		    chains = CallChainFilter.filter(chains, new RemoveNoClientClassStrategy(packages, true));
		    //System.out.println("size of chains after removing no client classes after start: " + chains.size());
		}
		
		chains = CallChainFilter.filter(chains, new RemoveSameEntryStrategy());
		System.out.println("size of chains after removing the same entry but keeping the shortest chain: " + chains.size());
		
		if(packages != null) {
		  chains = CallChainFilter.filter(chains, new MergeSamePrefixToLibCallStrategy(packages));
		  System.out.println("size of chains after removing same entry nodes to lib: " + chains.size());
		}
		
		chains = CallChainFilter.filter(chains, new MergeSameTailStrategy());
		System.out.println("size of chains after removing the same tail keeping the shortest chain: " + chains.size());
		
		return chains;
	}
}
