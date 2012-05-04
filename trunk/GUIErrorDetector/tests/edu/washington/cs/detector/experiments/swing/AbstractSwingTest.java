package edu.washington.cs.detector.experiments.swing;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.util.io.FileProvider;

import edu.washington.cs.detector.AnomalyCallChain;
import edu.washington.cs.detector.CGBuilder;
import edu.washington.cs.detector.CGBuilder.CG;
import edu.washington.cs.detector.CGEntryManager;
import edu.washington.cs.detector.CallChainFilter;
import edu.washington.cs.detector.SwingUIMethodEvaluator;
import edu.washington.cs.detector.ThreadStartFinder;
import edu.washington.cs.detector.UIAnomalyDetector;
import edu.washington.cs.detector.UIAnomalyMethodFinder;
import edu.washington.cs.detector.experimental.InvalidThreadAccessDetector;
import edu.washington.cs.detector.experiments.filters.MatchRunWithInvokeClassStrategy;
import edu.washington.cs.detector.experiments.filters.MergeSamePrefixToLibCallStrategy;
import edu.washington.cs.detector.experiments.filters.RemoveNoClientClassStrategy;
import edu.washington.cs.detector.experiments.filters.RemoveSameEntryStrategy;
import edu.washington.cs.detector.experiments.filters.RemoveSubsumedChainStrategy;
import edu.washington.cs.detector.experiments.filters.RemoveSystemCallStrategy;
import edu.washington.cs.detector.guider.CGTraverseGuider;
import edu.washington.cs.detector.guider.CGTraverseSwingGuider;
import edu.washington.cs.detector.guider.CGTraverseSwingUIAccessGuider;
import edu.washington.cs.detector.util.Log;
import edu.washington.cs.detector.util.SwingUtils;
import edu.washington.cs.detector.util.Utils;
import edu.washington.cs.detector.util.WALAUtils;
import junit.framework.TestCase;

public abstract class AbstractSwingTest extends TestCase {
	
	private boolean use_non_default_cg = false;
	
	private boolean add_handlers = false;
	
	private boolean add_extra_ep = false;
	
	private boolean match_run_with_invoke = false;
	
	private boolean prune_entrypoint = false;
	
	private boolean tweak_startnode = false;
	
	private CG type = null;
	
	private CGTraverseGuider threadStartGuider = null;
	
	private CGTraverseGuider uiAnomalyGuider = null;
	
	private boolean useexhaustivesearch = false;
	
	private boolean useworklist = false;
	
	protected void setCGType(CG t) {
		type = t;
	}
	
	public void setNondefaultCG(boolean nonDefault) {
		this.use_non_default_cg = nonDefault;
	}
	
	protected void setAddHandlers(boolean add) {
		this.add_handlers = add;
	}
	
	protected void setAddExtraEntrypoints(boolean addEP) {
		this.add_extra_ep = addEP;
	}
	
	protected void setMatchRunWithInvoke(boolean match) {
		this.match_run_with_invoke = match;
	}
	
	protected void setPruneEntrypoint(boolean prune) {
		this.prune_entrypoint = prune;
	}
	
	protected void setTweakStartnode(boolean tweak) {
		this.tweak_startnode = tweak;
	}
	
	protected void setExhaustiveSearch(boolean exhaust) {
		this.useexhaustivesearch = exhaust;
	}
	
	protected void setUseWorklist(boolean worklist) {
		this.useworklist = worklist;
	}
	
	protected Iterable<Entrypoint> getAdditonalEntrypoints(ClassHierarchy cha) {
		return Collections.emptySet();
	}
	
	protected Iterable<Entrypoint> pruneEntrypoints(Iterable<Entrypoint> eps) {
		return eps;
	}
	
	protected void setThreadStartGuider(CGTraverseGuider guider) {
		this.threadStartGuider = guider;
	}
	
	protected void setUIAnomalyGuider(CGTraverseGuider guider) {
		this.uiAnomalyGuider = guider;
	}
	
	protected Collection<CGNode> tweakStartNode(Collection<CGNode> nodes, ClassHierarchy cha) {
		return nodes;
	}
	
	protected void checkCallChainNumber(int expectedNum, String appPath, String[] packages /*for filtering*/ ) throws IOException,
	    ClassHierarchyException {
		
		//start to detect errors
		long start = System.currentTimeMillis();
		
		UIAnomalyDetector detector = new UIAnomalyDetector(appPath);
		
//		InvalidThreadAccessDetector detector = new InvalidThreadAccessDetector(appPath);
		
		/** a few configurations to configure the tool for swing*/
		if(type != null) {
			detector.setDefaultCGType(type);
		}
		detector.setExclusionFile(UIAnomalyDetector.EXCLUSION_FILE_SWING);
		detector.configureCheckingMethods("./tests/edu/washington/cs/detector/checkingmethods_for_swing.txt");
		detector.setAnomalyMethodEvaluator(new SwingUIMethodEvaluator());
		detector.setThreadStartGuider(new CGTraverseSwingGuider());
		detector.setUIAnomalyGuider(new CGTraverseSwingUIAccessGuider());
		
		//reset
		if(this.threadStartGuider != null) {
			detector.setThreadStartGuider(this.threadStartGuider);
		}
		if(this.uiAnomalyGuider != null) {
			detector.setUIAnomalyGuider(this.uiAnomalyGuider);
		}
		
		CGBuilder builder = null;
		if(!use_non_default_cg) {
			//already build the call graph when the default cg builder is returned
		    builder = detector.getDefaultCGBuilder();
		} else {
			//extract all event handling method
			builder = new CGBuilder(detector.getAppPath(), FileProvider.getFile(detector.getExclusionFile()));
			if(type != null) {
				builder.setCGType(type);
			}
			builder.makeScopeAndClassHierarchy();
			Iterable<Entrypoint> eps = com.ibm.wala.ipa.callgraph.impl.Util.makeMainEntrypoints(builder.getAnalysisScope(),
					builder.getClassHierarchy());
			//add handlers
			if(this.add_handlers) {
			    Iterable<Entrypoint> eventHandlers = SwingUtils.getAllAppEventhandlingMethodsAsEntrypoints(builder.getClassHierarchy(), packages);
			    eps = CGEntryManager.mergeEntrypoints(eps, eventHandlers);
			}
			//also add some additional entry points here
			if(this.add_extra_ep) {
			    eps = CGEntryManager.mergeEntrypoints(eps, this.getAdditonalEntrypoints(builder.getClassHierarchy()));
			}
			//need to do final prune?
			if(this.prune_entrypoint) {
				eps = this.pruneEntrypoints(eps);
			}
			
			//start build call graph
			long cgStart = System.currentTimeMillis();
			builder.buildCG(eps);
			long cgEnd = System.currentTimeMillis();
			System.out.println("Measure cg time: " + (cgEnd - cgStart));
//			System.exit(1);
		}
		
		//System.exit(1);
		
		//entry nodes in CG
		Iterable<Entrypoint> entries = builder.getEntrypoints();
		Log.logln("The entry point for CG: " + Utils.countIterable(entries));
		for(Entrypoint entry : entries) {
			Log.logln("   " + entry);
		}
		
		//get all entries
		List<AnomalyCallChain> chains = null;
		
		Collection<CGNode> nodes = SwingUtils.getAllAppEventhandlingMethods(builder.getAppCallGraph(),
				builder.getClassHierarchy());
		//filter CGNodes within the given packages
		if(packages != null && packages.length > 0) {
			Log.logln("CGNode before filtering: " + nodes.size());
			nodes = WALAUtils.filterCGNodeByPackages(nodes, packages);
			Log.logln("CGNode after filtering: " + nodes.size());
		}
		
		Log.logln("Number of CG nodes: " + builder.getAppCallGraph().getNumberOfNodes());
		Log.logln("The CG nodes to start traversing: " + nodes.size());
		for(CGNode node : nodes) {
			Log.logln("   " + node);
		}
		
		if(this.tweak_startnode) {
			nodes = this.tweakStartNode(nodes, builder.getClassHierarchy()); 
		}

		
		chains = detector.detectUIAnomaly(builder, nodes);
		
		
		System.out.println("size of chains before removing redundancy: " + chains.size());
		chains = Utils.removeRedundantAnomalyCallChains(chains);
		System.out.println("size of chains after removing redundancy: " + chains.size());
		
		
		chains = CallChainFilter.filter(chains, new RemoveSubsumedChainStrategy(nodes));
		System.out.println("size of chains after removing subsumed calls: " + chains.size());
		
		chains = CallChainFilter.filter(chains, new RemoveSystemCallStrategy());
		System.out.println("size of chains after removing system calls: " + chains.size());
		
		if(packages != null) {
		    chains = CallChainFilter.filter(chains, new RemoveNoClientClassStrategy(packages, true));
		    System.out.println("size of chains after removing no client classes after start: " + chains.size());
		}
		
		chains = CallChainFilter.filter(chains, new RemoveSameEntryStrategy());
		System.out.println("size of chains after removing the same entry but keeping the shortest chain: " + chains.size());
		
		if(packages != null) {
		  chains = CallChainFilter.filter(chains, new MergeSamePrefixToLibCallStrategy(packages));
		  System.out.println("size of chains after removing same entry nodes to lib: " + chains.size());
		}
		
		if(this.match_run_with_invoke) {
			chains = CallChainFilter.filter(chains, new MatchRunWithInvokeClassStrategy(builder.getClassHierarchy()));
			  System.out.println("size of chains after matching run with invoke: " + chains.size());
		}
		
		int count = 0;
		for(AnomalyCallChain chain : chains) {
			Log.logln("The " + (count++) + "-th chain");
			Log.logln(chain.getFullCallChainAsString());
		}
		if(expectedNum != -1) {
		    assertEquals("The number of expected call chain is wrong", expectedNum, chains.size());
		}
		
		long end = System.currentTimeMillis();
		
		System.out.println("Total time cost: " + (start - end) + "-millis");
		
		//resort the state to move dependent tests
		UIAnomalyMethodFinder.setMethodEvaluator(null);
		UIAnomalyMethodFinder.setCheckingMethods("./src/checking_methods.txt");
	}
}
