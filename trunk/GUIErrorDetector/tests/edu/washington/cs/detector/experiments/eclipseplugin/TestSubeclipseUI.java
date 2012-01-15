package edu.washington.cs.detector.experiments.eclipseplugin;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.callgraph.impl.DefaultEntrypoint;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.util.collections.HashSetFactory;

import edu.washington.cs.detector.AnomalyCallChain;
import edu.washington.cs.detector.CallChainFilter;
import edu.washington.cs.detector.SWTAppUIErrorMain;
import edu.washington.cs.detector.TestCommons;
import edu.washington.cs.detector.UIAnomalyDetector;
import edu.washington.cs.detector.CGBuilder.CG;
import edu.washington.cs.detector.experiments.filters.RemoveSystemCallStrategy;
import edu.washington.cs.detector.guider.CGTraverseSWTGuider;
import edu.washington.cs.detector.util.EclipsePluginCommons;
import edu.washington.cs.detector.util.EclipsePluginUtils;
import edu.washington.cs.detector.util.Globals;
import edu.washington.cs.detector.util.Log;
import edu.washington.cs.detector.util.Utils;
import edu.washington.cs.detector.util.WALAUtils;
import edu.washington.cs.detector.walaextension.ParamTypeCustomizedEntrypoint;

public class TestSubeclipseUI extends AbstractEclipsePluginTest {

	public static String PLUGIN_DIR = 
		// TestCommons.subeclipse_1_6 + Globals.fileSep 	+ "plugins"; //this is for 1.6.2
		"D:\\research\\guierror\\subjects\\plugins-for-evaluate\\subclipse-plugin-1.8.4\\plugins"; //for 1.8.4
	
	private boolean init_all_arg_objs = false;

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
		
		Collection<String> declaredClassesStrs = EclipsePluginUtils.getAllDeclaredClassesFromDir(getAppPath());
		Collection<IClass> declaredClasses = EclipsePluginUtils.getAllClasses(cha, declaredClassesStrs);
		Iterable<Entrypoint> entries = EclipsePluginUtils.getAllPublicProtectedEntypoints(declaredClasses, cha, false);
		
		return entries;
	}

	@Override
	protected Collection<CGNode> getStartNodes(Iterable<CGNode> allNodes,
			ClassHierarchy cha) {
		Collection<CGNode> startNodes = new HashSet<CGNode>();

		
		Collection<Entrypoint> actionEntrypoints = EclipsePluginUtils.getAllPublicProtectedActionEntrypoints(cha, super.getPackages(), false);
		Collection<Entrypoint> listenerEventHandler = EclipsePluginUtils.getAllPublicProtectedEventHandlerMethods(cha, super.getPackages(), false);
		Collection<Entrypoint> jobChangeEntries = EclipsePluginUtils.getAllJobChangeListenerMethods(cha, super.getPackages(), false);
		
		Set<String> methods = new HashSet<String>();
		for(Entrypoint ep : actionEntrypoints) {
			methods.add(WALAUtils.getFullMethodName(ep.getMethod()));
		}
//		for(Entrypoint ep : listenerEventHandler) {
//			methods.add(WALAUtils.getFullMethodName(ep.getMethod()));
//		}
//		for(Entrypoint ep : jobChangeEntries) {
//			methods.add(WALAUtils.getFullMethodName(ep.getMethod()));
//		}
		
		for(CGNode node : allNodes) {
			String methodCall = WALAUtils.getFullMethodName(node.getMethod());
			if(methods.contains(methodCall)) {
				startNodes.add(node);
			}
		}
		
		return startNodes;
	}
	
	public void testAllClasses() {
		Collection<String> clazzCol = EclipsePluginUtils.getAllDeclaredClassesFromDir(getAppPath());
		System.out.println("Total num: " + clazzCol.size());
		for(String c : clazzCol) {
			System.out.println("  " + c);
		}
	}

	public void testDetectUIErrors() throws IOException,
	    ClassHierarchyException {
		
		Log.logConfig("./log.txt");
		UIAnomalyDetector.DEBUG = true;
		
		this.init_all_arg_objs = true;
		
//		super.setCGType(CG.ZeroCFA);
		super.setCGType(CG.OneCFA);
		super.setThreadStartGuider(new CGTraverseSWTGuider());
		super.setUIAnomalyGuider(new CGTraverseSWTGuider());
		super.setPackages(new String[]{"org.tigris"});
		
		List<AnomalyCallChain> chains = super.reportUIErrors();
		
		int count = 0;
		for(AnomalyCallChain chain : chains) {
			Log.logln("The " + (count++) + "-th chain");
			Log.logln(chain.getFullCallChainAsString());
		}
		
    }
}
