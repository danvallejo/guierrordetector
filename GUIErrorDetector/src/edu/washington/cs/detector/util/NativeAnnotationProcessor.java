package edu.washington.cs.detector.util;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.classLoader.ShrikeCTMethod;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.shrikeCT.AnnotationsReader.UnimplementedException;
import com.ibm.wala.shrikeCT.InvalidClassFileException;
import com.ibm.wala.types.TypeName;
import com.ibm.wala.types.annotations.Annotation;
import com.ibm.wala.util.debug.Assertions;

public class NativeAnnotationProcessor {

	public static Map<String, Collection<String>> getNativeCallRelations(
			ClassHierarchy cha) throws InvalidClassFileException,
			UnimplementedException {

		Map<String, Collection<String>> callRelations = new LinkedHashMap<String, Collection<String>>();
		
		TypeName nativeAnnotation = TypeName.findOrCreateClassName(
				"edu/washington/cs/detector/util", "CallByNativeMethods");

		for (IClass clz : cha) {
			Collection<IMethod> methods = clz.getDeclaredMethods();
			for (IMethod method : methods) {
				if (method instanceof ShrikeCTMethod) {
					Annotation ann = getVisibleAnnotation(method,
							nativeAnnotation);
					if (ann != null) {
						System.out.println(" method: " + method);
						
						throw new RuntimeException("No idea in how to get annotation values!");
						
					}
				} else {
					System.err.println("WARNING: see a non-ShrikeCTMethod: "
							+ method + ", type: " + method.getClass());
				}
			}
		}

		return callRelations;
	}
	
	public static Annotation getVisibleAnnotation(IMethod m, TypeName type) {
	    if (m instanceof ShrikeCTMethod) {
	      Collection<Annotation> annotations = null;
	      try {
	        annotations = ((ShrikeCTMethod) m).getRuntimeVisibleAnnotations();
	      } catch (InvalidClassFileException e) {
	        e.printStackTrace();
	        Assertions.UNREACHABLE();
	      } catch (UnimplementedException e) {
	        e.printStackTrace();
	        Assertions.UNREACHABLE();
	      }
	      for (Annotation a : annotations) {
	        if (a.getType().getName().equals(type)) {
	          return a;
	        }
	      }
	    }
	    return null;
	  }
}
