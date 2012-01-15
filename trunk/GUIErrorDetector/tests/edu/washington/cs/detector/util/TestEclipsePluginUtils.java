package edu.washington.cs.detector.util;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;

import edu.washington.cs.detector.CGBuilder;
import junit.framework.TestCase;

public class TestEclipsePluginUtils extends TestCase {

	public void testGetAllSWTListeners() throws IOException, ClassHierarchyException {
		CGBuilder builder = new CGBuilder(EclipsePluginCommons.DEPENDENT_JARS);
		builder.makeScopeAndClassHierarchy();
		ClassHierarchy cha = builder.getClassHierarchy();
		
		String[] packages = new String[]{"org.eclipse"};
		IClass swtListener = EclipsePluginUtils.getSWTEventListener(cha);
		Collection<IClass> colls = EclipsePluginUtils.getAllAppSubClasses(cha, swtListener, packages);
		System.out.println("Number of collections: " + colls.size());
		int i = 0;
		Set<String> abstractMethods = new HashSet<String>();
		for(IClass c : colls) {
			if(c.isPublic() && (c.isAbstract() || c.isInterface())) {
				String fullName = WALAUtils.getJavaFullClassName(c);
				if(fullName.endsWith("Listener")) {
					System.out.println(c);
				}
				
				for(IMethod m : c.getDeclaredMethods()) {
					if(m.isAbstract()) {
					abstractMethods.add(m.getName().toString());
					} else {
						//System.out.println(m);
					}
				}
			    //System.out.println("   " + (i++) + c);
			} else {
				//System.out.println(" >> " + c.getSuperclass() + ",  " + c.getAllImplementedInterfaces());
			}
		}
		
//		i = 0;
//		packages = new String[]{"org.tigris"};
//		colls = EclipsePluginUtils.getAllAppSubClasses(cha, swtListener, packages);
//		System.out.println("Number of collections: " + colls.size());
//		for(IClass c : colls) {
//			    System.out.println("   " + (i++) + c);
		
//		}

//		System.out.println("Number of methods: " + abstractMethods.size());
//		for(String am : abstractMethods) {
//			System.out.println(am);
//		}
	}
}
