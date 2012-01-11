package edu.washington.cs.detector;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.cha.ClassHierarchyException;

import edu.washington.cs.detector.CGBuilder.CG;
import edu.washington.cs.detector.util.PDFViewer;
import edu.washington.cs.detector.util.Utils;
import edu.washington.cs.detector.walaextension.ParamTypeCustomizedEntrypoint;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class TestBuildCGForOpenProgram extends TestCase {

	public static Test suite() {
		return new TestSuite(TestBuildCGForOpenProgram.class);
	}
	
	public void testBuildCGWithoutObjCreation() throws IOException, ClassHierarchyException {
		String appPath = TestCommons.testfolder + "openprogram";
		CGBuilder builder = new CGBuilder(appPath);
		
		builder.makeScopeAndClassHierarchy();
		Iterable<Entrypoint> entries = CGEntryManager.getAllPublicMethods(builder, "test.openprogram.OpenProgram");
		
		System.out.println("Number of entries to build CG: " + Utils.countIterable(entries));
		System.out.println("All iterables: " + Utils.iterableToCollection(entries));
		
		builder.setCGType(CG.ZeroCFA);
		builder.buildCG(entries);
		
		PDFViewer.viewCG("openprogram-orig.pdf", builder.getAppCallGraph());
	}
	
	public void testBuildCGWithPreobjCreation() throws IOException, ClassHierarchyException {
		String appPath = TestCommons.testfolder + "openprogram";
		CGBuilder builder = new CGBuilder(appPath);
		
		builder.makeScopeAndClassHierarchy();
		Iterable<Entrypoint> entries = CGEntryManager.getAllPublicMethods(builder, "test.openprogram.OpenProgram");
		
		//First set up user required class-to-be-instantiated
		Collection<String> classesToInstantiate = new LinkedList<String>();
		classesToInstantiate.add("test.openprogram.MooSub");
		classesToInstantiate.add("test.openprogram.FooSub");
		classesToInstantiate.add("test.openprogram.BarSub");
		classesToInstantiate.add("test.openprogram.NooSub");
		ParamTypeCustomizedEntrypoint.setUserClasses(classesToInstantiate);
		
		//convert it to param type customized entry point
		entries = ParamTypeCustomizedEntrypoint.convertEntrypoints(entries);
		
		System.out.println("Number of entries to build CG: " + Utils.countIterable(entries));
		System.out.println("All iterables: " + Utils.iterableToCollection(entries));
		
		builder.setCGType(CG.ZeroCFA);
		builder.buildCG(entries);
		
		PDFViewer.viewCG("openprogram-orig-clazz.pdf", builder.getAppCallGraph());
	}
	
}
