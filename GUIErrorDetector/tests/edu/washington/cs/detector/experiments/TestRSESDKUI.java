package edu.washington.cs.detector.experiments;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.cha.ClassHierarchyException;

import edu.washington.cs.detector.AbstractUITest;
import edu.washington.cs.detector.AnomalyCallChain;
import edu.washington.cs.detector.CallChainFilter;
import edu.washington.cs.detector.FilterStrategy;
import edu.washington.cs.detector.RemoveSystemCallStrategy;
import edu.washington.cs.detector.SWTAppUIErrorMain;
import edu.washington.cs.detector.TestCommons;
import edu.washington.cs.detector.CGBuilder.CG;
import edu.washington.cs.detector.experiments.filters.MergeSameTailStrategy;
import edu.washington.cs.detector.experiments.filters.RemoveContainingNodeStrategy;
import edu.washington.cs.detector.experiments.filters.RemoveSameEntryStrategy;
import edu.washington.cs.detector.util.EclipsePluginCommons;
import edu.washington.cs.detector.util.Globals;
import edu.washington.cs.detector.util.Utils;

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
	
	//a ridiculous number
	public void testDetectUIErrorsByOneCFA() throws IOException,
			ClassHierarchyException {
		List<AnomalyCallChain> chains = super.reportUIErrors(null, CG.OneCFA);
		assertEquals(95645, chains.size());
		CallChainFilter filter = new CallChainFilter(chains);
		chains = filter.apply(new MergeSameTailStrategy());
		System.out.println("No of chains after removing common tails: " + chains.size());
		//Utils.dumpAnomalyCallChains(chains, SWTAppUIErrorMain.default_log);
		
		filter = new CallChainFilter(chains);
		chains = filter.apply(new RemoveSameEntryStrategy());
		System.out.println("No of chains after removing same starting node: " + chains.size());
		Utils.dumpAnomalyCallChains(chains, SWTAppUIErrorMain.default_log);
	}
	
	public void testDetectUIErrorsAndFilter() throws IOException,
	    ClassHierarchyException {
		List<AnomalyCallChain> chains = super.reportUIErrors(SWTAppUIErrorMain.default_log, CG.ZeroOneContainerCFA);
		
		System.out.println("No of chains before filtering: " + chains.size());
		
		CallChainFilter filter = new CallChainFilter(chains);
		chains = filter.apply(new RemoveSystemCallStrategy());
		System.out.println("No of chains after filtering system classes: " + chains.size());
		
		filter = new CallChainFilter(chains);
		chains = filter.apply(new RemoveContainingNodeStrategy("run(Lorg/eclipse/jface/operation/IRunnableWithProgress;ZLorg/eclipse/core/runtime/IProgressMonitor;Lorg/eclipse/swt/widgets/Display;)V"));
		System.out.println("No of chains after filtering RSE-specific FP: " + chains.size());
		
		filter = new CallChainFilter(chains);
		//exception capturing methods
		chains = filter.apply(new RemoveContainingNodeStrategy("Lorg/eclipse/jface/operation/ModalContext$ModalContextThread, run()V"));
		System.out.println("No of chains after filtering exception-capture FP: " + chains.size());
		
		int count = 0;
		for(AnomalyCallChain chain : chains) {
			System.out.println(++count + "-th chain.");
			System.out.println("--------------------");
			System.out.println(Globals.lineSep);
			System.out.println(chain.getFullCallChainAsString());
			System.out.println(Globals.lineSep);
			System.out.println(Globals.lineSep);
		}
		
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