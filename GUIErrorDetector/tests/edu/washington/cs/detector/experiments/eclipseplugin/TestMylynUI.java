package edu.washington.cs.detector.experiments.eclipseplugin;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;

import edu.washington.cs.detector.AnomalyCallChain;
import edu.washington.cs.detector.SWTAppUIErrorMain;
import edu.washington.cs.detector.TestCommons;
import edu.washington.cs.detector.util.EclipsePluginCommons;
import edu.washington.cs.detector.util.Globals;

public class TestMylynUI extends AbstractEclipsePluginTest {

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
	protected Iterable<Entrypoint> getEntrypoints(ClassHierarchy cha) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Collection<CGNode> getStartNodes(Iterable<CGNode> allNodes,
			ClassHierarchy cha) {
		// TODO Auto-generated method stub
		return null;
	}
	
//	@Override
//	protected boolean isUIClass(IClass kclass) {
//		return TestCommons.isConcreteAccessibleClass(kclass) && 
//		    kclass.toString().indexOf("/ui") != -1 && kclass.toString().indexOf("/mylyn/") != -1;
//	}
//	
//	public void testGetAppJars() {
//		super.checkAppJarNumber(82);
//	}
	
	public void testDetectUIErrors() throws IOException, ClassHierarchyException {
		//List<AnomalyCallChain> chains = super.reportUIErrors(SWTAppUIErrorMain.default_log);
		//assertEquals(100, chains.size());
	}


}