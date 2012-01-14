package edu.washington.cs.detector.experiments.eclipseplugin;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;

import edu.washington.cs.detector.AnomalyCallChain;
import edu.washington.cs.detector.CGBuilder;
import edu.washington.cs.detector.CallChainFilter;
import edu.washington.cs.detector.SWTAppUIErrorMain;
import edu.washington.cs.detector.TestCommons;
import edu.washington.cs.detector.experiments.filters.RemoveContainingNodeStrategy;
import edu.washington.cs.detector.util.EclipsePluginCommons;
import edu.washington.cs.detector.util.Globals;

public class TestCDTUI extends AbstractEclipsePluginTest {
	
	public static String PLUGIN_DIR = TestCommons.cdt_60_dir + Globals.fileSep + "plugins";

	@Override
	protected String getAppPath() {
		return PLUGIN_DIR;
	}

	@Override
	protected String getDependentJars() {
		return EclipsePluginCommons.DEPENDENT_JARS;
	}
	
	@Override
	protected boolean isUIClass(IClass kclass) {
		return TestCommons.isConcreteAccessibleClass(kclass) && 
		    kclass.toString().indexOf("/ui") != -1 && kclass.toString().indexOf("cdt/dsf") != -1;
	}
	

	public void testGetAppJars() {
		super.checkAppJarNumber(57);
	}
	
	public void testDetectAndFilterUIErrors() throws IOException, ClassHierarchyException {
		List<AnomalyCallChain> chains = super.reportUIErrors(SWTAppUIErrorMain.default_log);
		assertEquals(198, chains.size());
		
		CallChainFilter filter = new CallChainFilter(chains);
		chains = filter.apply(new RemoveContainingNodeStrategy("Lorg/eclipse/jface/operation/ModalContext$ModalContextThread, run()V"));
		System.out.println("No of chains after filtering exception-capture FP: " + chains.size());
	}
	
	public void testFindClassInJar() throws IOException, ClassHierarchyException {
		String appPath =  PLUGIN_DIR + Globals.fileSep + "org.eclipse.cdt.dsf.gdb_2.0.0.200906161748.jar";
	    CGBuilder builder = new CGBuilder(appPath);
	    builder.makeScopeAndClassHierarchy();
	    
	    ClassHierarchy cha = builder.getClassHierarchy();
	    for(IClass kclass : cha) {
	    	if(kclass.toString().indexOf("launching") != -1)
	    	    System.out.println(kclass);
		}
	    
	    File f = new File(appPath);
	    JarFile file = new JarFile(f);
	    int count = 0;
	    for (Enumeration<JarEntry> e = file.entries(); e.hasMoreElements();) {
	        ZipEntry Z = (ZipEntry) e.nextElement();
	        if(Z.toString().indexOf("launching") != -1) {
	            System.out.println("zip entry: " + Z);
	        }
	        count++;
	    }
	    assertEquals(669, count);
	}
	
}
