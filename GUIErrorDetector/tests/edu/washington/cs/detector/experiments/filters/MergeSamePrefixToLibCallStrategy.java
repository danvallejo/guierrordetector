package edu.washington.cs.detector.experiments.filters;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.ipa.callgraph.CGNode;

import edu.washington.cs.detector.AnomalyCallChain;
import edu.washington.cs.detector.FilterStrategy;
import edu.washington.cs.detector.util.Log;
import edu.washington.cs.detector.util.WALAUtils;

public class MergeSamePrefixToLibCallStrategy extends FilterStrategy {
	
	private final String[] programPackages;
	
	public MergeSamePrefixToLibCallStrategy(String[] programPackages) {
		this.programPackages = programPackages;
	}

	@Override
	public List<AnomalyCallChain> filter(List<AnomalyCallChain> chains) {
		List<AnomalyCallChain> result = new LinkedList<AnomalyCallChain>();
		
		Map<String, AnomalyCallChain> chainMap = new LinkedHashMap<String, AnomalyCallChain>(); 
			
		for(AnomalyCallChain chain : chains) {
			int index = this.indexOfLastNonlibCall(chain);
			if(index != -1) {
				if(index == chain.size() - 1) {
					throw new Error("The last one can only be lib calls.");
					//continue;
//					
//					result.add(chain);
//					Log.logln("Error? : " + chain.getFullCallChainAsString());
//					continue;
				}
				List<CGNode> entryToNonLibCall = chain.getFullCallChain().subList(0, index + 1);
				String chainStr = AnomalyCallChain.flatCGNodeListWithoutContext(entryToNonLibCall);
				if(!chainMap.containsKey(chainStr)) {
					chainMap.put(chainStr, chain);
				}
				
			}
		}
		
		result.addAll(chainMap.values());
		
		return result;
	}
	
	private int indexOfLastNonlibCall(AnomalyCallChain chain) {
		List<CGNode> list = chain.getFullCallChain();
		if(list.size() < 2) {
			throw new RuntimeException("The list: " + chain.getFullCallChainAsString() + " is too short");
		}
		for(int i = list.size() - 1; i >= 0; i--) {
			CGNode node = list.get(i);
			IClass klazz = node.getMethod().getDeclaringClass();
			String fullPackageName = WALAUtils.getJavaPackageName(klazz);
			if(this.isInLibPackage(fullPackageName)) {
				//return i;
				continue;
			} else {
				return i;
			}
		}
		return -1;
	}
	
	private boolean isInLibPackage(String pName) {
		for(String pn : this.programPackages) {
			if(pName.startsWith(pn)) {
				return false;
			}
		}
		return true;
	}
}
