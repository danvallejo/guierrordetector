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

//project page: http://marketplace.eclipse.org/content/pydev-python-ide-eclipse
//too large to analyze
public class TestPyDev extends AbstractEclipsePluginTest {
	
	private String[] allPluginFiles = new String[] {
			"D:\\research\\guierror\\subjects\\plugins-for-evaluate\\pydev\\PyDev 2.3.0\\plugins\\com.python.pydev.analysis_2.3.0.2011121518\\plugin.xml",
			"D:\\research\\guierror\\subjects\\plugins-for-evaluate\\pydev\\PyDev 2.3.0\\plugins\\com.python.pydev.codecompletion_2.3.0.2011121518\\plugin.xml",
			"D:\\research\\guierror\\subjects\\plugins-for-evaluate\\pydev\\PyDev 2.3.0\\plugins\\com.python.pydev.debug_2.3.0.2011121518\\plugin.xml",
			"D:\\research\\guierror\\subjects\\plugins-for-evaluate\\pydev\\PyDev 2.3.0\\plugins\\com.python.pydev.fastparser_2.3.0.2011121518\\plugin.xml",
			"D:\\research\\guierror\\subjects\\plugins-for-evaluate\\pydev\\PyDev 2.3.0\\plugins\\com.python.pydev.refactoring_2.3.0.2011121518\\plugin.xml",
			"D:\\research\\guierror\\subjects\\plugins-for-evaluate\\pydev\\PyDev 2.3.0\\plugins\\com.python.pydev_2.3.0.2011121518\\plugin.xml",
			"D:\\research\\guierror\\subjects\\plugins-for-evaluate\\pydev\\PyDev 2.3.0\\plugins\\org.python.pydev.ast_2.3.0.2011121518\\plugin.xml",
			"D:\\research\\guierror\\subjects\\plugins-for-evaluate\\pydev\\PyDev 2.3.0\\plugins\\org.python.pydev.core_2.3.0.2011121518\\plugin.xml",
			"D:\\research\\guierror\\subjects\\plugins-for-evaluate\\pydev\\PyDev 2.3.0\\plugins\\org.python.pydev.customizations_2.3.0.2011121518\\plugin.xml",
			"D:\\research\\guierror\\subjects\\plugins-for-evaluate\\pydev\\PyDev 2.3.0\\plugins\\org.python.pydev.debug_2.3.0.2011121518\\plugin.xml",
			"D:\\research\\guierror\\subjects\\plugins-for-evaluate\\pydev\\PyDev 2.3.0\\plugins\\org.python.pydev.django_2.3.0.2011121518\\plugin.xml",
			"D:\\research\\guierror\\subjects\\plugins-for-evaluate\\pydev\\PyDev 2.3.0\\plugins\\org.python.pydev.django_templates_2.3.0.2011121518\\plugin.xml",
			"D:\\research\\guierror\\subjects\\plugins-for-evaluate\\pydev\\PyDev 2.3.0\\plugins\\org.python.pydev.help_2.3.0.2011121518\\plugin.xml",
			"D:\\research\\guierror\\subjects\\plugins-for-evaluate\\pydev\\PyDev 2.3.0\\plugins\\org.python.pydev.jython_2.3.0.2011121518\\plugin.xml",
			"D:\\research\\guierror\\subjects\\plugins-for-evaluate\\pydev\\PyDev 2.3.0\\plugins\\org.python.pydev.parser_2.3.0.2011121518\\plugin.xml",
			"D:\\research\\guierror\\subjects\\plugins-for-evaluate\\pydev\\PyDev 2.3.0\\plugins\\org.python.pydev.red_core_2.3.0.2011121518\\plugin.xml",
			"D:\\research\\guierror\\subjects\\plugins-for-evaluate\\pydev\\PyDev 2.3.0\\plugins\\org.python.pydev.refactoring_2.3.0.2011121518\\plugin.xml",
			"D:\\research\\guierror\\subjects\\plugins-for-evaluate\\pydev\\PyDev 2.3.0\\plugins\\org.python.pydev_2.3.0.2011121518\\plugin.xml"
	};

	@Override
	protected String getAppPath() {
		return "D:\\research\\guierror\\subjects\\plugins-for-evaluate\\pydev\\extracted-jars";
	}

	@Override
	protected String getDependentJars() {
		return EclipsePluginCommons.DEPENDENT_JARS;
	}
	
	@Override
	protected String getExtraJars() {
		return "D:\\develop-tools\\eclipse\\eclipse\\plugins\\org.eclipse.core.resources_3.6.1.R36x_v20110131-1630.jar";
	}
	
	@Override
	protected Iterable<Entrypoint> getEntrypoints(ClassHierarchy cha) {
		//all declared
		Collection<String> declaredClassesStrs = //EclipsePluginUtils.getAllDeclaredClassesFromDir(getAppPath());
			Utils.extractClassFromPlugXMLFiles(allPluginFiles);
		
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
		super.setCGType(CG.RTA);
//		super.setCGType(CG.ZeroCFA);
		super.setThreadStartGuider(new CGTraverseSWTGuider());
		super.setUIAnomalyGuider(new CGTraverseSWTGuider());
		super.setPackages(new String[]{"com.python.pydev", "org.python.pydev"});
		
		Collection<AnomalyCallChain> chains = super.reportUIErrors();
		
		int count = 0;
		for(AnomalyCallChain chain : chains) {
			Log.logln("The " + (count++) + "-th chain");
			Log.logln(chain.getFullCallChainAsString());
		}
		System.out.println("Log: " + count + " output.");
	}

}
