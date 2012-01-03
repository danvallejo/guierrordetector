package test.nativeannotation;

import edu.washington.cs.detector.util.CallByNativeMethods;

public class NormalClass {
	
	/**
	 * should fetch calling relations for the following 2
	 * */
	
	@CallByNativeMethods(callers={"test.nativeannotation.NativeClass.nativeMethod1"})
	public void javaMethod1() {
		
	}
	
	@CallByNativeMethods(callers={"test.nativeannotation.NativeClass.nativeMethod1",
			"test.nativeannotation.NativeClass.nativeMethod2"})
	public void javaMethod2() {
		
	}

	/**
	 * should report errors below
	 * */
	
	@CallByNativeMethods(callers={"test.nativeannotation.NativeClass.nonNativeMethod"})
	public void incorrectAnnotationMethod() {
		
	}
	
	@CallByNativeMethods(callers={"test.nativeannotation.NativeClass.notexistmethod"})
	public void notExistAnnotationMethod() {
		
	}
}