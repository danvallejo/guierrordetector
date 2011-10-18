package edu.washington.cs.detector;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.callgraph.impl.DefaultEntrypoint;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.MethodReference;
import com.ibm.wala.types.TypeName;
import com.ibm.wala.util.collections.HashSetFactory;

import edu.washington.cs.detector.util.Utils;

public class CGEntryManager {

	
	public static Iterable<Entrypoint> getPublicMethodAsEntryPointsInApp(AnalysisScope scope, ClassHierarchy cha, String methodClass) {
		if (cha == null) {
			throw new IllegalArgumentException("cha is null");
		}
		ClassLoaderReference clr = scope.getApplicationLoader();
		final HashSet<Entrypoint> result = HashSetFactory.make();
		for (IClass klass : cha) {
			if (klass.getClassLoader().getReference().equals(clr)) {
				Collection<IMethod> allMethods = klass.getAllMethods();
				for(IMethod m : allMethods) {
					if(!m.isPublic()) {
						continue;
					}
					//XXX a bug in wala
					TypeName tn = m.getDeclaringClass().getName();
					String fullClassName = (tn.getPackage() != null ? Utils.translateSlashToDot(tn.getPackage().toString()) + "." : "")
					    + tn.getClassName().toString();
					//System.out.println("fullClassName: " + fullClassName + ", methodClass: " + methodClass);
					if(!fullClassName.equals(methodClass)) {
						continue;
					}
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
	
	public static Iterable<Entrypoint> getCustomizedEntryPointsInApp(
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