package edu.washington.cs.detector;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.ipa.callgraph.CGNode;

import edu.washington.cs.detector.util.Utils;

public class CGTraverseNoSystemCalls implements CGTraverseGuider {
	
	public static String[] system_classes = new String[] {"Ljava/lang/String",
		"Ljava/io/PrintStream", "Ljava/lang/System", "Ljava/lang/Class"
		, "Ljava/lang/StringBuilder", "Ljava/lang/SecurityManager", "Ljava/lang/Runtime",
		"Ljava/util/ArrayList", "Ljava/util/AbstractMap",
		"Ljava/util/HashMap", "Ljava/lang/Throwable", "Ljava/util/Hashtable",
		"Ljava/lang/ClassLoader"};

	@Override
	public boolean traverse(CGNode src, CGNode dest) {
		//check if the destination is a system call or not
		IClass klass = dest.getMethod().getDeclaringClass();
		String className = klass.getName().toString();
		//System.err.println(className);
		if(Utils.<String>includedIn(className, system_classes)) {
			return false;
		}
		if(this.matchJDKPackages(className)) {
			return false;
		}
		if(this.matchUnlikelyCalls(dest)) {
			return false;
		}
		return true;
	}
	
	private boolean matchJDKPackages(String className) {
		return className.indexOf("Ljava/util") != -1
		    || className.indexOf("Ljava/security") != -1
		    || className.indexOf("Ljava/io") != -1
		    || className.indexOf("Ljavax/") != -1
		    || className.indexOf("Lsun/") != -1;
	}
	
	private boolean matchUnlikelyCalls(CGNode node) {
		String cgNodeStr = node.toString();
		return cgNodeStr.indexOf("Lorg/eclipse/swt/widgets/Display, getDefault()") != -1;
	}
}
