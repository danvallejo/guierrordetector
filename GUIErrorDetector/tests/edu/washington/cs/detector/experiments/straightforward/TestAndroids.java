package edu.washington.cs.detector.experiments.straightforward;

import java.util.Collection;

import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.util.graph.Graph;

import edu.washington.cs.detector.experiments.straightforward.UnsafeUIAccessMethodFinder.GUI;
import edu.washington.cs.detector.util.Utils;

public class TestAndroids {

	/**
	 * modified the experiment driver to avoid repetition
	 * */
    public static void seeNaiveResult(Graph<CGNode> graph, ClassHierarchy cha, String[] packages) {
    	UnsafeUIAccessMethodFinder finder = new UnsafeUIAccessMethodFinder(graph, packages);
		finder.setClassHierarchy(cha);
		finder.setUIAccessDecider(GUI.Android);
		Collection<IMethod> unsafe = finder.finalAllUnsafeUIAccessMethods();
		
		System.out.println("No of unsafe: " + unsafe.size());
		Utils.dumpCollection(unsafe, "./logs/eclipsepluginnaive.txt");
	}
}
