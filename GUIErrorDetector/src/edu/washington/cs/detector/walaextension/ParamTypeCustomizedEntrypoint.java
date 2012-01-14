package edu.washington.cs.detector.walaextension;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.callgraph.impl.DefaultEntrypoint;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.util.collections.HashSetFactory;

import edu.washington.cs.detector.util.Files;
import edu.washington.cs.detector.util.WALAUtils;

/*
 * This class "selectively" creates a set of concrete classes of a entry
 * method's parameters, based on user's input.
 * */
public class ParamTypeCustomizedEntrypoint extends DefaultEntrypoint {

	// a set of user provided classes, whose instances should be created
	// in terms of full class name
	public static final Set<String> usr_classes = new HashSet<String>();
	public static void setUserClasses(Collection<String> classFullNames) {
		System.err.println("User class num: " + classFullNames.size() + " for param type customized entrypoint.");
		usr_classes.addAll(classFullNames);
	}
	public static void setUserClasses(String fileName) {
		setUserClasses(Files.readWholeNoExp(fileName));
	}
	public static void clearUserClasses() {
		usr_classes.clear();
	}
	public static ParamTypeCustomizedEntrypoint convertEntrypoint(
			Entrypoint defaultEntrypoint) {
		if(!(defaultEntrypoint instanceof DefaultEntrypoint)) {
			throw new RuntimeException("It must be a DefaultEntrypoint type.");
		}
		return new ParamTypeCustomizedEntrypoint(defaultEntrypoint.getMethod(),
				((DefaultEntrypoint)defaultEntrypoint).getCha());
	}
	//TODO ugly hack, the entrypoint here mean DefaultEntrypoint
	//The return type is actually ParamTypeCustomizedEntrypoint
	public static Iterable<Entrypoint> convertEntrypoints(
			Iterable<Entrypoint> eps) {
		HashSet<Entrypoint> converted = HashSetFactory.make();
		for (Entrypoint ep : eps) {
			converted.add(convertEntrypoint(ep));
		}
		return converted;
	}
	
	/**
	 * All instance method
	 * */

	public ParamTypeCustomizedEntrypoint(IMethod method, IClassHierarchy cha) {
		super(method, cha);
	}

	// overriding the following two methods
	@Override
	protected TypeReference[][] makeParameterTypes(IMethod method) {
		TypeReference[][] result = new TypeReference[method.getNumberOfParameters()][];
		for (int i = 0; i < result.length; i++) {
			result[i] = makeParameterTypes(method, i);
		}

		return result;
	}

	@Override
	protected TypeReference[] makeParameterTypes(IMethod method, int i) {
		//if there is no user specified entry point, just use the default ones
		if(usr_classes.isEmpty()) {
			return super.makeParameterTypes(method, i);
		}
		
		//debugging
		//System.err.println("processing: " + method);
		
		//add corresponding usr defined classes
		TypeReference nominal = method.getParameterType(i);
		if (nominal.isPrimitiveType() || nominal.isArrayType())
			return new TypeReference[] { nominal };
		else {
			IClass nc = getCha().lookupClass(nominal);
			if(nc == null) { //like a class which WALA pretends to not see, like java.net.URL
				//System.err.println("   " + nominal);
				return new TypeReference[] { nominal };
			}
			
			Collection<IClass> subcs = nc.isInterface()
			    ? getCha().getImplementors(nominal)
				: getCha().computeSubClasses(nominal);
			    
			//get all subclasses, including itself!
			Set<TypeReference> subs = HashSetFactory.make();
			subs.add(nominal);
			
			for (IClass cs : subcs) {
				if (!cs.isAbstract() && !cs.isInterface()) {
					String csFullName = WALAUtils.getJavaFullClassName(cs);
					//add it only if the class is included in the list provide by users
					if(usr_classes.contains(csFullName)) {
					    subs.add(cs.getReference());
					}
				}
			}
			return subs.toArray(new TypeReference[subs.size()]);
		}
	}

}
