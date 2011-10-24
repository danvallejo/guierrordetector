package edu.washington.cs.detector.experiments;

import java.io.IOException;
import java.util.List;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.ipa.cha.ClassHierarchyException;

import edu.washington.cs.detector.AbstractUITest;
import edu.washington.cs.detector.AnomalyCallChain;
import edu.washington.cs.detector.SWTAppUIErrorMain;
import edu.washington.cs.detector.TestCommons;
import edu.washington.cs.detector.CGBuilder.CG;
import edu.washington.cs.detector.util.EclipsePluginCommons;
import edu.washington.cs.detector.util.Globals;

public class TestSubeclipseUI extends AbstractUITest {

	public static String PLUGIN_DIR = TestCommons.subeclipse_1_6 + Globals.fileSep 	+ "plugins";
	
	@Override
	protected boolean isUIClass(IClass kclass) {
		return TestCommons.isConcreteAccessibleClass(kclass)
		    && kclass.toString().indexOf("/ui") != -1
		    && kclass.toString().indexOf("/subclipse/") != -1;
	}

	@Override
	protected String getAppPath() {
		return PLUGIN_DIR;
	}

	@Override
	protected String getDependentJars() {
		return EclipsePluginCommons.DEPENDENT_JARS;
	}
	
	public void testGetAppJars() {
		super.checkAppJarNumber(13);
	}

	public void testDetectUIErrors() throws IOException,
	    ClassHierarchyException {
        List<AnomalyCallChain> chains = super.reportUIErrors(SWTAppUIErrorMain.default_log, CG.ZeroOneContainerCFA);
        assertEquals(524, chains.size());
    }
}
