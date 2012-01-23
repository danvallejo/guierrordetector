package edu.washington.cs.detector.util;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;

import junit.framework.TestCase;

public class TestJarViewer extends TestCase {

	public void testTwoAndroidJars() throws ZipException, IOException {
		File jar1 = new File("D:\\research\\guierror\\eclipsews\\GUIErrorDetector\\exp-subjects\\android-2.2.1.jar");
		File jar2 = new File("D:\\research\\guierror\\eclipsews\\GUIErrorDetector\\exp-subjects\\original-android.jar");
		
		Collection<String> jar1Content = JarViewer.getContentsAsStr(jar1);
		Collection<String> jar2Content = JarViewer.getContentsAsStr(jar2);
		
		System.out.println("Jar1 content: " + jar1Content.size());
		System.out.println("Jar2 content: " + jar2Content.size());
		
		System.out.println("Jar2 contains Jar1: " + jar2Content.containsAll(jar1Content));
		
		jar2Content.removeAll(jar1Content);
		
		System.out.println("The number in jar2 after removing jar1: " + jar2Content.size());
		
		StringBuilder sb = new StringBuilder();
		for(String c : jar2Content) {
			sb.append(c);
			sb.append(Globals.lineSep);
		}
		Files.writeToFile(sb.toString(), "original-android-jar-diff.txt");
	}
	
	public void testAndroidJarWithSrc() throws ZipException, IOException {
		File jar1 = new File("D:\\research\\guierror\\eclipsews\\GUIErrorDetector\\exp-subjects\\original-android-src.jar");
		File jar2 = new File("D:\\research\\guierror\\eclipsews\\GUIErrorDetector\\exp-subjects\\original-android.jar");
		
		Collection<String> jar1Content = JarViewer.getContentsAsStr(jar1);
		Collection<String> jar2Content = JarViewer.getContentsAsStr(jar2);
		
		System.out.println("Jar1 content: " + jar1Content.size());
		System.out.println("Jar2 content: " + jar2Content.size());
	}
	
	//=========== ignore the following, to measure the class/method number for experiment
	public void testClassNumber() throws ZipException, IOException {
		int i = 0;
		int j = 0;
		String[] files = EclipsePluginCommons.DEPENDENT_JARS.split(Globals.pathSep);
		files = new String[] {
				"D:\\research\\guierror\\eclipsews\\GUIErrorDetector\\exp-subjects\\original-android.jar"
		};
		for (String file : files) {
			File jar2 = new File(file);
			Collection<ZipEntry> entries = JarViewer.getContents(jar2);
			for (ZipEntry entry : entries) {
				if (entry.getName().endsWith(".class")) {
					i++;
					System.out.println(entry.getName());
					String clazzName = Utils.translateSlashToDot(entry.getName());
					clazzName = clazzName.substring(0, clazzName.length() - 6);
					System.out.println(clazzName);
					
					try {
						Class c = Class.forName(clazzName);
						j = j + c.getDeclaredMethods().length;
					} catch (Throwable e) {
						continue;
					}
				}
				
			}
		}
		System.out.println("Number of class: " + i);
		System.out.println("Number of method: " + j);
	}
	
	public void testMeasureSwingMethodNumber() throws ZipException, IOException, ClassNotFoundException {
		File jar2 = new File("D:\\Java\\jdk1.6.0_27\\src.zip");
		int i = 0;
		Collection<ZipEntry> entries = JarViewer.getContents(jar2);
		for (ZipEntry entry : entries) {
			if (entry.getName().endsWith(".java") && entry.getName().indexOf("swing") != -1) {
				String name = entry.getName();
				String clazzName = Utils.translateSlashToDot(name);
				clazzName = clazzName.substring(0, clazzName.length() - 5);
				try {
				Class c = Class.forName(clazzName);
				i = i + c.getDeclaredMethods().length;
				System.out.println(clazzName);
				} catch (Exception e) {
					continue;
				}
			}
		}
		System.out.println("Number of method: " + i);
	}
	
	public void testMeasureSWTMethodNumber() throws ZipException, IOException, ClassNotFoundException {
		File jar2 = new File("D:/develop-tools/eclipse/eclipse/plugins/org.eclipse.swt.win32.win32.x86_64.source_3.6.2.v3659c.jar");
		int i = 0;
		Collection<ZipEntry> entries = JarViewer.getContents(jar2);
		for (ZipEntry entry : entries) {
			if (entry.getName().endsWith(".java")) {
				String name = entry.getName();
				String clazzName = Utils.translateSlashToDot(name);
				clazzName = clazzName.substring(0, clazzName.length() - 5);
				System.out.println(clazzName);
				try {
				Class c = Class.forName(clazzName);
				i = i + c.getDeclaredMethods().length;
				System.out.println(clazzName);
				} catch (Throwable e) {
					continue;
				}
			}
		}
		System.out.println("Number of method: " + i);
	}
}
