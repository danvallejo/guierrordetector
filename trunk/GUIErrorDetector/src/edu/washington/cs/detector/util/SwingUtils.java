package edu.washington.cs.detector.util;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.cha.ClassHierarchy;

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
}