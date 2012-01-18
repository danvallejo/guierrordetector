package edu.washington.cs.detector.experiments.eclipseplugin;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;

import edu.washington.cs.detector.AnomalyCallChain;
import edu.washington.cs.detector.UIAnomalyDetector;
import edu.washington.cs.detector.CGBuilder.CG;
import edu.washington.cs.detector.guider.CGTraverseSWTGuider;
import edu.washington.cs.detector.util.EclipsePluginCommons;
import edu.washington.cs.detector.util.EclipsePluginUtils;
import edu.washington.cs.detector.util.Globals;
import edu.washington.cs.detector.util.Log;
import edu.washington.cs.detector.util.Utils;

//no errors
public class TestFileSync4Eclipse extends AbstractEclipsePluginTest {
	@Override
	protected String getAppPath() {
		return "D:\\research\\guierror\\subjects\\plugins-for-evaluate\\filesync4eclipse\\filesync4eclipse-jar";
	}

	@Override
	protected String getDependentJars() {
		return EclipsePluginCommons.DEPENDENT_JARS;
	}
	
	@Override
	protected String getExtraJars() {
		return "D:\\develop-tools\\eclipse\\eclipse\\plugins\\org.eclipse.core.resources_3.6.1.R36x_v20110131-1630.jar"
		    + Globals.pathSep
		    + "D:\\develop-tools\\eclipse\\eclipse\\plugins\\org.eclipse.ui.ide_3.6.2.M20101201-0800.jar"
		    + Globals.pathSep
		    + "D:\\develop-tools\\eclipse\\eclipse\\plugins\\org.eclipse.core.net_1.2.100.I20100511-0800.jar"
		    + Globals.pathSep
		    + "D:\\develop-tools\\eclipse\\eclipse\\plugins\\org.eclipse.core.variables_3.2.400.v20100505.jar"
		    + Globals.pathSep
		    + "D:\\develop-tools\\eclipse\\eclipse\\plugins\\org.eclipse.ui.views_3.5.1.M20110202-0800.jar";
	}
	
	@Override
	protected Iterable<Entrypoint> getEntrypoints(ClassHierarchy cha) {
		//all declared
		Collection<String> declaredClassesStrs = EclipsePluginUtils.getAllDeclaredClassesFromDir(getAppPath());
		Utils.dumpCollection(declaredClassesStrs, "./logs/declared_classes.txt");
		Collection<IClass> declaredClasses = EclipsePluginUtils.getAllClasses(cha, declaredClassesStrs);
		Collection<Entrypoint> entries = EclipsePluginUtils.getAllPublicProtectedEntypoints(declaredClasses, cha, false);
		
		//get all plugin methods
		Collection<Entrypoint> pluginMethods = EclipsePluginUtils.getAllPluginMethodsAsEntrypoint(cha, super.getPackages());
		
		Collection<IClass> jobClasses = EclipsePluginUtils.getAllJobClasses(cha, super.getPackages());
		Collection<Entrypoint> jobMethods = EclipsePluginUtils.getAllPublicProtectedEntypoints(jobClasses, cha, false);
		
		Collection<Entrypoint> actionEntrypoints = EclipsePluginUtils.getAllPublicProtectedActionEntrypoints(cha, super.getPackages(), false);
		Collection<Entrypoint> listenerEventHandler = EclipsePluginUtils.getAllPublicProtectedEventHandlerMethods(cha, super.getPackages(), false);
		
		//all UI element
		Collection<IClass> workbenchClasses = EclipsePluginUtils.getAllAppSubClasses(cha, EclipsePluginUtils.getWorkbenchPart(cha),
				super.getPackages());
		Collection<Entrypoint> workbenchPoints = EclipsePluginUtils.getAllPublicProtectedEntypoints(workbenchClasses, cha, false);
		
		Collection<IClass> windowPartClasses = EclipsePluginUtils.getAllAppSubClasses(cha, EclipsePluginUtils.getWindowPart(cha),
				super.getPackages());
		Collection<Entrypoint> windowPoints = EclipsePluginUtils.getAllPublicProtectedEntypoints(windowPartClasses, cha, false);
		
		Collection<IClass> pageClasses = EclipsePluginUtils.getAllAppSubClasses(cha, EclipsePluginUtils.getPagePart(cha),
				super.getPackages());
		Collection<Entrypoint> pagePoints = EclipsePluginUtils.getAllPublicProtectedEntypoints(pageClasses, cha, false);
		
		Collection<IClass> dialogClasses = EclipsePluginUtils.getAllAppSubClasses(cha, EclipsePluginUtils.getDialog(cha),
				super.getPackages());
		Collection<Entrypoint> dialogPoints = EclipsePluginUtils.getAllPublicProtectedEntypoints(dialogClasses, cha, false);
		
		Collection<IClass> wizardClasses = EclipsePluginUtils.getAllAppSubClasses(cha, EclipsePluginUtils.getWizard(cha),
				super.getPackages());
		Collection<Entrypoint> wizardPoints = EclipsePluginUtils.getAllPublicProtectedEntypoints(wizardClasses, cha, false);
		
		Collection<IClass> viewerClasses = EclipsePluginUtils.getAllAppSubClasses(cha, EclipsePluginUtils.getViewer(cha),
				super.getPackages());
		Collection<Entrypoint> viewerPoints = EclipsePluginUtils.getAllPublicProtectedEntypoints(viewerClasses, cha, false);
		
		entries.addAll(pluginMethods);
		entries.addAll(workbenchPoints);
		entries.addAll(windowPoints);
		entries.addAll(pagePoints);
		entries.addAll(dialogPoints);
		entries.addAll(wizardPoints);
		entries.addAll(viewerPoints);
		
//		entries.addAll(jobMethods);
		
		return entries;
	}

	@Override
	protected Collection<CGNode> getStartNodes(Iterable<CGNode> allNodes,
			ClassHierarchy cha) {
		Collection<CGNode> startNodes = new HashSet<CGNode>();
		return startNodes;
	}

	public void testFindErrors() throws ClassHierarchyException, IOException {
		Log.logConfig("./log.txt");
		UIAnomalyDetector.DEBUG = true;
		
		super.setSeeUIAccessRunnable(true);
		
//		super.setCGType(CG.ZeroCFA);
		super.setCGType(CG.OneCFA);
		super.setThreadStartGuider(new CGTraverseSWTGuider());
		super.setUIAnomalyGuider(new CGTraverseSWTGuider());
		super.setPackages(new String[]{"de.loskutov.fs"});
		
		Collection<AnomalyCallChain> chains = super.reportUIErrors();
		
		int count = 0;
		for(AnomalyCallChain chain : chains) {
			Log.logln("The " + (count++) + "-th chain");
			Log.logln(chain.getFullCallChainAsString());
		}
		System.out.println("Log: " + count + " output.");
	}
	
}
