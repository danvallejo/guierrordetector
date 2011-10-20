package edu.washington.cs.detector;

import java.io.IOException;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.ipa.cha.ClassHierarchyException;

public class TestPDEUI extends AbstractUITest {

	public static String PLUGIN_DIR = TestCommons.pde_eclipseplugin_dir;
	
	@Override
	protected String getAppPath() {
		return PLUGIN_DIR;
	}

	@Override
	protected String getDependentJars() {
		return null;
	}
	
	@Override
	protected boolean isUIClass(IClass kclass) {
		return TestCommons.isConcreteAccessibleClass(kclass) && 
		    kclass.toString().indexOf("/ui") != -1 && kclass.toString().indexOf("/pde/") != -1;
	}

	public void testGetAppJars() {
		super.reportAppJars();
	}
	
	public void testPDEUIErrors() throws IOException, ClassHierarchyException {
		super.reportUIErrors(SWTAppUIErrorMain.default_log);
	}
}