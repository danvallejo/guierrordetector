package edu.washington.cs.detector;

import java.io.IOException;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.types.TypeName;

import edu.washington.cs.detector.util.EclipsePluginCommons;

import junit.framework.TestCase;

public class TestFindCustomizedEntries extends TestCase {
	
	public void testFindCustomizedEntries() throws IOException {
		String appPath = TestCommons.testfolder + "helloworld";// + ";" +  UIErrorMain.swtJar;
		CGBuilder builder = new CGBuilder(appPath);
		builder.buildCG();
		Iterable<Entrypoint> entries = builder.getCustomizedEntryPointsInApp(builder.getAnalysisScope(),
				builder.getClassHierarchy(), "test.helloworld", "sayHello2", "()V");
		
		int size = 0;
		for(Entrypoint entry : entries) {
			assertTrue(entry != null);
			size++;
		}
		
		assertEquals("Number of entries.", 1, size);
	}
	
	public void testFindCustomizedEntriesForPlugin() throws IOException {
		String appPath = "D:\\research\\guierror\\eclipsews\\plugintest\\bin" + ";" +  EclipsePluginCommons.DEPENDENT_UI_JARS;
		CGBuilder builder = new CGBuilder(appPath);
		builder.buildCG();
		ClassHierarchy cha = builder.getClassHierarchy();
		
		for(IClass kclass : cha) {
			if(kclass.toString().indexOf("plugintest") != -1) {
			    System.out.println("class: " + kclass);
			}
		}
		
		String methodClass = "plugintest.Activator";
		Iterable<Entrypoint> entries = builder.getCustomizedEntryPointsInApp(builder.getAnalysisScope(),
				//XXX a bug in wala, the class name does not take effects
				builder.getClassHierarchy(), methodClass, "start", "(Lorg/osgi/framework/BundleContext;)V");
		
		int size = 0;
		for(Entrypoint entry : entries) {
			assertTrue(entry != null);
			TypeName tn = entry.getMethod().getDeclaringClass().getName();
			String fullClassName = (tn.getPackage() != null ? tn.getPackage().toString() + "." : "") + tn.getClassName().toString();
			if(fullClassName.equals(methodClass)) {
			  System.out.println("entry: " + entry );
			  size++;
			}
		}
		
		assertEquals("Number of entries.", 1, size);
	}
	
}
