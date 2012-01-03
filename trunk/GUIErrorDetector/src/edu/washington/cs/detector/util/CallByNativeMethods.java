package edu.washington.cs.detector.util;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.ElementType;

/**
 * This annotation is used to decorate a method that may be called
 * by a native methods. For example:
 * 
 * @CallByNativeMethods(callers={"ClassName.native1", "ClassName.native2"})
 * public void someMethodCalledByNatives() { ... }
 * 
 * */

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface CallByNativeMethods {
	String[] callers();
}
