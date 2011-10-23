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

public class TestRSESDKUI extends AbstractUITest {
	public static String PLUGIN_DIR = TestCommons.rse_303_dir + Globals.fileSep
			+ "plugins";
	
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
		return TestCommons.isConcreteAccessibleClass(kclass)
				&& kclass.toString().indexOf("/ui") != -1
				&& kclass.toString().indexOf("/rse/") != -1;
	}

	public void testGetAppJars() {
		super.checkAppJarNumber(46);
	}

	public void testDetectUIErrors() throws IOException,
			ClassHierarchyException {
		List<AnomalyCallChain> chains = super.reportUIErrors(SWTAppUIErrorMain.default_log, CG.ZeroOneContainerCFA);
		assertEquals(308, chains.size());
	}
	
	//I can not reproduce this!
	public void testKnownBug267478() throws ClassHierarchyException, IOException {
		//org.eclipse.rse.services.dstore.util.DownloadListener
		AbstractUITest test = new AbstractUITest(){
			@Override
			protected boolean isUIClass(IClass kclass) {
				return kclass.toString().indexOf("org/eclipse/dstore/core/model/UpdateHandler") != -1;
			}
			@Override
			protected String getAppPath() {
				return PLUGIN_DIR;
			}
			@Override
			protected String getDependentJars() {
				return EclipsePluginCommons.DEPENDENT_JARS;
			}
			
		};
		AbstractUITest.DEBUG = true;
		List<AnomalyCallChain> chains  = test.reportUIErrors(SWTAppUIErrorMain.default_log, CG.ZeroOneCFA);
		assertEquals(0, chains.size());
	}
}
