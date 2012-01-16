package edu.washington.cs.detector;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.util.graph.Graph;

import edu.washington.cs.detector.guider.CGTraverseGuider;
import edu.washington.cs.detector.util.Log;
import edu.washington.cs.detector.util.Utils;
import edu.washington.cs.detector.util.WALAUtils;

public class UIAccessRunnableFinder {
	
	private Graph<CGNode> cg = null;
	private ClassHierarchy cha = null;
	private CGTraverseGuider guider = null;
	private NativeMethodConnector connector = null;
	private String[] packages = null;
	// = null;
	
	/**
	 * Here is the algorithm outline:
	 * 1. start from each Runnable to perform traverse
	 * 2. record the path if the runnable accesses the UI
	 * */
	public UIAccessRunnableFinder(Graph<CGNode> cg, ClassHierarchy cha, CGTraverseGuider guider,
			NativeMethodConnector connector, String[] packages) {
		this.cg = cg;
		this.cha = cha;
		this.guider = guider;
		this.connector = connector;
		this.packages = packages;
	}
	
	public Collection<AnomalyCallChain> findAllUIAccessingRunnables(Collection<CGNode> startNodes) {
		
		Collection<CGNode> reachableRuns = new HashSet<CGNode>();
		
		Collection<AnomalyCallChain> retChains = new HashSet<AnomalyCallChain>();
		
		//Collection<CGNode> startNodes = getAllAppRunMethods(cg, cha, packages);
		for(CGNode startNode : startNodes) {
			UIAnomalyMethodFinder finder = UIAnomalyMethodFinder.createInstance(cha, cg, startNode, guider, connector);
			List<CallChainNode> list = finder.findThreadUnsafeUINodes();
			if(!list.isEmpty()) {
				reachableRuns.add(startNode);
			}
			for(CallChainNode node : list) {
				AnomalyCallChain chain = new AnomalyCallChain();
				List<CGNode> listToRoot = node.getChainToRoot();
				chain.addNodes(Collections.EMPTY_LIST, node.getNode(), listToRoot);
				retChains.add(chain);
			}
		}
		
		Log.logln("The number UI reachable node: " + reachableRuns.size());
		for(CGNode run : reachableRuns) {
			Log.logln("   reach UI: " + run);
		}
		
		return retChains;
	}
	
	public static Collection<CGNode> getAllAppRunMethods(Graph<CGNode> cg, ClassHierarchy cha, String[] packages) {
		Collection<CGNode> nodes = new HashSet<CGNode>();
		IClass runnable = WALAUtils.getRunnable(cha);
		for(CGNode node : cg) {
			IMethod m = node.getMethod();
			IClass c = m.getDeclaringClass();
			if(!WALAUtils.isAppClass(c)) {
				continue;
			}
			if(packages != null) {
				if(!WALAUtils.isClassInPackages(c, packages)) {
					continue;
				}
			}
			//find all runnables
			if(cha.isAssignableFrom(runnable, c)) {
				if(m.getName().toString().equals("run") && m.getNumberOfParameters() == 1 /* the parameter includes this*/) {
					nodes.add(node);
				}
			}
		}
		
		return nodes;
	}
}