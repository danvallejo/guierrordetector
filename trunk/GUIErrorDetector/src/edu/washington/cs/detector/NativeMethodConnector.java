package edu.washington.cs.detector;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.util.graph.Graph;

import edu.washington.cs.detector.util.WALAUtils;

/**
 * A user specified annotation to connect native method with its callbacks
 * */
public class NativeMethodConnector {
	
	//full method name
	private Map<String, Collection<String>> methodMappings = new LinkedHashMap<String, Collection<String>>();
	
	public void addNativeMethodMapping(String nativeMethod, String callbackMethod) {
		if(!methodMappings.containsKey(nativeMethod)) {
			methodMappings.put(nativeMethod, new LinkedHashSet<String>());
		}
		methodMappings.get(nativeMethod).add(callbackMethod);
	}
	
	public Collection<String> getCallBacks(String nativeMethod) {
		return methodMappings.get(nativeMethod);
	}
	
	public boolean isEmpty() {
		return methodMappings.isEmpty();
	}
	
	//returns empty, but not null.
	public Collection<CGNode> getSucc(Graph<CGNode> cg, CGNode nativeNode) {
		IMethod nativeMethod = nativeNode.getMethod();
		if(!nativeMethod.isNative()) {
			throw new RuntimeException("The method: " + nativeMethod + " is not native.");
		}
		String fullNativeMethod = WALAUtils.getFullMethodName(nativeMethod);
		if(!methodMappings.containsKey(fullNativeMethod)) {
			return Collections.emptySet();
		}
		
		//find a list of succ nodes
		Collection<String> succNodeFullNames = methodMappings.get(fullNativeMethod);
		
		Collection<CGNode> succNodes = new LinkedHashSet<CGNode>();
		for(String succNodeFullName : succNodeFullNames) {
			Collection<CGNode> matchedNodes = WALAUtils.lookupCGNode(cg, succNodeFullName);
			succNodes.addAll(matchedNodes);
		}
		
		return succNodes;
	}

	/**
	 * Creates a default empty native method connector for graph traverse
	 * */
	public static NativeMethodConnector createEmptyConnector() {
		return new NativeMethodConnector();
	}
}
