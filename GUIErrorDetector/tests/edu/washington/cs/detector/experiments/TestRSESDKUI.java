package edu.washington.cs.detector.experiments;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.core.tests.callGraph.CallGraphTestUtil;
import com.ibm.wala.ipa.callgraph.AnalysisCache;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.CallGraphBuilder;
import com.ibm.wala.ipa.callgraph.CallGraphBuilderCancelException;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.callgraph.impl.DefaultEntrypoint;
import com.ibm.wala.ipa.callgraph.impl.Util;
import com.ibm.wala.ipa.callgraph.propagation.PropagationCallGraphBuilder;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.TypeName;
import com.ibm.wala.util.collections.HashSetFactory;
import com.ibm.wala.util.config.AnalysisScopeReader;
import com.ibm.wala.util.io.FileProvider;

import edu.washington.cs.detector.AbstractUITest;
import edu.washington.cs.detector.AnomalyCallChain;
import edu.washington.cs.detector.CGEntryManager;
import edu.washington.cs.detector.CallChainFilter;
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
		
		Utils.dumpAnomalyCallChains(chains, "./logs/rse_merge_tail.txt");
		
		filter = new CallChainFilter(chains);
		chains = filter.apply(new RemoveSameEntryStrategy());
		System.out.println("No of chains after removing same starting node: " + chains.size());
		Utils.dumpAnomalyCallChains(chains, "./logs/rse_remove_entry.txt");
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
	
	public void testEntryPointInCallGraph() throws IOException, ClassHierarchyException, IllegalArgumentException, CallGraphBuilderCancelException {
		String myClass = "org.eclipse.dstore.internal.core.client.ClientUpdateHandler";

		//PropagationCallGraphBuilder.DEBUG_ENTRYPOINTS = true;
		
		String appPath =  TestCommons.assemblyAppPath(getAppPath(), getDependentJars());
		AnalysisScope scope = AnalysisScopeReader.makeJavaBinaryAnalysisScope(appPath,
				FileProvider.getFile(CallGraphTestUtil.REGRESSION_EXCLUSIONS));
		ClassHierarchy cha = ClassHierarchy.make(scope);

		ClassLoaderReference clr = scope.getApplicationLoader();
		final HashSet<Entrypoint> result = HashSetFactory.make();
		for (IClass klass : cha) {
			if (klass.getClassLoader().getReference().equals(clr)) {
				Collection<IMethod> allMethods = klass.getDeclaredMethods();
				for(IMethod m : allMethods) {
					if(!m.isPublic() || m.isAbstract()) {
						continue;
					}
					TypeName tn = m.getDeclaringClass().getName();
					String fullClassName = (tn.getPackage() != null ? Utils.translateSlashToDot(tn.getPackage().toString()) + "." : "")
					    + tn.getClassName().toString();
					if(!fullClassName.equals(myClass)) {
						continue;
					}
					result.add(new DefaultEntrypoint(m, cha));
				}
			}
		}

		Iterable<Entrypoint> entrypoints = new Iterable<Entrypoint>() {
			public Iterator<Entrypoint> iterator() {
				return result.iterator();
			}
		}; 

		System.out.println("Number of entry points: " + Utils.countIterable(entrypoints));
		Utils.dumpCollection(entrypoints, System.out);
		
		/* Explicitly set entrypoints in the AnalysisOptions */
		AnalysisOptions options = new AnalysisOptions(scope, entrypoints);
		CallGraphBuilder builder = Util.makeZeroCFABuilder(options, new AnalysisCache(), cha, scope);

		System.out.println("Type of the call graph builder: " + builder.getClass());
		System.out.println("Before making call graph, the number of entry points: "
				+ Utils.countIterable(options.getEntrypoints()));
		CallGraph callgraph = builder.makeCallGraph(options, null);

		/*the size of entryNodes is DIFFERENT than the size of entrypoints*/
		Collection<CGNode> entryNodes = callgraph.getEntrypointNodes();
		System.out.println("Number of entry nodes: " + entryNodes.size());
		System.out.println(entryNodes);
		
		assertEquals("The entry size is not equal.", entryNodes.size(), Utils.countIterable(entrypoints));
	}
}