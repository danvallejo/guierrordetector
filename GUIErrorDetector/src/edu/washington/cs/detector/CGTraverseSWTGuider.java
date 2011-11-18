package edu.washington.cs.detector;

import com.ibm.wala.ipa.callgraph.CGNode;

public class CGTraverseSWTGuider implements CGTraverseGuider {

	CGTraverseGuider systemGuider = new CGTraverseNoSystemCalls();
	
	//Excluding the following method calls:
	String[] swtCalls = new String[] {
		"Lorg/eclipse/swt/graphics/GC, <init>(Lorg/eclipse/swt/graphics/Drawable;)V",
		"Lorg/eclipse/swt/graphics/GC, <init>(Lorg/eclipse/swt/graphics/Drawable;I)V",
		"Lorg/eclipse/swt/graphics/Device, getDevice()",
		"Lorg/eclipse/swt/graphics/Display, getDefault()",
		"Lorg/eclipse/swt/widgets/Display, getActiveShell()",
		"Lorg/eclipse/swt/widgets/Widget, toString()"
	};
	
	@Override
	public boolean traverse(CGNode src, CGNode dest) {
		if(!systemGuider.traverse(src, dest)) {
			return false;
		}
		//check specific SWT rules 
		if(this.matchExcludedSWTCalls(dest)) {
			return false;
		}
		return true;
	}
	
	private boolean matchExcludedSWTCalls(CGNode dest) {
		String destStr = dest.toString();
		for(String str : swtCalls) {
			if(destStr.indexOf(str) != -1) {
				return true;
			}
		}
		return false;
	}
}