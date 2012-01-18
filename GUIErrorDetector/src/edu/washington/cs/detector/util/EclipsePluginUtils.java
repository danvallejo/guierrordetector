package edu.washington.cs.detector.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.callgraph.impl.DefaultEntrypoint;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.util.graph.Graph;

import edu.washington.cs.detector.walaextension.ParamTypeCustomizedEntrypoint;

public class EclipsePluginUtils {
	
	private static IClass UI_PLUGIN = null;
	private static String uiPlugin = "org.eclipse.ui.plugin.AbstractUIPlugin";
	public static IClass getUIPlugin(ClassHierarchy cha) {
		if(UI_PLUGIN == null) {
			UI_PLUGIN = WALAUtils.lookupClass(cha, uiPlugin);
		}
		if(UI_PLUGIN == null) {
			throw new RuntimeException("The class can not be loaded: " + uiPlugin);
		}
		return UI_PLUGIN;
	}
	public static Collection<Entrypoint> getAllPluginMethodsAsEntrypoint(ClassHierarchy cha, String[] packages) {
		IClass plugin = getUIPlugin(cha);
		Collection<IClass> allPluginClasses = getAllAppSubClasses(cha, plugin, packages);
		
		Collection<Entrypoint> colls = new HashSet<Entrypoint>();
		
		for(IClass c : allPluginClasses) {
			for(IMethod m : c.getDeclaredMethods()) {
				IClass decl = m.getDeclaringClass();
				String declPackage = WALAUtils.getJavaPackageName(decl);
				if(declPackage.startsWith("java.")) {
					continue;
				}
				if(m.isPublic() || m.isProtected()) {
				    colls.add(new DefaultEntrypoint(m, cha));
				}
			}
		}
		
		return colls;
	}
	
	public static Collection<String> getAllDeclaredClasses(String jarFile) {
		try {
			return Utils.extractClassFromPluginXML(jarFile);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static Collection<String> getAllDeclaredClassesFromDir(String dir) {
		try {
			List<File> files = Files.getFileListing(new File(dir), "jar");
			Collection<String> classes = new HashSet<String>();
			for(File f : files) {
				classes.addAll(getAllDeclaredClasses(f.getAbsolutePath()));
			}
			return classes;
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static Collection<IClass> getAllClasses(ClassHierarchy cha, Collection<String> classes) {
		Collection<IClass> clz = new HashSet<IClass>();
		
		for(IClass c : cha) {
			String cstr = WALAUtils.getJavaFullClassName(c);
			if(classes.contains(cstr)) {
				clz.add(c);
			}
		}
		
		return clz;
	}
	
	public static Collection<Entrypoint> getAllPublicProtectedEntypoints(Collection<IClass> cs, ClassHierarchy cha, boolean considerSubtype) {
		Collection<Entrypoint> entries = new HashSet<Entrypoint>();
		
		Collection<IMethod> ms = new HashSet<IMethod>();
		for(IClass c : cs) {
			for(IMethod m : c.getDeclaredMethods()) {
				if(m.isPublic() || m.isProtected()) {
					ms.add(m);
				}
			}
		}
		
		for(IMethod m : ms) {
			if(!considerSubtype) {
				entries.add(new DefaultEntrypoint(m, cha));
			} else {
				entries.add(new ParamTypeCustomizedEntrypoint(m, cha));
			}
		}
		
		return entries;
	}
	/**
	 * The UI parts in eclipse
	 * */
	private static IClass WORKBENCH_PART =  null;
	private static String workbenchPart = "org.eclipse.ui.part.WorkbenchPart"; //include viewpart and editor part
	private static IClass WINDOW = null;
	private static String windowPart = "org.eclipse.jface.window.Window"; //window part
	private static IClass PAGE_VIEW = null;
	private static String pageView = "org.eclipse.ui.part.IPageBookViewPage"; //page part
	private static IClass DIALOG_PAGE = null;
	private static String dialogView = "org.eclipse.jface.dialogs.IDialogPage";
	private static IClass IWIZARD = null;
	private static String iwizard = "org.eclipse.jface.wizard.IWizard";
	private static IClass VIEWER = null;
	private static String viewer = "org.eclipse.jface.viewers.Viewer";
	
	public static IClass getWorkbenchPart(ClassHierarchy cha) {
		if(WORKBENCH_PART == null) {
			WORKBENCH_PART = WALAUtils.lookupClass(cha, workbenchPart);
		}
		if(WORKBENCH_PART == null) {
			throw new RuntimeException("The class can not be loaded: " + workbenchPart);
		}
		return WORKBENCH_PART;
	}
	
	public static IClass getWindowPart(ClassHierarchy cha) {
		if(WINDOW == null) {
			WINDOW = WALAUtils.lookupClass(cha, windowPart);
		}
		if(WINDOW == null) {
			throw new RuntimeException("The class can not be loaded: " + windowPart);
		}
		return WINDOW;
	}
	
	public static IClass getPagePart(ClassHierarchy cha) {
		if(PAGE_VIEW == null) {
			PAGE_VIEW = WALAUtils.lookupClass(cha, pageView);
		}
		if(PAGE_VIEW == null) {
			throw new RuntimeException("The class can not be loaded: " + pageView);
		}
		return PAGE_VIEW;
	}
	
	public static IClass getDialog(ClassHierarchy cha) {
		if(DIALOG_PAGE == null) {
			DIALOG_PAGE = WALAUtils.lookupClass(cha, dialogView);
		}
		if(DIALOG_PAGE == null) {
			throw new RuntimeException("The class can not be loaded: " + dialogView);
		}
		return DIALOG_PAGE;
	}
	
	public static IClass getWizard(ClassHierarchy cha) {
		if(IWIZARD == null) {
			IWIZARD = WALAUtils.lookupClass(cha, iwizard);
		}
		if(IWIZARD == null) {
			throw new RuntimeException("The class can not be loaded: " + iwizard);
		}
		return IWIZARD;
	}
	
	public static IClass getViewer(ClassHierarchy cha) {
		if(VIEWER == null) {
			VIEWER = WALAUtils.lookupClass(cha, viewer);
		}
		if(VIEWER == null) {
			throw new RuntimeException("The class can not be loaded: " + viewer);
		}
		return VIEWER;
	}
	
	/**
	 * All SWT Listeners, and all methods that extend eclipse Actions
	 * 1. org.eclipse.ui.IActionDelegate  (it is the interface)
	 * 2. org.eclipse.swt.internal.EventListener 
	 * 3. org.eclipse.ui.actions.ActionDelegate
	 * 
	 * JobChangeAdapter
	 * 
	 * Explanation on actions:
	 * http://stackoverflow.com/questions/552435/eclipse-rcp-actions-vs-commands
	 * 
	 * Workbench
	 * 
	 * */
	private static IClass ACTION_DELEGATION = null;
	private static String actionDelegation = "org.eclipse.ui.actions.ActionDelegate";
	private static IClass EVENT_LISTENER = null;
	private static String eventListener = "java.util.EventListener";
	private static IClass SWT_EVENT_LISTENER = null;
	private static String swtEventListener = "org.eclipse.swt.internal.SWTEventListener";
	private static IClass I_ACTION_DELEGATE = null;
	private static String iActionDelegate = "org.eclipse.ui.IActionDelegate";
	private static IClass JOB_CHANGE_LISTENER = null;
	private static String jobChangeListener = "org.eclipse.core.runtime.jobs.IJobChangeListener";
	private static IClass RESOURCE_CHANGE_LISTENER = null;
	private static String resourceChangeListener = "org.eclipse.core.resources.IResourceChangeListener";
	//public void resourceChanged(IResourceChangeEvent event); invoked by non-UI
	private static IClass ACTION = null;
	private static String action = "org.eclipse.jface.action.Action";  //run() invoked by non-UI
	//all public methods in a Job Change Listener
	
	public static IClass getActionDelegation(ClassHierarchy cha) {
		if(ACTION_DELEGATION == null) {
			ACTION_DELEGATION = WALAUtils.lookupClass(cha, actionDelegation);
		}
		if(ACTION_DELEGATION == null) {
			throw new RuntimeException("The class is not found: " + actionDelegation);
		}
		return ACTION_DELEGATION;
	}
	
	public static IClass getSWTEventListener(ClassHierarchy cha) {
		if(SWT_EVENT_LISTENER == null) {
			SWT_EVENT_LISTENER = WALAUtils.lookupClass(cha, swtEventListener);
		}
		if(SWT_EVENT_LISTENER == null) {
			throw new RuntimeException("The class is not found: " + swtEventListener);
		}
		return SWT_EVENT_LISTENER;
	}
	
	public static IClass getI_ActionDelegate(ClassHierarchy cha) {
		if(I_ACTION_DELEGATE == null) {
			I_ACTION_DELEGATE = WALAUtils.lookupClass(cha, iActionDelegate);
		}
		if(I_ACTION_DELEGATE == null) {
			throw new RuntimeException("The class is not found: " + iActionDelegate);
		}
		return I_ACTION_DELEGATE;
	}
	
	public static IClass getJobChangeListener(ClassHierarchy cha) {
		if(JOB_CHANGE_LISTENER == null) {
			JOB_CHANGE_LISTENER = WALAUtils.lookupClass(cha, jobChangeListener);
		}
		if(JOB_CHANGE_LISTENER == null) {
			throw new RuntimeException("The class is not found: " + jobChangeListener);
		}
		return JOB_CHANGE_LISTENER;
	}
	
	public static IClass getEventListener(ClassHierarchy cha) {
		if(EVENT_LISTENER == null) {
			EVENT_LISTENER = WALAUtils.lookupClass(cha, eventListener);
		}
		if(EVENT_LISTENER == null) {
			throw new RuntimeException("The class is not found: " + eventListener);
		}
		return EVENT_LISTENER;
	}
	
	public static IClass getResourceChangeListener(ClassHierarchy cha) {
		if(RESOURCE_CHANGE_LISTENER == null) {
			RESOURCE_CHANGE_LISTENER = WALAUtils.lookupClass(cha, resourceChangeListener);
		}
		if(RESOURCE_CHANGE_LISTENER == null) {
			throw new RuntimeException("The class is not found: " + resourceChangeListener);
		}
		return RESOURCE_CHANGE_LISTENER;
	}
	
	public static IClass getAction(ClassHierarchy cha) {
		if(ACTION == null) {
			ACTION = WALAUtils.lookupClass(cha, action);
		}
		if(ACTION == null) {
			throw new RuntimeException("The class is not found: " + action);
		}
		return ACTION;
	}
	
	/**
	 * Various methods to get the entry methods for eclipse plugins
	 * */
	//first get all action methods
	public static Collection<IClass> getAllAppSubClasses(ClassHierarchy cha, IClass topClass, String[] packages) {
		Collection<IClass> clzColl = new HashSet<IClass>();
		
		for(IClass c : cha) {
			if(!WALAUtils.isAppClass(c)) {
				continue;
			}
			if(packages != null) { 
				if(!WALAUtils.isClassInPackages(c, packages)) {
					continue;
				}
			}
			//check if it is Action
			if(cha.isAssignableFrom(topClass, c)) {
				clzColl.add(c);
			}
		}
		
		return clzColl;
	}
	public static Collection<Entrypoint> getAllPublicProtectedActionEntrypoints(ClassHierarchy cha, String[] packages, boolean considerSubType) {
		Collection<IClass> clzColl = getAllAppSubClasses(cha, EclipsePluginUtils.getActionDelegation(cha), packages);
		return getAllPublicProtectedEntypoints(clzColl, cha, considerSubType);
	}
	
	//then get all event listener classes
	private static Set<String> handlerMethodNames = null;
	
	public static Set<String> getHandlerMethodNames() {
		if(handlerMethodNames != null) {
			return handlerMethodNames;
		}
		handlerMethodNames = new HashSet<String>();
		
		handlerMethodNames.add("getMaximumValue");
		handlerMethodNames.add("getColumnSpan");
		handlerMethodNames.add("paintControl");
		handlerMethodNames.add("getChild");
		handlerMethodNames.add("getVisibleColumns");
		handlerMethodNames.add("getHelp");
		handlerMethodNames.add("getColumns");
		handlerMethodNames.add("dragEnter");
		handlerMethodNames.add("open");
		handlerMethodNames.add("hide");
		handlerMethodNames.add("selectColumn");
		handlerMethodNames.add("getRowHeaders");
		handlerMethodNames.add("getColumn");
		handlerMethodNames.add("getActionCount");
		handlerMethodNames.add("lineGetStyle");
		handlerMethodNames.add("isRowSelected");
		handlerMethodNames.add("getMinimumValue");
		handlerMethodNames.add("doAction");
		handlerMethodNames.add("removeSelection");
		handlerMethodNames.add("verifyText");
		handlerMethodNames.add("getChildAtPoint");
		handlerMethodNames.add("getHyperlinkIndex");
		handlerMethodNames.add("dropAccept");
		handlerMethodNames.add("getRanges");
		handlerMethodNames.add("keyPressed");
		handlerMethodNames.add("getSelectedCellCount");
		handlerMethodNames.add("treeExpanded");
		handlerMethodNames.add("getColumnHeaders");
		handlerMethodNames.add("dragSetData");
		handlerMethodNames.add("treeCollapsed");
		handlerMethodNames.add("focusLost");
		handlerMethodNames.add("itemClosed");
		handlerMethodNames.add("lineGetBackground");
		handlerMethodNames.add("keyReleased");
		handlerMethodNames.add("imageDataLoaded");
		handlerMethodNames.add("mouseEnter");
		handlerMethodNames.add("textChanging");
		handlerMethodNames.add("getNextOffset");
		handlerMethodNames.add("getPreviousOffset");
		handlerMethodNames.add("getColumnDescription");
		handlerMethodNames.add("menuHidden");
		handlerMethodNames.add("getColumnIndex");
		handlerMethodNames.add("dragLeave");
		handlerMethodNames.add("controlMoved");
		handlerMethodNames.add("getKeyboardShortcut");
		handlerMethodNames.add("getAnchorTarget");
		handlerMethodNames.add("getChildCount");
		handlerMethodNames.add("dragFinished");
		handlerMethodNames.add("getRole");
		handlerMethodNames.add("getFocus");
		handlerMethodNames.add("getColumnHeader");
		handlerMethodNames.add("getCaption");
		handlerMethodNames.add("focusGained");
		handlerMethodNames.add("controlResized");
		handlerMethodNames.add("getTextBounds");
		handlerMethodNames.add("getSelectedCells");
		handlerMethodNames.add("completed");
		handlerMethodNames.add("getRowSpan");
		handlerMethodNames.add("widgetDefaultSelected");
		handlerMethodNames.add("setCaretOffset");
		handlerMethodNames.add("getDescription");
		handlerMethodNames.add("deselectColumn");
		handlerMethodNames.add("getSelectedRows");
		handlerMethodNames.add("getKeyBinding");
		handlerMethodNames.add("addSelection");
		handlerMethodNames.add("shellActivated");
		handlerMethodNames.add("getCharacterCount");
		handlerMethodNames.add("modifyText");
		handlerMethodNames.add("dragOperationChanged");
		handlerMethodNames.add("getSummary");
		handlerMethodNames.add("getLocation");
		handlerMethodNames.add("dragOver");
		handlerMethodNames.add("getColumnCount");
		handlerMethodNames.add("keyTraversed");
		handlerMethodNames.add("menuDetected");
		handlerMethodNames.add("getCaretOffset");
		handlerMethodNames.add("getDefaultAction");
		handlerMethodNames.add("getName");
		handlerMethodNames.add("getSelection");
		handlerMethodNames.add("getAnchor");
		handlerMethodNames.add("helpRequested");
		handlerMethodNames.add("setSelectedColumn");
		handlerMethodNames.add("getSelectionRange");
		handlerMethodNames.add("showList");
		handlerMethodNames.add("getState");
		handlerMethodNames.add("getTable");
		handlerMethodNames.add("drop");
		handlerMethodNames.add("widgetDisposed");
		handlerMethodNames.add("getChildren");
		handlerMethodNames.add("isColumnSelected");
		handlerMethodNames.add("show");
		handlerMethodNames.add("changed");
		handlerMethodNames.add("close");
		handlerMethodNames.add("getRowDescription");
		handlerMethodNames.add("getHyperlinkCount");
		handlerMethodNames.add("getRowIndex");
		handlerMethodNames.add("dragStart");
		handlerMethodNames.add("itemExpanded");
		handlerMethodNames.add("getRow");
		handlerMethodNames.add("setSelectedRow");
		handlerMethodNames.add("deselectRow");
		handlerMethodNames.add("textSet");
		handlerMethodNames.add("itemCollapsed");
		handlerMethodNames.add("getSelectedColumns");
		handlerMethodNames.add("selectRow");
		handlerMethodNames.add("shellIconified");
		handlerMethodNames.add("textChanged");
		handlerMethodNames.add("mouseUp");
		handlerMethodNames.add("getVisibleRows");
		handlerMethodNames.add("isSelected");
		handlerMethodNames.add("getHyperlink");
		handlerMethodNames.add("mouseMove");
		handlerMethodNames.add("restore");
		handlerMethodNames.add("getCell");
		handlerMethodNames.add("getRowCount");
		handlerMethodNames.add("mouseDoubleClick");
		handlerMethodNames.add("maximize");
		handlerMethodNames.add("mouseDown");
		handlerMethodNames.add("getAttributes");
		handlerMethodNames.add("getEndIndex");
		handlerMethodNames.add("getValue");
		handlerMethodNames.add("mouseScrolled");
		handlerMethodNames.add("dragDetected");
		handlerMethodNames.add("getOffsetAtPoint");
		handlerMethodNames.add("shellDeiconified");
		handlerMethodNames.add("getColumnHeaderCells");
		handlerMethodNames.add("getRowHeaderCells");
		handlerMethodNames.add("setSelection");
		handlerMethodNames.add("getSelectionCount");
		handlerMethodNames.add("getSelectedColumnCount");
		handlerMethodNames.add("shellDeactivated");
		handlerMethodNames.add("authenticate");
		handlerMethodNames.add("getText");
		handlerMethodNames.add("verifyKey");
		handlerMethodNames.add("shellClosed");
		handlerMethodNames.add("getSelectedRowCount");
		handlerMethodNames.add("getCurrentValue");
		handlerMethodNames.add("getStartIndex");
		handlerMethodNames.add("getTextAttributes");
		handlerMethodNames.add("getRows");
		handlerMethodNames.add("setCurrentValue");
		handlerMethodNames.add("getRowHeader");
		handlerMethodNames.add("paintObject");
		handlerMethodNames.add("mouseHover");
		handlerMethodNames.add("scrollText");
		handlerMethodNames.add("menuShown");
		handlerMethodNames.add("widgetSelected");
		handlerMethodNames.add("minimize");
		handlerMethodNames.add("getVisibleRanges");
		handlerMethodNames.add("lineGetSegments");
		handlerMethodNames.add("caretMoved");
		handlerMethodNames.add("changing");
		handlerMethodNames.add("mouseExit");
		handlerMethodNames.add("widgetArmed");
		
		return handlerMethodNames;
	}

	public static Collection<Entrypoint> getAllPublicProtectedEventHandlerMethods(ClassHierarchy cha, String[] packages, boolean considerSubType) {
		Set<String> handlerNames = getHandlerMethodNames();
		Collection<IClass> allListeners = EclipsePluginUtils.getAllAppSubClasses(cha, EclipsePluginUtils.getSWTEventListener(cha), packages);
		Set<IMethod> methodSet = new HashSet<IMethod>();
		for(IClass c : allListeners) {
			for(IMethod m : c.getDeclaredMethods()) {
				if(handlerNames.contains(m.getName().toString())) {
					methodSet.add(m);
				}
			}
		}
		//create the entries
		Collection<Entrypoint> entries = new HashSet<Entrypoint>();
		for(IMethod m : methodSet) {
			if(!considerSubType) {
				entries.add(new DefaultEntrypoint(m, cha));
			} else {
				entries.add(new ParamTypeCustomizedEntrypoint(m, cha));
			}
		}
		return entries;
	}

    //the job change listener
	//The JobListener is not running at EDT
	private static Set<String> jobListenerMethods = null;
	public static Set<String> getJobListenerMethods() {
		if(jobListenerMethods != null) {
			return jobListenerMethods;
		} else {
			jobListenerMethods = new HashSet<String>();
			jobListenerMethods.add("aboutToRun");
			jobListenerMethods.add("awake");
			jobListenerMethods.add("done");
			jobListenerMethods.add("running");
			jobListenerMethods.add("scheduled");
			jobListenerMethods.add("sleeping");
			return jobListenerMethods;
		}
	}
	
	//all job change listener methods
	public static Collection<Entrypoint> getAllJobChangeListenerMethods(ClassHierarchy cha, String[] packages, boolean considerSubType) {
		Collection<IClass> allJobChangedListeners =
		    EclipsePluginUtils.getAllAppSubClasses(cha, getJobChangeListener(cha), packages);
		
//		Set<String> jobListenerMethods = getJobListenerMethods();
		Collection<Entrypoint> entries = new HashSet<Entrypoint>();
		for(IClass c : allJobChangedListeners) {
			for(IMethod m : c.getDeclaredMethods()) {
//				if(jobListenerMethods.contains(m.getName().toString())) {
			    if(m.isPublic() || m.isProtected()) {
					if(!considerSubType) {
						entries.add(new DefaultEntrypoint(m, cha));
					} else {
						entries.add(new ParamTypeCustomizedEntrypoint(m, cha));
					}
				}
			}
		}
		return entries;
	}
	
	//eclipse Job, UIJob
	private static IClass UI_JOB = null;
	private static String uiJobStr = "org.eclipse.ui.progress.UIJob";
	
	public static IClass getUIJob(ClassHierarchy cha) {
		if(UI_JOB == null) {
			UI_JOB = WALAUtils.lookupClass(cha, uiJobStr);
		}
		if(UI_JOB == null) {
			throw new RuntimeException("The class is not found: " + uiJobStr);
		}
		return UI_JOB;
	}
	
    public static boolean isUIJob(IClass c, ClassHierarchy cha) {
    	IClass uiJob = getUIJob(cha);
    	return cha.isAssignableFrom(uiJob, c);
	}
	
	public static boolean isRunInUIThreadMethod(IMethod method) {
		return method.getName().toString().equals("runInUIThread"); //FIXME, not complete
	}
	
	private static IClass JOB = null;
	private static String jobStr = "org.eclipse.core.runtime.jobs.Job";
	public static IClass getJob(ClassHierarchy cha) {
		if(JOB == null) {
			JOB = WALAUtils.lookupClass(cha, jobStr);
		}
		if(JOB == null) {
			throw new RuntimeException("The class is not found: " + jobStr);
		}
		return JOB;
	}
	
	public static Collection<IClass> getAllJobClasses(ClassHierarchy cha, String[] packages) {
		IClass job = getJob(cha);
		Collection<IClass> retClasses = new HashSet<IClass>();
		
		for(IClass c : cha) {
			if(packages != null) {
				if(!WALAUtils.isClassInPackages(c, packages)) {
					continue;
				}
			}
			if(cha.isAssignableFrom(job, c)) {
				retClasses.add(c);
			}
		}
		
		return retClasses;
	}
	
	//get all Job run nodes
	//the job runnable methods may not be called in a UI thread
	public static Collection<CGNode> getAllJobRunMethods(Graph<CGNode> cg, ClassHierarchy cha, String[] packages) {
		IClass job = getJob(cha);
		Collection<CGNode> retNodes = new HashSet<CGNode>();
		
		for(CGNode node : cg) {
			IMethod m = node.getMethod();
			IClass c = m.getDeclaringClass();
			if(packages != null) {
				if(!WALAUtils.isClassInPackages(c, packages)) {
					continue;
				}
			}
			if(cha.isAssignableFrom(job, c)) {
				//IStatus run(IProgressMonitor monitor)
				//run(Lorg/eclipse/core/runtime/IProgressMonitor;)V
				if(m.getName().toString().equals("run") && m.getNumberOfParameters() == 2
						//&& m.getParameterType(1).toString().equals("Lorg/eclipse/core/runtime/IProgressMonitor;")
						) { //FIXME
					//System.out.println("-- " + node);
					retNodes.add(node);
				}
			}
		}
		
		return retNodes;
	}
	
	public static Collection<CGNode> getAllJobPublicProtectMethods(Graph<CGNode> cg, ClassHierarchy cha, String[] packages) {
		IClass job = getJob(cha);
		Collection<CGNode> retNodes = new HashSet<CGNode>();
		for(CGNode node : cg) {
			IMethod m = node.getMethod();
			IClass c = m.getDeclaringClass();
			if(packages != null) {
				if(!WALAUtils.isClassInPackages(c, packages)) {
					continue;
				}
			}
			if(cha.isAssignableFrom(job, c)) {
				if(m.isPublic() || m.isProtected()) {
					retNodes.add(node);
				}
			}
		}
		return retNodes;
	}
	
	/**
	 * 
	 * //public void resourceChanged(IResourceChangeEvent event); invoked by non-UI
	private static IClass ACTION = null;
	private static String action = "org.eclipse.jface.action.Action";  //run() invoked by non-UI
	//all public methods in a Job Change Listener
	 * */
	
	public static Collection<CGNode> getAllResourceChangeMethods(Graph<CGNode> graph, ClassHierarchy cha, String[] packages) {
		Collection<CGNode> resourceChangeMethods = new HashSet<CGNode>();
		
		IClass resourceListener = getResourceChangeListener(cha);
		for(CGNode node : graph) {
			IMethod m = node.getMethod();
			IClass c = m.getDeclaringClass();
			if(packages != null) {
				if(!WALAUtils.isClassInPackages(c, packages)) {
					continue;
				}
			}
			if(cha.isAssignableFrom(resourceListener, c)) {
				if(m.getName().toString().equals("resourceChanged")) { //XXX may not complete
					resourceChangeMethods.add(node);
				}
			}
		}
		
		return resourceChangeMethods;
	}
	
	public static Collection<CGNode> getAllActionRunMethods(Graph<CGNode> graph, ClassHierarchy cha, String[] packages) {
		Collection<CGNode>  actionRuns = new HashSet<CGNode>();
		IClass action = getAction(cha);
		for(CGNode node : graph) {
			IMethod m = node.getMethod();
			IClass c = m.getDeclaringClass();
			if(packages != null) {
				if(!WALAUtils.isClassInPackages(c, packages)) {
					continue;
				}
			}
			if(cha.isAssignableFrom(action, c)) {
				if(m.getName().toString().equals("run")) {
					actionRuns.add(node);
				}
			}
		}
		return actionRuns;
	}
	
	private static IClass IPROGRESS_MONITOR = null;
	private static String progressMonitor = "org.eclipse.core.runtime.IProgressMonitor";
	public static IClass getProgressMonitor(ClassHierarchy cha) {
		if(IPROGRESS_MONITOR == null) {
			IPROGRESS_MONITOR = WALAUtils.lookupClass(cha, progressMonitor);
		}
		if(IPROGRESS_MONITOR == null) {
			throw new RuntimeException("The class is not loaded: " + progressMonitor);
		}
		return IPROGRESS_MONITOR;
	}
	private static Set<String> monitorMethods = null;
	public static Set<String> getMonitorMethods() {
		if(monitorMethods == null) {
			monitorMethods = new HashSet<String>();
			monitorMethods.add("beginTask");
			monitorMethods.add("done");
			monitorMethods.add("internalWorked");
			monitorMethods.add("isCanceled");
			monitorMethods.add("setCanceled");
			monitorMethods.add("setTaskName");
			monitorMethods.add("subTask");
			monitorMethods.add("worked");
		}
		return monitorMethods;
	}
	//the progress monitor methods are not supposed to be executed
	//in the UI thread
	public static Collection<CGNode> getAllMonitorMethods(Graph<CGNode> cg, ClassHierarchy cha, String[] packages) {
		IClass monitor = getProgressMonitor(cha);
		Set<String> methods = getMonitorMethods();
		Collection<CGNode> retNodes = new HashSet<CGNode>();
		for(CGNode node : cg) {
			IMethod m = node.getMethod();
			IClass c = m.getDeclaringClass();
			if(packages != null) {
				if(!WALAUtils.isClassInPackages(c, packages)) {
					continue;
				}
			}
			if(cha.isAssignableFrom(monitor, c)) {
				//check the name here
				if(methods.contains(m.getName().toString())) {
					retNodes.add(node);
				}
			}
		}
		return retNodes;
	}
}
