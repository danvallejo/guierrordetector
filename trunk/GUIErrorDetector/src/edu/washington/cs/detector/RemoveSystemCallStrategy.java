package edu.washington.cs.detector;

import java.util.LinkedList;
import java.util.List;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.ipa.callgraph.CGNode;

import edu.washington.cs.detector.util.Utils;

public class RemoveSystemCallStrategy extends FilterStrategy {
	
	public static String[] system_classes = new String[] {"Ljava/lang/String", "Ljava/io/PrintStream"};

	@Override
	public List<AnomalyCallChain> filter(List<AnomalyCallChain> chains) {
		List<AnomalyCallChain> result = new LinkedList<AnomalyCallChain>();
		for(AnomalyCallChain callChain : chains) {
			if(!this.containSystemCall(callChain)) {
				result.add(callChain);
			}
		}
		return result;
	}
	
	protected boolean containSystemCall(AnomalyCallChain chain) {
		for(CGNode node : chain.getFullCallChain()) {
			IClass klass = node.getMethod().getDeclaringClass();
			
			String className = klass.getName().toString();
			//System.err.println(className);
			if(Utils.<String>includedIn(className, system_classes)) {
				return true;
			}
		}
		return false;
	}
}
