package edu.washington.cs.detector.guider;

import java.util.LinkedHashMap;
import java.util.Map;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.CGNode;

import edu.washington.cs.detector.util.WALAUtils;

public class CGTraverseExploreClientRunnableStrategy implements CGTraverseGuider {
	
	public final String[] clientRunnablePackage;
	
	public final Map<String, String> methodCallMapping = new LinkedHashMap<String, String>();
	
	CGTraverseGuider safeGuier = new CGTraverseAndroidSafeMethodGuider();
	
	public CGTraverseExploreClientRunnableStrategy(String[] clientCodePackage) {
		this.clientRunnablePackage = clientCodePackage;
	}
	
	//the format:  a.b.c.Class.methodName
	public void addMethodGuidance(String srcSig, String destSig) {
		methodCallMapping.put(srcSig, destSig);
	}

	@Override
	public boolean traverse(CGNode src, CGNode dest) {
		if(!safeGuier.traverse(src, dest)) {
			return false;
		}
		
		IMethod srcMethod = src.getMethod();
		IClass srcClass = srcMethod.getDeclaringClass();
		String classFullName = WALAUtils.getJavaFullClassName(srcClass);
		//System.err.println(classFullName);
		String methodName = srcMethod.getName().toString();
		if(classFullName.equals("java.lang.Thread") && (methodName.equals("start") || methodName.equals("run"))) {
			IClass destClass = dest.getMethod().getDeclaringClass();
			String destCode = WALAUtils.getJavaFullClassName(destClass);
			if(!this.isInClientPackage(destCode)) {
				//System.err.println("thread start: " + dest);
				return false;
			}
		}
		
		if(!methodCallMapping.isEmpty()) {
			String srcmethod = classFullName + "." + methodName;
			if(methodCallMapping.containsKey(srcmethod)) {
				IClass destClass = dest.getMethod().getDeclaringClass();
				String destCode = WALAUtils.getJavaFullClassName(destClass);
				if(!destCode.startsWith(methodCallMapping.get(srcmethod))) {
					return false;
				}
			}
		}
		
		return true;
	}
	
	private boolean isInClientPackage(String destCode) {
		for(String pName : this.clientRunnablePackage) {
			if(destCode.startsWith(pName)) {
				return true;
			}
		}
		return false;
	}

}
