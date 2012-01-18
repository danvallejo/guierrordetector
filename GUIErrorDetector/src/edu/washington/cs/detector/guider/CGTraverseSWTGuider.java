package edu.washington.cs.detector.guider;

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
		"Lorg/eclipse/swt/widgets/Widget, toString()",
		"Lorg/eclipse/swt/widgets/Widget, dispose()V",
		"Lorg/eclipse/swt/widgets/Synchronizer, syncExec(Ljava/lang/Runnable;)V",
		//safe methods
		"Lorg/eclipse/swt/widgets/Display, syncExec(Ljava/lang/Runnable;)V",
		"Lorg/eclipse/swt/widgets/Display, asyncExec(Ljava/lang/Runnable;)V",
		"Lorg/eclipse/swt/widgets/Display, runAsyncMessages(Z)Z",
		"Lorg/eclipse/core/internal/runtime/Log, log(Lorg/eclipse/core/runtime/IStatus;)V" //add Log, it is highly unlikely
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