package edu.washington.cs.detector.experiments.search;

import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.util.graph.Graph;

import edu.washington.cs.detector.MethodEvaluator;
import edu.washington.cs.detector.util.Files;
import edu.washington.cs.detector.util.Utils;

public class UIAnomalySearcher extends ExhaustiveSearcher {
	
	private MethodEvaluator evaluator = null;
	
	private ClassHierarchy cha;
	
	private String[] checking_methods
    = Files.readWholeNoExp("./src/checking_methods.txt").toArray(new String[0]);

	public UIAnomalySearcher(Graph<CGNode> graph, CGNode startNode) {
		super(graph, startNode);
	}
	
	public void setMethodEvaluator(MethodEvaluator evl) {
		System.err.println("Use method evaluator to identify UI-accessing method: " + evl);
		evaluator = evl;
	}
	
	public void setClassHierarchy(ClassHierarchy cha) {
		this.cha = cha;
	}
	
	public void setCheckingMethods(String[] checkings) {
		this.checking_methods = checkings;
	}
	
	@Override
	public boolean isDestNode(CGNode node) {
		IMethod m = node.getMethod();
		String methodSig = m.getSignature();
		//System.out.println(methodSig);
		if(Utils.<String>includedIn(methodSig, checking_methods) ) {
			return true;
		} else if(evaluator != null) {
			if(this.cha == null) {
				throw new RuntimeException("If the evaluator is set, ClassHierarchy can not be null.");
			}
			return evaluator.isThreadUnsafeMethod(this.cha, m);
		} else {
			return false;
		}
	}

}
