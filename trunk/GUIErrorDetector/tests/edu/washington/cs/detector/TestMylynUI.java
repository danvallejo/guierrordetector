package edu.washington.cs.detector;

import java.io.IOException;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.ipa.cha.ClassHierarchyException;

import edu.washington.cs.detector.util.EclipsePluginCommons;
import edu.washington.cs.detector.util.Globals;

public class TestMylynUI extends AbstractUITest {

	public static String PLUGIN_DIR = TestCommons.mylyn_362_dir + Globals.fileSep + "plugins";

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
		    kclass.toString().indexOf("/ui") != -1 && kclass.toString().indexOf("/mylyn/") != -1;
	}
	
	public void testGetAppJars() {
		super.reportAppJars();
	}
	
	public void testDetectUIErrors() throws IOException, ClassHierarchyException {
		super.reportUIErrors(SWTAppUIErrorMain.default_log);
	}

}