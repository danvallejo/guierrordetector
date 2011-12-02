package edu.washington.cs.detector.guider;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.CGNode;

import edu.washington.cs.detector.util.WALAUtils;

/**
 * This is a very aggressive exploring strategy, buyer aware.
 * It only explore start -> runnable, where runnable is declared by client
 * */
public class CGTraverseOnlyClientRunnableStrategy implements CGTraverseGuider {
	
	CGTraverseGuider safeGuier = new CGTraverseAndroidSafeMethodGuider();
	
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
			String destPackage = WALAUtils.getJavaPackageName(destClass);
			if(destPackage != null && destPackage.startsWith("android.")) {
				//System.err.println("thread start: " + dest);
				return false;
			}
		}
		
		return true;
	}

}
