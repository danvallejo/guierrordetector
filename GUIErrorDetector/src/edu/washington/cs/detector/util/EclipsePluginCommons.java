package edu.washington.cs.detector.util;

import java.util.List;

public class EclipsePluginCommons {
	
	public static String PLUGIN_DIR = //PropertyReader.createInstance("./src/detector.properties").getProperty("eclipse.plugin.dir"); 
		"D:\\develop-tools\\eclipse\\eclipse\\plugins\\";

	public static String DEPENDENT_JARS = getAllDependentJars("./src/eclipse_jars.txt");
	
	public static void resetPluginDir(String plugindir) {
		PLUGIN_DIR = plugindir;
	}
	
	public static void resetDependentJars(String filePath) {
		DEPENDENT_JARS = getAllDependentJars(filePath);
	}
	
//	private static String OLD_DEPENDENT_JARS =
//		PLUGIN_DIR + "org.eclipse.ui_3.6.2.M20110203-1100.jar" + Globals.pathSep +
//		PLUGIN_DIR + "org.eclipse.swt_3.6.2.v3659c.jar" + Globals.pathSep +
//		PLUGIN_DIR + "org.eclipse.swt.win32.win32.x86_64_3.6.2.v3659c.jar" + Globals.pathSep +
//		PLUGIN_DIR + "org.eclipse.jface_3.6.2.M20110210-1200.jar" + Globals.pathSep +
//		PLUGIN_DIR + "org.eclipse.core.commands_3.6.0.I20100512-1500.jar" + Globals.pathSep +
//		PLUGIN_DIR + "org.eclipse.ui.workbench_3.6.2.M20110210-1200.jar" + Globals.pathSep +
//		PLUGIN_DIR + "org.eclipse.core.runtime_3.6.0.v20100505.jar" + Globals.pathSep +
//		PLUGIN_DIR + "org.eclipse.osgi_3.6.2.R36x_v20110210.jar" + Globals.pathSep +
//		PLUGIN_DIR + "org.eclipse.equinox.common_3.6.0.v20100503.jar" + Globals.pathSep +
//		PLUGIN_DIR + "org.eclipse.core.jobs_3.5.1.R36x_v20100824.jar" + Globals.pathSep +
//		PLUGIN_DIR + "\\org.eclipse.core.runtime.compatibility.registry_3.3.0.v20100520\\runtime_registry_compatibility.jar" + Globals.pathSep +
//		PLUGIN_DIR + "org.eclipse.equinox.registry_3.5.0.v20100503.jar" + Globals.pathSep +
//		PLUGIN_DIR + "org.eclipse.equinox.preferences_3.3.0.v20100503.jar" + Globals.pathSep +
//		PLUGIN_DIR + "org.eclipse.core.contenttype_3.4.100.v20100505-1235.jar" + Globals.pathSep +
//		PLUGIN_DIR + "org.eclipse.equinox.app_1.3.1.R36x_v20100803.jar" + Globals.pathSep +
//		PLUGIN_DIR + "org.eclipse.equinox.common_3.6.0.v20100503.jar";

	private static String getAllDependentJars(String fileName) {
		List<String> jarNames = Files.readWholeNoExp(fileName);
		StringBuilder sb = new StringBuilder();
		int count = 0;
		for(String jarName : jarNames) {
			if(count != 0) {
				sb.append(Globals.pathSep);
			}
			count++;
			sb.append(PLUGIN_DIR);
			sb.append(jarName);
		}
		return sb.toString();
	}
	
//	public static void main(String[] args) {
//		String path = getAllDependentJars();
//		if(!path.equals(OLD_DEPENDENT_JARS)) {
//			throw new RuntimeException();
//		}
//	}
	
}