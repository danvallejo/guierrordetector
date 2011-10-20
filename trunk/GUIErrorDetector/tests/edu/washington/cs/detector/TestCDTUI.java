package edu.washington.cs.detector;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;

import edu.washington.cs.detector.util.EclipsePluginCommons;
import edu.washington.cs.detector.util.Globals;

public class TestCDTUI extends AbstractUITest {
	
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
		super.reportAppJars();
	}
	
	public void testDetectUIErrors() throws IOException, ClassHierarchyException {
		super.reportUIErrors(SWTAppUIErrorMain.default_log);
	}
	
	public void testFindClassInJar() throws IOException, ClassHierarchyException {
		String appPath =  "D:\\research\\guierror\\subjects\\cdt-master-6.0.0\\plugins\\org.eclipse.cdt.dsf.gdb_2.0.0.200906161748.jar";
	    CGBuilder builder = new CGBuilder(appPath);
	    builder.makeScopeAndClassHierarchy();
	    
	    ClassHierarchy cha = builder.getClassHierarchy();
	    for(IClass kclass : cha) {
	    	if(kclass.toString().indexOf("launching") != -1)
	    	    System.out.println(kclass);
		}
	    
	    File f = new File(appPath);
	    JarFile file = new JarFile(f);
	    for (Enumeration e = file.entries(); e.hasMoreElements();) {
	        ZipEntry Z = (ZipEntry) e.nextElement();
	        if(Z.toString().indexOf("launching") != -1)
	            System.out.println("zip entry: " + Z);
	    }
	}
	
}
