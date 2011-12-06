package edu.washington.cs.detector.experiments.filters;

import java.util.LinkedList;
import java.util.List;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.ipa.callgraph.CGNode;

import edu.washington.cs.detector.AnomalyCallChain;
import edu.washington.cs.detector.FilterStrategy;
import edu.washington.cs.detector.util.WALAUtils;

public class RemoveNonClientHeadStrategy extends FilterStrategy  {
	
	/**
	 * Package names are in the form of a.b.c.d
	 * Default package is ""
	 * */
	private final String[] clientPackageNames;
	
	public RemoveNonClientHeadStrategy(String[] packages) {
		this.clientPackageNames = packages;
	}

	@Override
	public List<AnomalyCallChain> filter(List<AnomalyCallChain> chains) {
		List<AnomalyCallChain> result = new LinkedList<AnomalyCallChain>();
		for(AnomalyCallChain chain : chains) {
			if(!this.shouldRemove(chain)) {
				result.add(chain);
			}
		}
		return result;
	}
	
	private boolean shouldRemove(AnomalyCallChain chain) {
		List<CGNode> nodes = chain.getFullCallChain();
		
		if(nodes.size() < 2) {
			throw new RuntimeException("The chain size should never < 2");
		}
		boolean hasClientClass = false;
		CGNode node = nodes.get(0);
		IClass clazz = node.getMethod().getDeclaringClass();
		String packageName = WALAUtils.getJavaPackageName(clazz);
		for(String pName : this.clientPackageNames) {
			if(packageName.startsWith(pName)) {
				hasClientClass = true;
				break;
			}
		}
		
		return !hasClientClass;
	}
}
