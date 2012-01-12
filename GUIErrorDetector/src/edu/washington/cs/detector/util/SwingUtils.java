package edu.washington.cs.detector.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
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
	
	private static String[] safeMethods = new String[]{
		"java.awt.Component.repaint",
		"java.awt.Container.invalidate",
		"javax.swing.JComponent.revalidate"
	};
	//java.awt.Component.repaint
	//java.awt.Container.invalidate
	//javax.swing.revalidate
	public static boolean isThreadSafeMethod(IMethod method) {
		String fullMethodName = WALAUtils.getFullMethodName(method);
		if(Utils.includedIn(fullMethodName, safeMethods)) {
			return true;
		}
		/*
		 * Methods for adding, getting, and deleting listerners are all safe
		 * getListeners(..)) ||
         *  call(* add*Listener(..)) ||
         *  call(* remove*Listener(..));
		 * */
		String methodName = method.getName().toString();
		if(methodName.equals("getListeners")
		 || (methodName.startsWith("add") && methodName.endsWith("Listener"))
		 || (methodName.startsWith("remove") && methodName.endsWith("Listener"))) {
			return true;
		}
		return false;
	}
	
//	public static boolean isEmptyMethod(IMethod method) {
//		method.
//	}

	/**
	 * This is potentially incomplete!
	 * */
	public static boolean isThreadUnsafeMethod(IMethod method, ClassHierarchy cha) {
		if (method.isAbstract()) {
			return false;
		}
		
		//the methods excluded by
		if(isThreadSafeMethod(method)) {
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
		
		//XXX FIXME need to check other components here?
		
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
	
	public static boolean isInAWTPackage(IClass clazz) {
		String packageName = WALAUtils.getJavaPackageName(clazz);
		return packageName.startsWith("java.awt.");
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
			listeners = new IClass[] {
					WALAUtils.lookupClass(cha, "javax.swing.event.InternalFrameAdapter"),
					WALAUtils.lookupClass(cha, "javax.swing.event.DocumentListener"),
					WALAUtils.lookupClass(cha, "javax.swing.event.UndoableEditListener"),
					WALAUtils.lookupClass(cha, "javax.swing.event.CaretListener"),
					WALAUtils.lookupClass(cha, "javax.swing.event.MenuListener"),
					WALAUtils.lookupClass(cha, "javax.swing.event.CellEditorListener"),
					WALAUtils.lookupClass(cha, "javax.swing.event.RowSorterListener"),
					WALAUtils.lookupClass(cha, "javax.swing.event.HyperlinkListener"),
					WALAUtils.lookupClass(cha, "javax.swing.event.TreeSelectionListener"),
					WALAUtils.lookupClass(cha, "javax.swing.event.ListDataListener"),
					WALAUtils.lookupClass(cha, "javax.swing.event.AncestorListener"),
					WALAUtils.lookupClass(cha, "javax.swing.event.MouseInputListener"),
					WALAUtils.lookupClass(cha, "javax.swing.event.ListSelectionListener"),
					WALAUtils.lookupClass(cha, "javax.swing.event.TableModelListener"),
					WALAUtils.lookupClass(cha, "javax.swing.event.ChangeListener"),
					WALAUtils.lookupClass(cha, "javax.swing.event.TreeWillExpandListener"),
					WALAUtils.lookupClass(cha, "javax.swing.event.InternalFrameListener"),
					WALAUtils.lookupClass(cha, "javax.swing.event.TreeExpansionListener"),
					WALAUtils.lookupClass(cha, "javax.swing.event.MenuDragMouseListener"),
					WALAUtils.lookupClass(cha, "javax.swing.event.MenuKeyListener"),
					WALAUtils.lookupClass(cha, "javax.swing.event.TableColumnModelListener"),
					WALAUtils.lookupClass(cha, "javax.swing.event.PopupMenuListener"),
					WALAUtils.lookupClass(cha, "javax.swing.event.TreeModelListener"),
					WALAUtils.lookupClass(cha, "javax.swing.event.MouseInputAdapter"),
					WALAUtils.lookupClass(cha, "java.awt.event.MouseMotionAdapter"),
					WALAUtils.lookupClass(cha, "java.awt.event.TextListener"),
					WALAUtils.lookupClass(cha, "java.awt.event.HierarchyBoundsListener"),
					WALAUtils.lookupClass(cha, "java.awt.event.KeyListener"),
					WALAUtils.lookupClass(cha, "java.awt.event.AWTEventListener"),
					WALAUtils.lookupClass(cha, "java.awt.event.MouseAdapter"),
					WALAUtils.lookupClass(cha, "java.awt.event.ContainerAdapter"),
					WALAUtils.lookupClass(cha, "java.awt.event.WindowFocusListener"),
					WALAUtils.lookupClass(cha, "java.awt.event.ContainerListener"),
					WALAUtils.lookupClass(cha, "java.awt.event.FocusAdapter"),
					WALAUtils.lookupClass(cha, "java.awt.event.MouseWheelListener"),
					WALAUtils.lookupClass(cha, "java.awt.event.AdjustmentListener"),
					WALAUtils.lookupClass(cha, "java.awt.event.AWTEventListenerProxy"),
					WALAUtils.lookupClass(cha, "java.awt.event.MouseMotionListener"),
					WALAUtils.lookupClass(cha, "java.awt.event.KeyAdapter"),
					WALAUtils.lookupClass(cha, "java.awt.event.WindowStateListener"),
					WALAUtils.lookupClass(cha, "java.awt.event.MouseListener"),
					WALAUtils.lookupClass(cha, "java.awt.event.HierarchyListener"),
					WALAUtils.lookupClass(cha, "java.awt.event.WindowListener"),
					WALAUtils.lookupClass(cha, "java.awt.event.WindowAdapter"),
					WALAUtils.lookupClass(cha, "java.awt.event.ItemListener"),
					WALAUtils.lookupClass(cha, "java.awt.event.FocusListener"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.basic.BasicScrollPaneUI$ViewportChangeHandler"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.basic.BasicMenuUI$MouseInputHandler"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.basic.BasicScrollPaneUI$MouseWheelHandler"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.basic.BasicTreeUI$KeyHandler"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.basic.BasicInternalFrameTitlePane$CloseAction"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.basic.BasicSplitPaneUI$KeyboardResizeToggleHandler"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.basic.BasicSliderUI$PropertyChangeHandler"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.basic.BasicInternalFrameTitlePane$MoveAction"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.basic.BasicTreeUI$FocusHandler"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.metal.MetalLabelUI"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.basic.BasicToolBarUI$DockingListener"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.basic.BasicSplitPaneUI$PropertyHandler"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.basic.BasicTreeUI$ComponentHandler"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.basic.BasicTabbedPaneUI$FocusHandler"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.metal.MetalToolBarUI$MetalRolloverListener"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.basic.BasicComboPopup$PropertyChangeHandler"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.basic.BasicRootPaneUI"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.basic.BasicTreeUI$TreeTraverseAction"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.basic.BasicComboPopup$ListSelectionHandler"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.basic.BasicComboPopup$ListDataHandler"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.basic.BasicSliderUI$FocusHandler"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.basic.BasicListUI$ListDataHandler"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.metal.MetalComboBoxEditor"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.basic.BasicComboBoxUI$ListDataHandler"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.basic.BasicFileChooserUI$DoubleClickListener"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.basic.BasicComboBoxUI$KeyHandler"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.metal.MetalFileChooserUI$DirectoryComboBoxAction"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.basic.BasicTreeUI$TreeHomeAction"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.basic.BasicDesktopPaneUI$OpenAction"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.basic.BasicInternalFrameUI$BorderListener"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.basic.BasicSliderUI$ActionScroller"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.basic.BasicSplitPaneUI$KeyboardEndHandler"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.basic.BasicTableHeaderUI$MouseInputHandler"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.basic.BasicFileChooserUI$NewFolderAction"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.basic.BasicLabelUI"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.metal.MetalFileChooserUI$SingleClickListener"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.metal.MetalToolBarUI$MetalContainerListener"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.basic.BasicDesktopPaneUI$NavigateAction"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.basic.BasicSplitPaneDivider$MouseHandler"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.basic.BasicTreeUI$SelectionModelPropertyChangeHandler"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.basic.BasicListUI$ListSelectionHandler"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.basic.BasicTreeUI$TreeExpansionHandler"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.basic.BasicScrollBarUI$ScrollListener"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.basic.BasicInternalFrameTitlePane$SizeAction"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.basic.BasicTableUI$FocusHandler"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.basic.BasicFileChooserUI$CancelSelectionAction"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.basic.BasicComboPopup$InvocationMouseMotionHandler"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.basic.BasicTreeUI$PropertyChangeHandler"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.basic.BasicComboPopup$ItemHandler"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.basic.BasicOptionPaneUI$PropertyChangeHandler"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.metal.MetalFileChooserUI$FilterComboBoxModel"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.basic.BasicInternalFrameUI$BasicInternalFrameListener"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.basic.BasicTabbedPaneUI$PropertyChangeHandler"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.metal.MetalComboBoxUI$MetalPropertyChangeListener"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.basic.BasicInternalFrameTitlePane$RestoreAction"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.basic.BasicScrollPaneUI$PropertyChangeHandler"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.basic.BasicSplitPaneUI$KeyboardHomeHandler"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.basic.BasicFileChooserUI$SelectionListener"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.basic.BasicOptionPaneUI$ButtonActionListener"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.basic.BasicSplitPaneUI$FocusHandler"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.basic.BasicComboPopup$ListMouseHandler"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.basic.BasicToolBarUI$FrameListener"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.basic.BasicColorChooserUI$PropertyHandler"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.basic.BasicListUI$MouseInputHandler"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.basic.BasicTreeUI$TreeToggleAction"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.basic.BasicTreeUI$MouseHandler"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.basic.BasicTreeUI$TreeCancelEditingAction"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.basic.BasicInternalFrameTitlePane$MaximizeAction"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.basic.BasicScrollPaneUI$VSBChangeListener"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.basic.BasicScrollBarUI$ArrowButtonListener"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.basic.BasicTableUI$MouseInputHandler"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.basic.BasicTextUI$BasicCaret"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.basic.BasicSliderUI$TrackListener"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.basic.BasicFileChooserUI$GoHomeAction"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.basic.BasicMenuItemUI$MouseInputHandler"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.basic.BasicTabbedPaneUI$MouseHandler"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.basic.BasicComboPopup$InvocationMouseHandler"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.metal.MetalSliderUI$MetalPropertyListener"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.basic.BasicTreeUI$TreeModelHandler"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.basic.BasicScrollBarUI$PropertyChangeHandler"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.basic.BasicSliderUI$ScrollListener"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.basic.BasicSplitPaneUI$KeyboardDownRightHandler"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.basic.BasicScrollPaneUI$HSBChangeListener"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.basic.BasicToolBarUI$ToolBarContListener"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.basic.BasicScrollBarUI$ModelListener"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.basic.BasicComboBoxUI$FocusHandler"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.basic.BasicFileChooserUI$ApproveSelectionAction"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.basic.BasicProgressBarUI$ChangeHandler"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.basic.BasicComboBoxUI$PropertyChangeHandler"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.basic.BasicFileChooserUI$UpdateAction"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.basic.BasicSplitPaneDivider"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.basic.BasicInternalFrameUI$GlassPaneDispatcher"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.basic.BasicListUI$FocusHandler"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.basic.BasicComboPopup$InvocationKeyHandler"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.basic.BasicTreeUI$TreePageAction"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.basic.BasicDesktopPaneUI$CloseAction"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.basic.BasicComboBoxUI$ItemHandler"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.basic.BasicButtonListener"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.basic.BasicTableUI$KeyHandler"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.basic.BasicDesktopPaneUI$MinimizeAction"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.metal.MetalComboBoxEditor$UIResource"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.basic.BasicToolBarUI$ToolBarFocusListener"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.basic.BasicSliderUI$ChangeHandler"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.basic.BasicSliderUI$ComponentHandler"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.basic.BasicTabbedPaneUI$TabSelectionHandler"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.basic.BasicTreeUI$TreeSelectionHandler"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.basic.BasicScrollBarUI$TrackListener"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.basic.BasicDesktopPaneUI$MaximizeAction"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.basic.BasicSplitPaneUI$KeyboardUpLeftHandler"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.basic.BasicComboPopup$ListMouseMotionHandler"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.basic.BasicTreeUI$MouseInputHandler"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.basic.BasicFileChooserUI$ChangeToParentDirectoryAction"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.basic.BasicComboBoxEditor"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.metal.MetalToolBarUI$MetalDockingListener"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.basic.BasicTreeUI$CellEditorHandler"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.basic.BasicInternalFrameUI$ComponentHandler"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.basic.BasicInternalFrameUI$InternalFramePropertyChangeListener"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.metal.MetalRootPaneUI"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.basic.BasicToolBarUI$PropertyListener"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.basic.BasicInternalFrameTitlePane$PropertyChangeHandler"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.basic.BasicMenuUI$ChangeHandler"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.basic.BasicInternalFrameTitlePane$IconifyAction"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.basic.BasicDesktopIconUI$MouseInputHandler"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.basic.BasicDirectoryModel"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.basic.BasicListUI$PropertyChangeHandler"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.basic.BasicComboBoxEditor$UIResource"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.basic.BasicTreeUI$TreeIncrementAction"),
					WALAUtils.lookupClass(cha, "javax.swing.plaf.synth.SynthSliderUI$SynthTrackListener"),
					WALAUtils.lookupClass(cha, "javax.swing.text.StyledEditorKit$UnderlineAction"),
					WALAUtils.lookupClass(cha, "javax.swing.text.DefaultEditorKit$InsertTabAction"),
					WALAUtils.lookupClass(cha, "javax.swing.text.DefaultCaret"),
					WALAUtils.lookupClass(cha, "javax.swing.text.StyledEditorKit$ItalicAction"),
					WALAUtils.lookupClass(cha, "javax.swing.text.DefaultEditorKit$InsertContentAction"),
					WALAUtils.lookupClass(cha, "javax.swing.text.html.HTMLEditorKit$HTMLTextAction"),
					WALAUtils.lookupClass(cha, "javax.swing.text.StyledEditorKit$FontSizeAction"),
					WALAUtils.lookupClass(cha, "javax.swing.text.DefaultEditorKit$CopyAction"),
					WALAUtils.lookupClass(cha, "javax.swing.text.JTextComponent$AccessibleJTextComponent"),
					WALAUtils.lookupClass(cha, "javax.swing.text.html.HTMLEditorKit$InsertHTMLTextAction"),
					WALAUtils.lookupClass(cha, "javax.swing.text.DefaultEditorKit$BeepAction"),
					WALAUtils.lookupClass(cha, "javax.swing.text.DefaultEditorKit$CutAction"),
					WALAUtils.lookupClass(cha, "javax.swing.text.html.HTMLEditorKit$LinkController"),
					WALAUtils.lookupClass(cha, "javax.swing.text.TextAction"),
					WALAUtils.lookupClass(cha, "javax.swing.text.html.FormView"),
					WALAUtils.lookupClass(cha, "javax.swing.text.StyledEditorKit$FontFamilyAction"),
					WALAUtils.lookupClass(cha, "javax.swing.text.StyledEditorKit$StyledTextAction"),
					WALAUtils.lookupClass(cha, "javax.swing.text.DefaultEditorKit$InsertBreakAction"),
					WALAUtils.lookupClass(cha, "javax.swing.text.StyledEditorKit$AlignmentAction"),
					WALAUtils.lookupClass(cha, "javax.swing.text.StyledEditorKit$BoldAction"),
					WALAUtils.lookupClass(cha, "javax.swing.text.DefaultEditorKit$DefaultKeyTypedAction"),
					WALAUtils.lookupClass(cha, "javax.swing.text.DefaultEditorKit$PasteAction"),
					WALAUtils.lookupClass(cha, "javax.swing.text.StyledEditorKit$ForegroundAction"),
					WALAUtils.lookupClass(cha, "javax.swing.text.html.FormView$MouseEventListener"),
					WALAUtils.lookupClass(cha, "javax.swing.tree.DefaultTreeCellEditor"),
					WALAUtils.lookupClass(cha, "javax.swing.table.DefaultTableColumnModel"),
					WALAUtils.lookupClass(cha, "javax.swing.table.JTableHeader"),
					WALAUtils.lookupClass(cha, "javax.swing.undo.UndoManager"),
					WALAUtils.lookupClass(cha, "java.awt.TextField$AccessibleAWTTextField"),
					WALAUtils.lookupClass(cha, "java.awt.TextArea$AccessibleAWTTextArea"),
					WALAUtils.lookupClass(cha, "java.awt.Checkbox$AccessibleAWTCheckbox"),
					WALAUtils.lookupClass(cha, "java.awt.List$AccessibleAWTList"),
					WALAUtils.lookupClass(cha, "java.awt.Container$AccessibleAWTContainer$AccessibleContainerHandler"),
					WALAUtils.lookupClass(cha, "java.awt.AWTEventMulticaster"),
					WALAUtils.lookupClass(cha, "java.awt.Component$AccessibleAWTComponent$AccessibleAWTComponentHandler"),
					WALAUtils.lookupClass(cha, "java.awt.TextComponent$AccessibleAWTTextComponent"),
					WALAUtils.lookupClass(cha, "java.awt.datatransfer.FlavorListener"),
					WALAUtils.lookupClass(cha, "java.awt.Component$AccessibleAWTComponent$AccessibleAWTFocusHandler"),
					WALAUtils.lookupClass(cha, "java.awt.event.ActionListener"),
					WALAUtils.lookupClass(cha, "java.awt.event.InputMethodListener"),
					WALAUtils.lookupClass(cha, "java.awt.event.HierarchyBoundsAdapter"),
					WALAUtils.lookupClass(cha, "java.awt.event.ComponentAdapter"),
					WALAUtils.lookupClass(cha, "java.awt.event.ComponentListener"),
					WALAUtils.lookupClass(cha, "java.awt.dnd.DropTargetAdapter"),
					WALAUtils.lookupClass(cha, "java.awt.dnd.DropTarget"),
					WALAUtils.lookupClass(cha, "java.awt.dnd.DragSourceListener"),
					WALAUtils.lookupClass(cha, "java.awt.dnd.DropTargetListener"),
					WALAUtils.lookupClass(cha, "java.awt.dnd.DragSourceMotionListener"),
					WALAUtils.lookupClass(cha, "java.awt.dnd.DropTarget$DropTargetAutoScroller"),
					WALAUtils.lookupClass(cha, "java.awt.dnd.DragSourceAdapter"),
					WALAUtils.lookupClass(cha, "java.awt.dnd.MouseDragGestureRecognizer"),
					WALAUtils.lookupClass(cha, "java.awt.dnd.DragGestureListener"),
					WALAUtils.lookupClass(cha, "java.awt.dnd.DragSourceContext")	
			};
			
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
			handlerNames.add("mouseClicked");
			handlerNames.add("windowIconified");
			handlerNames.add("menuCanceled");
			handlerNames.add("hyperlinkUpdate");
			handlerNames.add("windowDeiconified");
			handlerNames.add("dragEnter");
			handlerNames.add("intervalAdded");
			handlerNames.add("internalFrameDeactivated");
			handlerNames.add("mouseWheelMoved");
			handlerNames.add("undoableEditHappened");
			handlerNames.add("keyPressed");
			handlerNames.add("treeExpanded");
			handlerNames.add("inputMethodTextChanged");
			handlerNames.add("treeCollapsed");
			handlerNames.add("focusLost");
			handlerNames.add("menuDragMouseDragged");
			handlerNames.add("menuDragMouseEntered");
			handlerNames.add("componentAdded");
			handlerNames.add("keyReleased");
			handlerNames.add("menuDeselected");
			handlerNames.add("treeWillCollapse");
			handlerNames.add("componentResized");
			handlerNames.add("mouseReleased");
			handlerNames.add("keyTyped");
			handlerNames.add("valueChanged");
			handlerNames.add("treeWillExpand");
			handlerNames.add("windowClosed");
			handlerNames.add("dragGestureRecognized");
			handlerNames.add("dragDropEnd");
			handlerNames.add("menuKeyReleased");
			handlerNames.add("internalFrameOpened");
			handlerNames.add("contentsChanged");
			handlerNames.add("windowActivated");
			handlerNames.add("internalFrameClosing");
			handlerNames.add("menuDragMouseReleased");
			handlerNames.add("focusGained");
			handlerNames.add("columnSelectionChanged");
			handlerNames.add("windowClosing");
			handlerNames.add("caretUpdate");
			handlerNames.add("editingCanceled");
			handlerNames.add("treeNodesInserted");
			handlerNames.add("adjustmentValueChanged");
			handlerNames.add("tableChanged");
			handlerNames.add("popupMenuWillBecomeVisible");
			handlerNames.add("actionPerformed");
			handlerNames.add("dragOver");
			handlerNames.add("windowStateChanged");
			handlerNames.add("ancestorRemoved");
			handlerNames.add("columnRemoved");
			handlerNames.add("internalFrameClosed");
			handlerNames.add("componentMoved");
			handlerNames.add("editingStopped");
			handlerNames.add("mouseDragged");
			handlerNames.add("windowGainedFocus");
			handlerNames.add("popupMenuWillBecomeInvisible");
			handlerNames.add("dragMouseMoved");
			handlerNames.add("mousePressed");
			handlerNames.add("drop");
			handlerNames.add("ancestorMoved");
			handlerNames.add("treeNodesRemoved");
			handlerNames.add("windowLostFocus");
			handlerNames.add("componentHidden");
			handlerNames.add("menuKeyPressed");
			handlerNames.add("internalFrameActivated");
			handlerNames.add("menuKeyTyped");
			handlerNames.add("textValueChanged");
			handlerNames.add("internalFrameIconified");
			handlerNames.add("componentRemoved");
			handlerNames.add("itemStateChanged");
			handlerNames.add("stateChanged");
			handlerNames.add("dragExit");
			handlerNames.add("ancestorResized");
			handlerNames.add("mouseMoved");
			handlerNames.add("popupMenuCanceled");
			handlerNames.add("menuSelected");
			handlerNames.add("componentShown");
			handlerNames.add("ancestorAdded");
			handlerNames.add("dropActionChanged");
			handlerNames.add("mouseExited");
			handlerNames.add("columnMarginChanged");
			handlerNames.add("menuDragMouseExited");
			handlerNames.add("windowOpened");
			handlerNames.add("changedUpdate");
			handlerNames.add("mouseEntered");
			handlerNames.add("windowDeactivated");
			handlerNames.add("sorterChanged");
			handlerNames.add("internalFrameDeiconified");
			handlerNames.add("treeStructureChanged");
			handlerNames.add("eventDispatched");
			handlerNames.add("flavorsChanged");
			handlerNames.add("columnMoved");
			handlerNames.add("removeUpdate");
			handlerNames.add("insertUpdate");
			handlerNames.add("columnAdded");
			handlerNames.add("hierarchyChanged");
			handlerNames.add("caretPositionChanged");
			handlerNames.add("treeNodesChanged");
			handlerNames.add("intervalRemoved");
			
			return handlerNames;
		}
	}
	
	/**
	 * Get all listeners
	 * */
	public static Collection<String> getAllPublicSwingAWTListeners(ClassHierarchy cha) {
		Collection<String> listeners = new LinkedHashSet<String>();
		
		IClass eventListener = getEventListener(cha);
		for(IClass c : cha) {
			if(c.isPublic() && cha.isAssignableFrom(eventListener, c) && (isInSwingPackage(c) || isInAWTPackage(c))) {
				listeners.add(WALAUtils.getJavaFullClassName(c));
			}
		}
		
		return listeners;
	}
	
}