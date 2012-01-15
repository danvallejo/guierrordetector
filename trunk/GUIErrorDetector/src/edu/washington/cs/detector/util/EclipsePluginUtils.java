package edu.washington.cs.detector.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.callgraph.impl.DefaultEntrypoint;
import com.ibm.wala.ipa.cha.ClassHierarchy;

import edu.washington.cs.detector.walaextension.ParamTypeCustomizedEntrypoint;

public class EclipsePluginUtils {
	
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
	private static IClass SWT_EVENT_LISTENER = null;
	private static String swtEventListener = "org.eclipse.swt.internal.SWTEventListener";
	private static IClass I_ACTION_DELEGATE = null;
	private static String iActionDelegate = "org.eclipse.ui.IActionDelegate";
	private static IClass JOB_CHANGE_LISTENER = null;
	private static String jobChangeListener = "org.eclipse.core.runtime.jobs.IJobChangeListener";
	
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
	public static Collection<Entrypoint> getAllJobChangeListenerMethods(ClassHierarchy cha, String[] packages, boolean considerSubType) {
		Collection<IClass> allJobChangedListeners =
		    EclipsePluginUtils.getAllAppSubClasses(cha, getJobChangeListener(cha), packages);
		
		Set<String> jobListenerMethods = getJobListenerMethods();
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
}
