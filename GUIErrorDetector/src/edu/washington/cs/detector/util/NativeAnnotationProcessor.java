package edu.washington.cs.detector.util;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipException;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.classLoader.ShrikeCTMethod;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.shrikeCT.AnnotationsReader.UnimplementedException;
import com.ibm.wala.shrikeCT.InvalidClassFileException;
import com.ibm.wala.types.TypeName;

public class NativeAnnotationProcessor {
	
	public static Collection<String[]> getAllCallingPairsByNativeAnnotations(String jarFile, String lib) {
		Collection<String[]> pairs = new LinkedList<String[]>();
		try {
		    Map<String, Collection<String>> relations = findCalledByNativesAnntationFromJarFile(jarFile, lib);
		    for(String key : relations.keySet()) {
		    	Collection<String> values = relations.get(key);
		    	for(String value : values) {
		    		String[] pair = new String[]{value, key};
		    		pairs.add(pair);
		    	}
		    }
		    return pairs;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	//java method  ->  a collection native method callers
	public static Map<String, Collection<String>> findCalledByNativesAnntationFromJarFile(String jarFile, String lib) {
		if(!jarFile.endsWith(".jar")) {
			throw new RuntimeException("The file provided: " + jarFile + " is not a jar file.");
		}
		Map<String, Collection<String>> callRelations = new LinkedHashMap<String, Collection<String>>();
		
		try {
			Set<Class<?>> classes = loadAllClasses(jarFile, lib);
			for(Class<?> clz : classes) {
				Method[] methods = clz.getDeclaredMethods();
				for(Method m : methods) {
					String methodSig = m.getDeclaringClass().getName() + "." + m.getName();
					Annotation[] annotations = m.getAnnotations();
					for(Annotation ann : annotations) {
						if(ann.annotationType().equals(CalledByNativeMethods.class)) {
							System.out.println("find annotation: " + ann);
							CalledByNativeMethods nativeAnn = (CalledByNativeMethods)ann;
							String[] callers = nativeAnn.callers();
							if(callers.length != 0) {
								if(!callRelations.containsKey(methodSig)) {
									callRelations.put(methodSig, new LinkedHashSet<String>());
								}
								for(String caller : callers) {
									callRelations.get(methodSig).add(caller);
								}
							}
						}
					}
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		return callRelations;
	}
	
	//FIXME a quick-dirty implementation, only allows 1 lib.
	public static Set<Class<?>> loadAllClasses(String jarFile, String lib) throws ClassNotFoundException, ZipException, IOException {
		File file  = new File(jarFile);
		File libFile = new File(lib);
		URL[] urls = new URL[]{file.toURL(), libFile.toURL()};
		ClassLoader cl = new URLClassLoader(urls);

		Set<Class<?>> loadedClasses = new LinkedHashSet<Class<?>>();
		Collection<String> contents = JarViewer.getContentsAsStr(file);
		//Class<?> cls = cl.loadClass("com.mypackage.myclass");
		
		for(String content : contents) {
			if(content.endsWith(".class")) {
				//System.out.println(content);
				String className = content.substring(0, content.length() - ".class".length());
				//System.out.println(className);
				className = Utils.translateSlashToDot(className);
				//System.out.println(className);
				Class<?> cls = cl.loadClass(className);
				loadedClasses.add(cls);
			}
		}
		
		return loadedClasses;
	}
	
	/**
	 * The below code uses WALA to read the annotation. However, as of Dec 2011, WALA does not
	 * support method annotation yet, although a late commit indicates the annotation support
	 * has been added:
	 * http://sourceforge.net/mailarchive/forum.php?thread_name=OFA74890B8.9E3755EA-ON87257982.
	 * 006305F0-87257982.006315AE%40us.ibm.com&forum_name=wala-wala
	 * 
	 * For the sake of reducing potential risk, the above Java reflection-based approach
	 * is still being used.
	 * */
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
//	    if (m instanceof ShrikeCTMethod) {
//	      Collection<Annotation> annotations = null;
//	      try {
//	        annotations =((ShrikeCTMethod) m).getRuntimeVisibleAnnotations();
//	      } catch (InvalidClassFileException e) {
//	        e.printStackTrace();
//	        Assertions.UNREACHABLE();
//	      } catch (UnimplementedException e) {
//	        e.printStackTrace();
//	        Assertions.UNREACHABLE();
//	      }
//	      for (Annotation a : annotations) {
//	        if (a.getType().getName().equals(type)) {
//	          return a;
//	        }
//	      }
//	    }
	    return null;
	  }
}
