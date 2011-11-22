package edu.washington.cs.detector.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.types.ClassLoaderReference;

public class SwingUtils {

	private static IClass JCOMPONENT = null;

	// javax.swing.ListModel
	// javax.swing.CellEditor
	// javax.swing.SpinnerModel
	// javax.swing.ButtonModel

	private static IClass[] models = null;
	
	public static IClass[] getModels(ClassHierarchy cha) {
		if(models != null) {
			return models;
		}
		//TODO fixme
		models = new IClass[4];
		models[0] = WALAUtils.lookupClass(cha, "javax.swing.ListModel");
		models[1] = WALAUtils.lookupClass(cha, "javax.swing.CellEditor");
		models[2] = WALAUtils.lookupClass(cha, "javax.swing.SpinnerModel");
		models[3] = WALAUtils.lookupClass(cha, "javax.swing.ButtonModel");
//		models[4] = WALAUtils.lookupClass(cha, "javax.swing.TableModel");
		
		Utils.checkNoNull(models);
		
		return models;
	}
	
	// javax.swing.Icon
	// javax.swing.JDialog
	// javax.swing.JFrame
	// javax.swing.JWindow
	private static IClass[] components = null;
	public static IClass[] getComponents(ClassHierarchy cha) {
		if(components != null) {
			return components;
		}
		components = new IClass[4];
		components[0] = WALAUtils.lookupClass(cha, "javax.swing.Icon");
		components[1] = WALAUtils.lookupClass(cha, "javax.swing.JDialog");
		components[2] = WALAUtils.lookupClass(cha, "javax.swing.JFrame");
		components[3] = WALAUtils.lookupClass(cha, "javax.swing.JWindow");
		
		Utils.checkNoNull(components);
		
		return components;
	}

	public static IClass getJComponent(ClassHierarchy cha) {
		if (JCOMPONENT != null) {
			return JCOMPONENT;
		}
		JCOMPONENT = WALAUtils.lookupClass(cha, "javax.swing.JComponent");
		return JCOMPONENT;
	}

	public static boolean isSwingComponentClass(ClassHierarchy cha, IClass clazz) {
		if (getJComponent(cha) == null) {
			throw new RuntimeException(
					"javax.swing.JComponent is not loaded yet.");
		}
		return cha.isAssignableFrom(JCOMPONENT, clazz);
	}

	/**
	 * This is potentially incomplete!
	 * */
	public static boolean isThreadUnsafeMethod(IMethod method, ClassHierarchy cha) {
		if (method.isAbstract()) {
			return false;
		}
		IClass clazz = method.getDeclaringClass();
		if (!isInSwingPackage(clazz)) {
			return false;
		}
		
		//see is that class a subclass of JComponent or Model
		if(cha.isAssignableFrom(getJComponent(cha), clazz)) {
			return true;
		}
		//sub class of a model?
		if(isSubClass(clazz, getModels(cha), cha)) {
			return true;
		}

		return false;
	}
	
	private static boolean isSubClass(IClass clazz, IClass[] allClasses, ClassHierarchy cha) {
		for(IClass c : allClasses) {
			if(cha.isAssignableFrom(c, clazz)) {
				return true;
			}
		}
		return false;
	}

	public static boolean isInSwingPackage(IClass clazz) {
		String packageName = WALAUtils.getJavaPackageName(clazz);
		return packageName.startsWith("javax.swing.");
	}
	
	/***
	 * Given a CGNode, check if it is the action handling method
	 * */
	public static Collection<CGNode> getAllAppEventhandlingMethods(Iterable<CGNode> nodes,
			ClassHierarchy cha) {
		Collection<CGNode> eventHandlingMethods = new LinkedList<CGNode>();
		for(CGNode node : nodes) {
			if(isEventHandlingMethod(node, cha)) {
				if(node.getMethod().getDeclaringClass().getClassLoader().getReference().equals(ClassLoaderReference.Application)) {
				    eventHandlingMethods.add(node);
				}
			}
		}
		return eventHandlingMethods;
	}
	
	public static Collection<CGNode> getAllEventhandlingMethods(Iterable<CGNode> nodes,
			ClassHierarchy cha, String[] packageNames) {
		Collection<CGNode> eventHandlingMethods = new LinkedList<CGNode>();
		for(CGNode node : nodes) {
			if(isEventHandlingMethod(node, cha)) {
				if(packageNames != null) {
					//should have a matched package
					String nodePackageName = WALAUtils.getJavaPackageName(node.getMethod().getDeclaringClass());
					
					boolean matched = false;
					for(String pN : packageNames) {
						if(nodePackageName.startsWith(pN)) {
							matched = true;
							break;
						}
					}
					if(matched) {
						eventHandlingMethods.add(node);
					}
				} else {
				    eventHandlingMethods.add(node);
				}
			}
		}
		return eventHandlingMethods;
	}
	
	public static boolean isEventHandlingMethod(CGNode node, ClassHierarchy cha) {
		String methodName = node.getMethod().getName().toString();
		IClass declaringClass = node.getMethod().getDeclaringClass();
		
		Set<String> allHandlers = getHandlerNames();
		if(!allHandlers.contains(methodName)) {
			return false;
		}
		
		//check if it is the subclass of an event listerner
		IClass el = getEventListener(cha);
		if(cha.isAssignableFrom(el, declaringClass)) {
			return true;
		}
		return false;
	}
	
	public static IClass[] listeners = null;
	
	public static IClass eventlistener = null;
	
	public static Set<String> handlerNames = null;
	
	public static IClass getEventListener(ClassHierarchy cha) {
		if(eventlistener != null) {
			return eventlistener;
		}
		//FIXME, not accurately enough
		eventlistener = WALAUtils.lookupClass(cha, "java.util.EventListener");
		return eventlistener;
	}
	
	//tested, every class is loaded
	public static IClass[] getListeners(ClassHierarchy cha) {
		if(listeners != null) {
			return listeners;
		} else {
			//listerner numbers: 15 awt + 21 swing 
			listeners = new IClass[15 + 21];
			//all listeners from AWT implements EventListener
			listeners[0] = WALAUtils.lookupClass(cha, "java.awt.event.ActionListener");
			listeners[1] = WALAUtils.lookupClass(cha, "java.awt.event.AdjustmentListener");
			listeners[2] = WALAUtils.lookupClass(cha, "java.awt.event.AWTEventListener");
			listeners[3] = WALAUtils.lookupClass(cha, "java.awt.event.ComponentListener");
			listeners[4] = WALAUtils.lookupClass(cha, "java.awt.event.ContainerListener");
			listeners[5] = WALAUtils.lookupClass(cha, "java.awt.event.FocusListener");
			listeners[6] = WALAUtils.lookupClass(cha, "java.awt.event.HierarchyBoundsListener");
			listeners[7] = WALAUtils.lookupClass(cha, "java.awt.event.HierarchyListener");
			listeners[8] = WALAUtils.lookupClass(cha, "java.awt.event.InputMethodListener");
			listeners[9] = WALAUtils.lookupClass(cha, "java.awt.event.ItemListener");
			listeners[10] = WALAUtils.lookupClass(cha, "java.awt.event.KeyListener");
			listeners[11] = WALAUtils.lookupClass(cha, "java.awt.event.MouseListener");
			listeners[12] = WALAUtils.lookupClass(cha, "java.awt.event.MouseMotionListener");
			listeners[13] = WALAUtils.lookupClass(cha, "java.awt.event.TextListener");
			listeners[14] = WALAUtils.lookupClass(cha, "java.awt.event.WindowListener");
			
			listeners[15] = WALAUtils.lookupClass(cha, "javax.swing.event.AncestorListener");
			listeners[16] = WALAUtils.lookupClass(cha, "javax.swing.event.CaretListener");
			listeners[17] = WALAUtils.lookupClass(cha, "javax.swing.event.CellEditorListener");
			listeners[18] = WALAUtils.lookupClass(cha, "javax.swing.event.ChangeListener");
			listeners[19] = WALAUtils.lookupClass(cha, "javax.swing.event.TreeWillExpandListener");
			listeners[20] = WALAUtils.lookupClass(cha, "javax.swing.event.UndoableEditListener");
			listeners[21] = WALAUtils.lookupClass(cha, "javax.swing.event.DocumentListener");
			listeners[22] = WALAUtils.lookupClass(cha, "javax.swing.event.HyperlinkListener");
			listeners[23] = WALAUtils.lookupClass(cha, "javax.swing.event.InternalFrameListener");
			listeners[24] = WALAUtils.lookupClass(cha, "javax.swing.event.ListDataListener");
			listeners[25] = WALAUtils.lookupClass(cha, "javax.swing.event.ListSelectionListener");
			listeners[26] = WALAUtils.lookupClass(cha, "javax.swing.event.MenuDragMouseListener");
			listeners[27] = WALAUtils.lookupClass(cha, "javax.swing.event.MenuKeyListener");
			listeners[28] = WALAUtils.lookupClass(cha, "javax.swing.event.MenuListener");
			listeners[29] = WALAUtils.lookupClass(cha, "javax.swing.event.MouseInputListener");
			listeners[30] = WALAUtils.lookupClass(cha, "javax.swing.event.PopupMenuListener");
			listeners[31] = WALAUtils.lookupClass(cha, "javax.swing.event.TableColumnModelListener");
			listeners[32] = WALAUtils.lookupClass(cha, "javax.swing.event.TableModelListener");
			listeners[33] = WALAUtils.lookupClass(cha, "javax.swing.event.TreeExpansionListener");
			listeners[34] = WALAUtils.lookupClass(cha, "javax.swing.event.TreeModelListener");
			listeners[35] = WALAUtils.lookupClass(cha, "javax.swing.event.TreeSelectionListener");
			
			Utils.checkNoNull(listeners);
			
			return listeners;
		}
	}
	
	public static Set<String> getHandlerNames() {
		if(handlerNames != null) {
			return handlerNames;
		} else {
			handlerNames = new HashSet<String>();
			
			//event handling method names from AWT
			handlerNames.add("actionPerformed");
			handlerNames.add("adjustmentValueChanged");
			handlerNames.add("eventDispatched");
			handlerNames.add("componentHidden");
			handlerNames.add("componentMoved");
			handlerNames.add("componentResized");
			handlerNames.add("componentShown");
			handlerNames.add("componentAdded");
			handlerNames.add("componentRemoved");
			handlerNames.add("focusGained");
			handlerNames.add("focusLost");
			handlerNames.add("ancestorMoved");
			handlerNames.add("ancestorResized");
			handlerNames.add("hierarchyChanged");
			handlerNames.add("caretPositionChanged");
			handlerNames.add("inputMethodTextChanged");
			handlerNames.add("itemStateChanged");
			handlerNames.add("mouseClicked");
			handlerNames.add("mouseEntered");
			handlerNames.add("mouseExited");
			handlerNames.add("mousePressed");
			handlerNames.add("mouseReleased");
			handlerNames.add("mouseDragged");
			handlerNames.add("mouseMoved");
			handlerNames.add("textValueChanged");
			handlerNames.add("windowActivated");
			handlerNames.add("windowClosed");
			handlerNames.add("windowClosing");
			handlerNames.add("windowDeactivated");
			handlerNames.add("windowDeiconified");
			handlerNames.add("windowIconified");
			handlerNames.add("windowOpened");

			//event handling method names from Swing
			handlerNames.add("ancestorAdded");
			handlerNames.add("ancestorMoved");
			handlerNames.add("ancestorRemoved");
			handlerNames.add("caretUpdate");
			handlerNames.add("editingCanceled");
			handlerNames.add("editingStopped");
			handlerNames.add("stateChanged");
			handlerNames.add("changedUpdate");
			handlerNames.add("insertUpdate");
			handlerNames.add("removeUpdate");
			handlerNames.add("hyperlinkUpdate");
			handlerNames.add("internalFrameActivated");
			handlerNames.add("internalFrameClosed");
			handlerNames.add("internalFrameClosing");
			handlerNames.add("internalFrameDeactivated");
			handlerNames.add("internalFrameDeiconified");
			handlerNames.add("internalFrameIconified");
			handlerNames.add("internalFrameOpened");
			handlerNames.add("contentsChanged");
			handlerNames.add("intervalAdded");
			handlerNames.add("intervalRemoved");
			handlerNames.add("valueChanged");
			handlerNames.add("menuDragMouseDragged");
			handlerNames.add("menuDragMouseEntered");
			handlerNames.add("menuDragMouseExited");
			handlerNames.add("menuDragMouseReleased");
			handlerNames.add("menuCanceled");
			handlerNames.add("menuDeselected");
			handlerNames.add("menuSelected");
			handlerNames.add("popupMenuCanceled");
			handlerNames.add("popupMenuWillBecomeInvisible");
			handlerNames.add("popupMenuWillBecomeVisible");
			handlerNames.add("columnAdded");
			handlerNames.add("columnMarginChanged");
			handlerNames.add("columnMoved");
			handlerNames.add("columnRemoved");
			handlerNames.add("columnSelectionChanged");
			handlerNames.add("tableChanged");
			handlerNames.add("treeNodesChanged");
			handlerNames.add("treeNodesInserted");
			handlerNames.add("treeNodesRemoved");
			handlerNames.add("treeStructureChanged");
			handlerNames.add("valueChanged");
			handlerNames.add("treeWillCollapse");
			handlerNames.add("treeWillExpand");
			handlerNames.add("undoableEditHappened");
			
			return handlerNames;
		}
	}
	
}