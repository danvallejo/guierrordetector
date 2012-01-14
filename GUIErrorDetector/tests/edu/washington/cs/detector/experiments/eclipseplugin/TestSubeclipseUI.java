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
import edu.washington.cs.detector.CallChainFilter;
import edu.washington.cs.detector.SWTAppUIErrorMain;
import edu.washington.cs.detector.TestCommons;
import edu.washington.cs.detector.CGBuilder.CG;
import edu.washington.cs.detector.experiments.filters.RemoveSystemCallStrategy;
import edu.washington.cs.detector.util.EclipsePluginCommons;
import edu.washington.cs.detector.util.Globals;
import edu.washington.cs.detector.util.Utils;

public class TestSubeclipseUI extends AbstractEclipsePluginTest {

	public static String PLUGIN_DIR = TestCommons.subeclipse_1_6 + Globals.fileSep 	+ "plugins";
	
//	@Override
//	protected boolean isUIClass(IClass kclass) {
//		return //TestCommons.isConcreteAccessibleClass(kclass) &&
//		    kclass.toString().indexOf("/ui/operations/CheckoutAsProjectOperation") != -1;
////		    && kclass.toString().indexOf("/subclipse/") != -1;
//	}

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

	public void testDetectUIErrors() throws IOException,
	    ClassHierarchyException {
//        List<AnomalyCallChain> chains = super.reportUIErrors(SWTAppUIErrorMain.default_log, CG.ZeroOneContainerCFA);
//        System.out.println("No of chains before filtering system classes: " + chains.size());
//        
//        CallChainFilter filter = new CallChainFilter(chains);
//		chains = filter.apply(new RemoveSystemCallStrategy());
//		System.out.println("No of chains after filtering system classes: " + chains.size());
//		
//		Utils.dumpAnomalyCallChains(chains, "./logs/subclipse-1.6.txt");
    }
}
