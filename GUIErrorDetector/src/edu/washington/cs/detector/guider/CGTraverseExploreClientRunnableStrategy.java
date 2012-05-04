package edu.washington.cs.detector.guider;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.CGNode;

import edu.washington.cs.detector.util.Utils;
import edu.washington.cs.detector.util.WALAUtils;

public class CGTraverseExploreClientRunnableStrategy implements CGTraverseGuider {
	
	public final String[] clientRunnablePackage;
	
	public final Map<String, String> methodCallMapping = new LinkedHashMap<String, String>();
	
	public final Set<String> exclusionMethods = new LinkedHashSet<String>();
	
	CGTraverseGuider safeGuier = new CGTraverseAndroidSafeMethodGuider();
	
	public CGTraverseExploreClientRunnableStrategy(String[] clientCodePackage) {
		this.clientRunnablePackage = clientCodePackage;
	}
	
	//the format:  a.b.c.Class.methodName
	public void addMethodGuidance(String srcSig, String destSig) {
		methodCallMapping.put(srcSig, destSig);
	}
	
	public void addExclusionGuidance(String exclusionMethod) {
		exclusionMethods.add(exclusionMethod);
	}

	@Override
	public boolean traverse(CGNode src, CGNode dest) {
//		if(src.toString().indexOf("dispatchMessage") != -1) {
//		    System.out.println("Checking traverse: src: " + src
//		    		+ "\n    dest: " + dest);
//		    IMethod srcMethod = src.getMethod();
//			IClass srcClass = srcMethod.getDeclaringClass();
//			String classFullName = WALAUtils.getJavaFullClassName(srcClass);
//			//System.err.println(classFullName);
//			String methodName = srcMethod.getName().toString();
//		    String srcmethod = classFullName + "." + methodName;
//		    System.out.println("  src method: " + srcmethod);
//		}
		
		if(!safeGuier.traverse(src, dest)) {
			return false;
		}
		
		if(Utils.containIn(dest.getMethod().getSignature(), exclusionMethods.toArray(new String[0]))) {
			return false;
		}
		
		if(this.clientRunnablePackage.length == 0 && methodCallMapping.isEmpty()) {
			return true;
		}
		
		IMethod srcMethod = src.getMethod();
		IClass srcClass = srcMethod.getDeclaringClass();
		String classFullName = WALAUtils.getJavaFullClassName(srcClass);
		//System.err.println(classFullName);
		String methodName = srcMethod.getName().toString();
		if (this.clientRunnablePackage.length > 0) {
			if (classFullName.equals("java.lang.Thread") && (methodName.equals("start") /*|| methodName.equals("run")*/)) {
				IClass destClass = dest.getMethod().getDeclaringClass();
				String destCode = WALAUtils.getJavaFullClassName(destClass) + "." + dest.getMethod().getName().toString() ;
//				System.out.println("source node: " + src);
//				System.out.println("dest node: " + dest);
				if (this.isInClientPackage(destCode.trim())) {
					// System.err.println("thread start: " + dest);
//					System.out.println(" XX  starting traverse! " + src + "  --> " + dest);
					return true;
				} else {
					return false; //be aware of this
				}
			}
		}
		
		if(!methodCallMapping.isEmpty()) {
			String srcmethod = classFullName + "." + methodName;
			if(methodCallMapping.containsKey(srcmethod)) {
				IClass destClass = dest.getMethod().getDeclaringClass();
				String destCode = WALAUtils.getJavaFullClassName(destClass) + "." + dest.getMethod().getName().toString();
//				System.out.println("Checking method call mapping: ");
//				System.out.println("    srcmethod: " + srcmethod);
//				System.out.println("    destcode: " + destCode);
//				System.out.println("    methodCallMapping.get(srcmethod): " + methodCallMapping.get(srcmethod));
				if(!destCode.startsWith(methodCallMapping.get(srcmethod))) {
//					System.out.println("  checking fails...");
					return false;
				} else {
//				    System.out.println("visiting: " + srcmethod + "  --> " + destCode);
				    return true;
				}
			}
		}
		
		//System.out.println("visiting: " + src + "  --> " + dest);
		return true;
	}
	
	private boolean isInClientPackage(String destCode) {
//		System.out.println("   ===== " +destCode);
		for(String pName : this.clientRunnablePackage) {
//			System.out.println("          +++++ " + pName);
			if(destCode.startsWith(pName.trim())) {
				return true;
			}
		}
		return false;
	}

}
