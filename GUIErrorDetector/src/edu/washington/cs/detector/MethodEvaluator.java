package edu.washington.cs.detector;

import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.cha.ClassHierarchy;

public interface MethodEvaluator {
	public boolean isThreadUnsafeMethod(ClassHierarchy cha, IMethod m);
}
