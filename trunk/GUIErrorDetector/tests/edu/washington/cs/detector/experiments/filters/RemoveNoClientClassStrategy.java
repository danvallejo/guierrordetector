package edu.washington.cs.detector.experiments.filters;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.ipa.callgraph.CGNode;

import edu.washington.cs.detector.AnomalyCallChain;
import edu.washington.cs.detector.FilterStrategy;
import edu.washington.cs.detector.util.WALAUtils;

public class RemoveNoClientClassStrategy extends FilterStrategy {
	
	/**
	 * Package names are in the form of a.b.c.d
	 * Default package is ""
	 * */
	private final String[] clientPackageNames;
	
	private boolean onlyLookAfterStart = false;
	
	public RemoveNoClientClassStrategy(String[] packageNames) {
		this(packageNames, false);
	}
	
	public RemoveNoClientClassStrategy(String[] packageNames, boolean onlyAfterStart) {
		this.clientPackageNames = packageNames;
		this.onlyLookAfterStart = onlyAfterStart;
	}
	
	public RemoveNoClientClassStrategy(Collection<String> packageNameList) {
		this.clientPackageNames = packageNameList.toArray(new String[0]);
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
		
		if(this.onlyLookAfterStart) {
			nodes = chain.getStart2Check();
		}
		
		if(nodes.size() < 2) {
			throw new RuntimeException("The chain size should never < 2");
		}
		boolean hasClientClass = false;
		for(int i = 1 /*note, the 0-th node is for sure UI node*/;
		    i < nodes.size(); i++) {
			CGNode node = nodes.get(i);
			IClass clazz = node.getMethod().getDeclaringClass();
			String packageName = WALAUtils.getJavaPackageName(clazz);
			
			for(String pName : this.clientPackageNames) {
				if(packageName.startsWith(pName)) {
					hasClientClass = true;
					break;
				}
			}
			
			if(hasClientClass) {
				break;
			}
		}
		
		return !hasClientClass;
	}
}
