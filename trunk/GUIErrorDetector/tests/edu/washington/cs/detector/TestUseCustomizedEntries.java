package edu.washington.cs.detector;

import java.io.IOException;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.util.graph.Graph;

import edu.washington.cs.detector.util.EclipsePluginCommons;

import junit.framework.TestCase;

public class TestUseCustomizedEntries extends TestCase {
	
	public void testFindCustomizedEntries() throws IOException {
		String appPath = TestCommons.testfolder + "helloworld";// + ";" +  UIErrorMain.swtJar;
		CGBuilder builder = new CGBuilder(appPath);
		builder.buildCG();
		Iterable<Entrypoint> entries = CGEntryManager.getCustomizedEntryPointsInApp(builder.getAnalysisScope(),
				builder.getClassHierarchy(), "test.helloworld", "sayHello2", "()V");
		
		int size = 0;
		for(Entrypoint entry : entries) {
			assertTrue(entry != null);
			size++;
		}
		
		assertEquals("Number of entries.", 1, size);
	}
	
	public void testFindAllPublicMethodsForPlugin() throws ClassHierarchyException, IOException {
		String appPath = "D:\\research\\guierror\\eclipsews\\plugintest\\bin" + ";" +  EclipsePluginCommons.DEPENDENT_JARS;
		CGBuilder builder = new CGBuilder(appPath);
		builder.makeScopeAndClassHierarchy();
		ClassHierarchy cha = builder.getClassHierarchy();
		
		for(IClass kclass : cha) {
			if(kclass.toString().indexOf("plugintest") != -1) {
			    System.out.println("class: " + kclass);
			}
		}
		
		String methodClass = "plugintest.views.SampleView";
		Iterable<Entrypoint> entries = CGEntryManager.getPublicMethodAsEntryPointsInApp(builder.getAnalysisScope(), cha, methodClass);
		
		int size = 0;
		for(Entrypoint entry : entries) {
			assertTrue(entry != null);
			System.out.println("entry: " + entry);
			size++;
		}
		
		System.out.println("size is: " + size);
		
        builder.buildCG(entries);
		
		//PDFViewer.viewCG("plugincg.pdf", builder.getAppCallGraph());
		Graph<CGNode> appCG = builder.getAppCallGraph();
		int count = 0;
		for(CGNode node : appCG) {
			count++;
			if(node.toString().indexOf("plugintest") != -1)
			    System.out.println("App cg node: " + node);
		}
		System.out.println("count: " + count);
		
		//try to detect errors from all public methods
		UIAnomalyDetector detector = new UIAnomalyDetector(appPath);
//		Log.logConfig(UIErrorMain.default_log);
		assertEquals(detector.detectUIAnomaly(builder).size(), 4);
	}
	
	public void testFindCustomizedEntriesForPlugin() throws IOException, ClassHierarchyException {
		String appPath = "D:\\research\\guierror\\eclipsews\\plugintest\\bin" + ";" +  EclipsePluginCommons.DEPENDENT_JARS;
		CGBuilder builder = new CGBuilder(appPath);
		builder.makeScopeAndClassHierarchy();
		ClassHierarchy cha = builder.getClassHierarchy();
		
		for(IClass kclass : cha) {
			if(kclass.toString().indexOf("plugintest") != -1) {
			    System.out.println("class: " + kclass);
			}
		}
		
		String methodClass = "plugintest.Activator";
		Iterable<Entrypoint> entries = CGEntryManager.getCustomizedEntryPointsInApp(builder.getAnalysisScope(),
				//XXX a bug in wala, the class name does not take effects
				builder.getClassHierarchy(), methodClass, "start", "(Lorg/osgi/framework/BundleContext;)V");
		
		int size = 0;
		for(Entrypoint entry : entries) {
			assertTrue(entry != null);
			System.out.println("entry: " + entry);
			size++;
		}
		
		assertEquals("Number of entries.", 1, size);
		
		builder.buildCG(entries);
		
		//PDFViewer.viewCG("plugincg.pdf", builder.getAppCallGraph());
		Graph<CGNode> appCG = builder.getAppCallGraph();
		int count = 0;
		for(CGNode node : appCG) {
			count++;
			if(node.toString().indexOf("plugintest") != -1)
			    System.out.println("App cg node: " + node);
		}
		System.out.println("count: " + count);
	}
	
}
