package edu.washington.cs.detector.util;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
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
}
