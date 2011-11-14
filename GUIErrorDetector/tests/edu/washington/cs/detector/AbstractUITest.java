package edu.washington.cs.detector;

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

import edu.washington.cs.detector.util.Files;
import edu.washington.cs.detector.util.Globals;
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
		
		/** see UI2start reachable, for testing purpose */
		//this.testFindAnomalyCallChains(builder.getCallGraph(), uiEntryNodes, "ClientConnection, localConnect");
		
		List<AnomalyCallChain> chains = detector.detectUIAnomaly(builder, uiEntryNodes);
		System.out.println("Number of anomaly call chains: " + chains.size());
		
		return chains;
	}
	
	/**
	 * A few repetitions below
	 * */
	public List<AnomalyCallChain> checkPathValidity(String outputFilePath,
			CGBuilder.CG opt, String startNodeSig, String[] pathNodeSigs)
			throws IOException, ClassHierarchyException {
		String appPath = TestCommons.assemblyAppPath(getAppPath(), getDependentJars());
		CGBuilder builder = new CGBuilder(appPath);
		builder.makeScopeAndClassHierarchy();
		// find all UI classes
		ClassHierarchy cha = builder.getClassHierarchy();
		List<String> uiClasses = new LinkedList<String>();
		for (IClass kclass : cha) {
			if (this.isUIClass(kclass)) {
				uiClasses.add(WALAUtils.getJavaFullClassName(kclass));
			}
		}
		// find all entry classes
		List<String> entryClasses = new LinkedList<String>();
		for (IClass kclass : cha) {
			if (this.isEntryClass(kclass)) {
				entryClasses.add(WALAUtils.getJavaFullClassName(kclass));
			}
		}
		//get all entry points
		Iterable<Entrypoint> entries = CGEntryManager.getAllPublicMethods(
				builder, entryClasses);
		//set cg type
		if (opt != null) {
			builder.setCGType(opt);
		}
		builder.buildCG(entries);

		Iterable<Entrypoint> queryUIEntries = CGEntryManager
				.getAllPublicMethods(builder, uiClasses);
		Collection<CGNode> uiEntryNodes = builder
				.getCallGraphEntryNodes(queryUIEntries);
		
		//get the matched ui nodes
		List<CGNode> matchedUINodes = new LinkedList<CGNode>();
		for(CGNode uiEntryNode : uiEntryNodes) {
			if(uiEntryNode.toString().indexOf(startNodeSig) != -1) {
				matchedUINodes.add(uiEntryNode);
			}
		}
		
		if(matchedUINodes.isEmpty()) {
			throw new RuntimeException("NO nodes matched for: " + startNodeSig);
		}
		
		//get entry nodes
		List<AnomalyCallChain> chains = new LinkedList<AnomalyCallChain>();
		for(CGNode startNode : matchedUINodes) {
		     AnomalyCallChain chain = AnomalyCallChain.extractCallChainNodeByNode(builder.getAppCallGraph(), startNode, pathNodeSigs);
		     if(chain != null) {
		    	 chains.add(chain);
		     }
		}
		

		return chains;
	}
	
	private void testFindAnomalyCallChains(CallGraph cg, Collection<CGNode> entries, String filterSig) throws IOException {
		
		Collection<CGNode> filteredEntries = new LinkedList<CGNode>();
		for(CGNode entry : entries) {
			if(entry.toString().indexOf(filterSig) != -1) {
				filteredEntries.add(entry);
			}
		}
		
		Set<CGNode> uniqueStarts = new LinkedHashSet<CGNode>();
		
		StringBuilder sb = new StringBuilder();
		sb.append(filteredEntries.size() + " entries in total.");
		sb.append(Globals.lineSep);
		int count = 0;
		for(CGNode entry : filteredEntries) {
		    ThreadStartFinder finder = new ThreadStartFinder(cg, entry);
            Collection<CallChainNode> reachableStarts = finder.getReachableThreadStarts();
            
            uniqueStarts.addAll(CallChainNode.getUnderlyingCGNodes(reachableStarts));
            
            sb.append(count++ + "-th entry: " + entry);
			sb.append(Globals.lineSep);
			sb.append("   has " + reachableStarts.size() + " reachable starts.");
			sb.append(Globals.lineSep);
			
			int startNum = 0;
			for(CallChainNode start : reachableStarts) {
				sb.append("  num: " + startNum++ + " Thread.start()");
				sb.append(Globals.lineSep);
				sb.append(start.getChainToRootAsStr());
				sb.append(Globals.lineSep);
				
				if(start.getChainToRootAsStr().indexOf("Class, getClassLoader()") != -1) {
					sb.append("   --- ignored...");
					sb.append(Globals.lineSep);
					continue;
				}
				
				List<CallChainNode> start2checks = this.getStart2CheckNode(cg, start.getNode());
				sb.append(" ====== the number of call chains: " + start2checks.size() + ", for start with id: " + System.identityHashCode(start.getNode()));
				sb.append(Globals.lineSep);
				int num = 0;
				for(CallChainNode node : start2checks) {
					sb.append("== Num: " + num++ + "-th node");
					sb.append(Globals.lineSep);
					sb.append(node.getChainToRootAsStr());
					sb.append(Globals.lineSep);
				}
			}
		}
		Files.writeToFile(sb.toString(), "./logs/reachable_starts.txt");
		System.out.println("Num of unique starts: " + uniqueStarts.size());
		Utils.dumpCollection(uniqueStarts, "./logs/unique_thread_starts.txt");
	}
	
	private List<CallChainNode> getStart2CheckNode(Graph<CGNode> cg, CGNode startNode) {
		AnomalyFinder anomalyMethodFinder = new UIAnomalyMethodFinder(cg, startNode);
		List<CallChainNode> chains = anomalyMethodFinder.findThreadUnsafeUINodes();
		return chains;
	}
}
