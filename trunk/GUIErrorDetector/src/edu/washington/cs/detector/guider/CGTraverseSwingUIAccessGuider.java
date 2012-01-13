package edu.washington.cs.detector.guider;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.ipa.callgraph.CGNode;

import edu.washington.cs.detector.util.Utils;
import edu.washington.cs.detector.util.WALAUtils;

public class CGTraverseSwingUIAccessGuider implements CGTraverseGuider {
	
	String[] safeMethods = new String[]{"javax.swing.SwingUtilities.invokeLater", "javax.swing.SwingUtilities.invokeAndWait"};
	
	@Override
	public boolean traverse(CGNode src, CGNode dest) {
		//check if the destination is a system call or not
		IClass klass = dest.getMethod().getDeclaringClass();
		String className = klass.getName().toString();
		//System.err.println(className);
		
		//one exception for Timer
		if(Utils.<String>includedIn(className, CGTraverseNoSystemCalls.excep_classes)) {
			return true;
		}
		
		if(Utils.<String>includedIn(className, CGTraverseNoSystemCalls.system_classes)) {
			return false;
		}
		
		String methodName = WALAUtils.getFullMethodName(dest.getMethod());
		for(String safeMethod : safeMethods) {
			if(methodName.equals(safeMethod)){
				return false;
			}
		}
		
		
		return true;
	}

}
