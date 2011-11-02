package edu.washington.cs.detector.experiments.filters;

import java.util.LinkedList;
import java.util.List;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.ipa.callgraph.CGNode;

import edu.washington.cs.detector.AnomalyCallChain;
import edu.washington.cs.detector.FilterStrategy;
import edu.washington.cs.detector.util.Utils;

public class RemoveSystemCallStrategy extends FilterStrategy {
	
	private static String[] system_classes = new String[] {"Ljava/lang/String",
		"Ljava/io/PrintStream", "Ljava/lang/System"};
	
	public static void setSystemClasses(String[] classes) {
		assert classes != null;
		system_classes = classes;
	}

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
	
	//a system call occurs before calling thread.start()
	protected boolean containSystemCall(AnomalyCallChain chain) {
		boolean seeThreadStart = false;
		for(CGNode node : chain.getFullCallChain()) {
			IClass klass = node.getMethod().getDeclaringClass();
			
			if(node.getMethod().getSignature().equals(thread_start_method)) {
				seeThreadStart = true;
			}
			
			String className = klass.getName().toString();
			//System.err.println(className);
			if(Utils.<String>includedIn(className, system_classes) && !seeThreadStart) {
				return true;
			}
		}
		return false;
	}
}
