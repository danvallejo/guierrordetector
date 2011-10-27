package edu.washington.cs.detector;

import java.io.IOException;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.util.graph.Graph;

import edu.washington.cs.detector.CGBuilder.CG;
import edu.washington.cs.detector.util.EclipsePluginCommons;
import edu.washington.cs.detector.util.Globals;
import edu.washington.cs.detector.util.PDFViewer;
import edu.washington.cs.detector.util.Utils;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class TestUseCustomizedEntries extends TestCase {
	
	public static Test suite() {
		return new TestSuite(TestUseCustomizedEntries.class);
	}
	
	//"test.callgraphentries.NoAbstract"
	public void testNoAbstractCallGraphEntries() throws ClassHierarchyException, IOException {
		this.checkCallGraphEntries(TestCommons.testfolder + "callgraphentries", "test.callgraphentries.NoAbstract");
	}
	
	//"test.callgraphentries.NoAbstract"
	public void testAbstractCallGraphEntries() throws ClassHierarchyException, IOException {
		try {
		   this.checkCallGraphEntries(TestCommons.testfolder + "callgraphentries", "test.callgraphentries.CGEntries");
		   assertTrue("Should not reach here.", false);
		} catch (AssertionError error) {
			//pass
		}
	}
	
	public void testInterfaceClass() throws ClassHierarchyException, IOException {
		this.checkCallGraphEntries(TestCommons.testfolder + "useinterface",
				"test.useinterface.InterfaceClass");
	}
	
	public void checkCallGraphEntries(String appPath, String className) throws IOException, ClassHierarchyException {
		CGBuilder builder = new CGBuilder(appPath);
		builder.makeScopeAndClassHierarchy();
		Iterable<Entrypoint> entries = CGEntryManager.getAppPublicMethodsByClass(builder.getAnalysisScope(),
				builder.getClassHierarchy(), className);
		builder.setCGType(CG.RTA);
		builder.buildCG(entries);
		PDFViewer.viewCG("cgentries_" + className + ".pdf", builder.getAppCallGraph());
		System.out.println("entries num: " + Utils.countIterable(entries));
		System.out.println("entries: " + Utils.dumpCollection(entries));
		//let's see the entries
		System.out.println("Number of entries in the built CG: " + builder.getCallGraph().getEntrypointNodes().size());
		assertEquals("The no of entries not equal, in built CG and the given entries.",
				Utils.countIterable(entries), builder.getCallGraph().getEntrypointNodes().size());
	}
	
	public void testFindCustomizedEntries() throws IOException {
		String appPath = TestCommons.testfolder + "helloworld";// + ";" +  UIErrorMain.swtJar;
		CGBuilder builder = new CGBuilder(appPath);
		builder.buildCG();
		Iterable<Entrypoint> entries = CGEntryManager.getAppMethodsBySiganture(builder.getAnalysisScope(),
				builder.getClassHierarchy(), "test.helloworld.HelloWorld", "sayHello2", "()V");
		
		int size = 0;
		for(Entrypoint entry : entries) {
			assertTrue(entry != null);
			size++;
		}
		
		assertEquals("Number of entries.", 1, size);
	}
	
	public void testFindAllPublicMethodsForPlugin() throws ClassHierarchyException, IOException {
		String appPath = TestCommons.plugintest_bin_dir + Globals.pathSep  +  EclipsePluginCommons.DEPENDENT_JARS;
		CGBuilder builder = new CGBuilder(appPath);
		builder.makeScopeAndClassHierarchy();
		ClassHierarchy cha = builder.getClassHierarchy();
		
		for(IClass kclass : cha) {
			if(kclass.toString().indexOf("plugintest") != -1) {
			    System.out.println("class: " + kclass);
			}
		}
		
		String methodClass = "plugintest.views.SampleView";
		Iterable<Entrypoint> entries = CGEntryManager.getAppPublicMethodsByClass(builder.getAnalysisScope(), cha, methodClass);
		
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
		assertEquals(detector.detectUIAnomaly(builder).size(), 4);
	}
	
	public void testFindCustomizedEntriesForPlugin() throws IOException, ClassHierarchyException {
		String appPath = TestCommons.plugintest_bin_dir + Globals.pathSep +  EclipsePluginCommons.DEPENDENT_JARS;
		CGBuilder builder = new CGBuilder(appPath);
		builder.makeScopeAndClassHierarchy();
		ClassHierarchy cha = builder.getClassHierarchy();
		
		for(IClass kclass : cha) {
			if(kclass.toString().indexOf("plugintest") != -1) {
			    System.out.println("class: " + kclass);
			}
		}
		
		String methodClass = "plugintest.Activator";
		Iterable<Entrypoint> entries = CGEntryManager.getAppMethodsBySiganture(builder.getAnalysisScope(),
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
		Graph<CGNode> appCG = builder.getAppCallGraph();
		int count = 0;
		for(CGNode node : appCG) {
			count++;
			if(node.toString().indexOf("plugintest") != -1)
			    System.out.println("App cg node: " + node);
		}
		System.out.println("count: " + count);
	}
	
	public void testGetPublicMethodsFromAllSubclasses() throws IOException, ClassHierarchyException {
		String appPath = TestCommons.plugintest_bin_dir + Globals.pathSep +  EclipsePluginCommons.DEPENDENT_JARS;
		CGBuilder builder = new CGBuilder(appPath);
		builder.makeScopeAndClassHierarchy();
		
		Iterable<Entrypoint> entries = CGEntryManager.getAppPublicMethodsInSubclasses(builder.getAnalysisScope(),
				builder.getClassHierarchy(), "org.eclipse.ui.part.WorkbenchPart", "plugintest");
		
		int count = 0;
		for(Entrypoint ep : entries) {
			System.out.println("entry: " + ep);
			count++;
		}
		System.out.println("count = : " + count);
		assertEquals(3, count);
	}
	
}