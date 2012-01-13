package edu.washington.cs.detector.experiments.filters;

import java.util.LinkedList;
import java.util.List;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.cha.ClassHierarchy;

import edu.washington.cs.detector.AnomalyCallChain;
import edu.washington.cs.detector.FilterStrategy;
import edu.washington.cs.detector.util.SwingUtils;
import edu.washington.cs.detector.util.WALAUtils;

public class MatchRunWithInvokeClassStrategy extends FilterStrategy {
	
	ClassHierarchy cha = null;
	
	public MatchRunWithInvokeClassStrategy(ClassHierarchy cha) {
		this.cha = cha;
	}

	@Override
	public List<AnomalyCallChain> filter(List<AnomalyCallChain> chains) {
		List<AnomalyCallChain> result = new LinkedList<AnomalyCallChain>();
		
		for(AnomalyCallChain chain : chains) {
			if(this.runMatchInvoked(chain)) {
				result.add(chain);
			}
		}
		
		return result;
	}
	
	private boolean runMatchInvoked(AnomalyCallChain chain) {
		List<CGNode> ui2start = chain.getUI2Start();
		List<CGNode> start2error = chain.getStart2Check();
		
		String runClass = null;
		for(CGNode node : start2error) {
			IMethod m = node.getMethod();
			if(m.getName().toString().equals("run") && this.cha.isAssignableFrom(SwingUtils.getRunnable(this.cha), m.getDeclaringClass())) { //maybe imprecise
				runClass = WALAUtils.getJavaFullClassName(m.getDeclaringClass());
				break;
			}
		}
		
		if(runClass == null) {
			throw new Error("Null: " + chain.getFullCallChainAsString());
		}
		
		//in the reverse order
		for(int i = ui2start.size() - 1; i >= 0; i--) {
			IClass c = ui2start.get(i).getMethod().getDeclaringClass();
			if(WALAUtils.getJavaFullClassName(c).equals(runClass)) {
				return true;
			}
		}
		
		return false;
	}

}
