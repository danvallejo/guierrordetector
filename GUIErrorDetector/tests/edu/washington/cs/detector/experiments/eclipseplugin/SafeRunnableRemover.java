package edu.washington.cs.detector.experiments.eclipseplugin;

import java.util.Iterator;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.util.graph.Graph;

public class SafeRunnableRemover {
	
	//check if node is called by async or sync method
	public static boolean isSafeRunnable(Graph<CGNode> graph, CGNode node) {
		//a quick and dirty tweak
		System.out.println("Check node: " + node);
		IClass c = node.getMethod().getDeclaringClass();
		boolean isInner = c.getName().toString().indexOf("$") != -1;
		if(isInner) {
			Iterator<CGNode> itNode = graph.getPredNodes(node);
			while (itNode.hasNext()) {
				CGNode pred = itNode.next();
//				m
				System.out.println("   The pred node: " + pred);
			}
		}
		return true;
	}
	
}