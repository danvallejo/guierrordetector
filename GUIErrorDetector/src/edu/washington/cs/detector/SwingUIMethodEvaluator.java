package edu.washington.cs.detector;

import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.cha.ClassHierarchy;

import edu.washington.cs.detector.util.SwingUtils;

public class SwingUIMethodEvaluator implements MethodEvaluator {

	@Override
	public boolean isThreadUnsafeMethod(ClassHierarchy cha, IMethod m) {
		return SwingUtils.isThreadUnsafeMethod(m, cha);
	}

}
