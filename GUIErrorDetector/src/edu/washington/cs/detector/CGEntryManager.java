package edu.washington.cs.detector;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.callgraph.impl.DefaultEntrypoint;
import com.ibm.wala.ipa.callgraph.impl.SubtypesEntrypoint;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.MethodReference;
import com.ibm.wala.types.TypeName;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.util.collections.HashSetFactory;

import edu.washington.cs.detector.util.Utils;
import edu.washington.cs.detector.util.WALAUtils;

public class CGEntryManager {

	public static Iterable<Entrypoint> mergeEntrypoints(Iterable<Entrypoint> ep1, Iterable<Entrypoint> ep2) {
		final HashSet<Entrypoint> result = HashSetFactory.make();
		for(Entrypoint ep : ep1) {
			result.add(ep);
		}
		for(Entrypoint ep : ep2) {
			result.add(ep);
		}
		return result;
	}
	
	//superClassFullName: a.b.c.ClassName; appClassPackage: a.b.c
	public static Iterable<Entrypoint> getAppPublicMethodsInSubclasses(AnalysisScope scope, ClassHierarchy cha, String superClassFullName,
			String appClassPackage) {
		if (cha == null) {
			throw new IllegalArgumentException("cha is null");
		}
		//get the superclass
		String claszzSignature = "L" + Utils.translateDotToSlash(superClassFullName);
		IClass superClazz = cha.lookupClass(TypeReference.findOrCreate(ClassLoaderReference.Application, claszzSignature));
		if(superClazz == null) {
			throw new IllegalArgumentException("Class: " + superClassFullName + " can not be found in app loader.");
		}
		
		//get all classes extending the superClass
		List<IClass> subclasses = new LinkedList<IClass>();
		for(IClass clz : cha) {
			if(cha.isAssignableFrom(superClazz, clz)) {
				if(appClassPackage != null ) {
				    //only added if clz is inside the appClassPackage
					TypeName tn = clz.getName();
					if(Utils.translateSlashToDot(tn.getPackage().toString()).startsWith(appClassPackage)) {
						subclasses.add(clz);
					}
				} else {
					subclasses.add(clz);
				}
			}
		}
		
		final HashSet<Entrypoint> result = HashSetFactory.make();
		//get all public methods
		for(IClass clz : subclasses) {
			for(IMethod m : clz.getDeclaredMethods()) {
				if(m.isPublic()) {
					result.add(new DefaultEntrypoint(m, cha));
				}
			}
		}
		
		return new Iterable<Entrypoint>() {
			public Iterator<Entrypoint> iterator() {
				return result.iterator();
			}
		};
	}
	
	public static Iterable<Entrypoint> getConstructors(CGBuilder builder, Collection<String> methodClasses) {
		String[] array = new String[methodClasses.size()];
		int i = 0;
		for(String str : methodClasses) {
			array[i++] = str;
		}
		return getConstructors(builder, array);
	}
	
	public static Iterable<Entrypoint> getConstructors(CGBuilder builder, String...methodClasses) {
		AnalysisScope scope = builder.getAnalysisScope();
		ClassHierarchy cha = builder.getClassHierarchy();
		final HashSet<Entrypoint> result = HashSetFactory.make();
		for(String methodClass : methodClasses) {
			Iterable<Entrypoint> eps = getConstructors(scope, cha, methodClass);
			for(Entrypoint ep : eps) {
				result.add(ep);
			}
		}
		return result;
	}
	
	// class name: a.b.c.d class
	public static Iterable<Entrypoint> getConstructors(AnalysisScope scope,
			ClassHierarchy cha, String methodClass) {
		final HashSet<Entrypoint> result = HashSetFactory.make();
		for (IClass klass : cha) {
			// check the class name
            if(!WALAUtils.getJavaFullClassName(klass).equals(methodClass)) {
            	continue;
            }
            //get all constructors
			Collection<IMethod> allMethods = klass.getDeclaredMethods();
			for (IMethod m : allMethods) {
				if (!m.isPublic()) {
					continue;
				}
				if (m.getName().toString().equals("<init>")) {
					result.add(new DefaultEntrypoint(m, cha));
				}
			}
		}
		return new Iterable<Entrypoint>() {
			public Iterator<Entrypoint> iterator() {
				return result.iterator();
			}
		};
	}
	
	//class name: a.b.c.d.Class
	public static Iterable<Entrypoint> getAllPublicMethods(CGBuilder builder, String... uiClasses) {
		AnalysisScope scope = builder.getAnalysisScope();
		ClassHierarchy cha = builder.getClassHierarchy();
		
		return getAllPublicMethods(scope, cha, uiClasses);
	}
	
	public static Iterable<Entrypoint> getAllPublicMethods(CGBuilder builder, List<String> uiClasses) {
		AnalysisScope scope = builder.getAnalysisScope();
		ClassHierarchy cha = builder.getClassHierarchy();
		
		return getAllPublicMethods(scope, cha, uiClasses);
	}
	
	public static Iterable<Entrypoint> getAllPublicMethods(AnalysisScope scope, ClassHierarchy cha, String... methodClasses) {
		List<String> clazzList = new LinkedList<String>();
		for(String uiClass : methodClasses) {
			clazzList.add(uiClass);
		}
		return getAllPublicMethods(scope, cha, clazzList);
	}
	
	public static Iterable<Entrypoint> getAllPublicMethods(AnalysisScope scope, ClassHierarchy cha, List<String> methodClasses) {
		final HashSet<Entrypoint> result = HashSetFactory.make();
		for(String methodClass : methodClasses) {
			//System.out.println("method class: " + methodClass);
		    Iterable<Entrypoint> entries = CGEntryManager.getAppPublicMethodsByClass(scope, cha, methodClass);
		    for(Entrypoint ep : entries) {
		    	result.add(ep);
		    }
		}
		
		return new Iterable<Entrypoint>() {
			public Iterator<Entrypoint> iterator() {
				return result.iterator();
			}
		}; 
	}
	
	//methodClass: a.b.c.d
	public static Iterable<Entrypoint> getAppPublicMethodsByClass(AnalysisScope scope, ClassHierarchy cha, String methodClass) {
		return getAppPublicMethodsByClass(scope, cha, methodClass, false);
	}
	
	//methodClass: a.b.c.d
	//XXX be aware, it only considers app class loader
	public static Iterable<Entrypoint> getAppPublicMethodsByClass(AnalysisScope scope, ClassHierarchy cha, String methodClass, boolean useSubClass) {
		if (cha == null) {
			throw new IllegalArgumentException("cha is null");
		}
		ClassLoaderReference clr = scope.getApplicationLoader();
		final HashSet<Entrypoint> result = HashSetFactory.make();
		for (IClass klass : cha) {
			if (klass.getClassLoader().getReference().equals(clr)) {
				Collection<IMethod> allMethods = klass.getDeclaredMethods();
				for(IMethod m : allMethods) {
					if(!m.isPublic()) {
						continue;
					}
					//XXX a bug in wala
					TypeName tn = m.getDeclaringClass().getName();
					String fullClassName = (tn.getPackage() != null ? Utils.translateSlashToDot(tn.getPackage().toString()) + "." : "")
					    + tn.getClassName().toString();
					//System.out.println("For methods: " + m + "; fullClassName: " + fullClassName + ", methodClass: " + methodClass);
					if(!fullClassName.equals(methodClass)) {
						continue;
					}
					if(useSubClass) {
						result.add(new SubtypesEntrypoint(m, cha));
					} else {
					    result.add(new DefaultEntrypoint(m, cha));
					}
				}
			}
		}
		
		return new Iterable<Entrypoint>() {
			public Iterator<Entrypoint> iterator() {
				return result.iterator();
			}
		}; 
	}
	
	public static Iterable<Entrypoint> getAppMethodsBySiganture(
			AnalysisScope scope, ClassHierarchy cha, String methodClass, String methodName, String methodSignature) {
		if (cha == null) {
			throw new IllegalArgumentException("cha is null");
		}
		ClassLoaderReference clr = scope.getApplicationLoader();
		
		final HashSet<Entrypoint> result = HashSetFactory.make();
		for (IClass klass : cha) {
			if (klass.getClassLoader().getReference().equals(clr)) {
				MethodReference methodRef = MethodReference.findOrCreate(clr, methodClass, methodName, methodSignature);
				IMethod m = klass.getMethod(methodRef.getSelector());
				if (m != null) {
					result.add(new DefaultEntrypoint(m, cha));
				}
			}
		}
		
		//XXX I suspect there is a bug in WALA, the {@link methodClass} argument does not take effect
		final HashSet<Entrypoint> prunedResult = HashSetFactory.make();
		for(Entrypoint p : result) {
			TypeName tn = p.getMethod().getDeclaringClass().getName();
			String fullClassName = (tn.getPackage() != null ? Utils.translateSlashToDot(tn.getPackage().toString()) + "." : "") + tn.getClassName().toString();
			if(fullClassName.equals(methodClass)) {
			  prunedResult.add(p);
			}
		}
		
		return new Iterable<Entrypoint>() {
			public Iterator<Entrypoint> iterator() {
				return prunedResult.iterator();
			}
		};
	}
}