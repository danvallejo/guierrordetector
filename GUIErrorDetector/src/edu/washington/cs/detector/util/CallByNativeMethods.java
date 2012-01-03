package edu.washington.cs.detector.util;

/**
 * This annotation is used to decorate a method that may be called
 * by a native methods. For example:
 * 
 * @CallByNativeMethods(callers={"native1", "native2"})
 * public void someMethodCalledByNatives() { ... }
 * 
 * */

@interface CallByNativeMethods {
	String[] callers();
}
