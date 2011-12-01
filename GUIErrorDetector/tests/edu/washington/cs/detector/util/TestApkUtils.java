package edu.washington.cs.detector.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.util.Collection;
import java.util.List;

import junit.framework.TestCase;

public class TestApkUtils extends TestCase {
	
	public void testExtractXML() throws IOException {
		ApkUtils.setApkToolDir("D:\\develop-tools\\apktool");
		
		String apkFile = "D:\\develop-tools\\apktool\\androidschool.cartoon.apk";
		String extractDir = "D:\\develop-tools\\apktool\\tmp";
		
		String resultDir = ApkUtils.decryptXMFiles(apkFile, extractDir);
		assertEquals(resultDir, extractDir);
		
		List<Reader> readers = AndroidUtils.getAllLayoutXMLFromDir(new File(extractDir));
		Collection<String> uis = AndroidUtils.extractAndroidUIs(readers);
		
		assertEquals(9, uis.size());
		
		ApkUtils.restoreToDefault();
	}
	
	//be aware it takes a long time
	public void testExtractXMLForAll_1100_Apps() throws FileNotFoundException {
		Log.logConfig("./1100_app_xml.txt");
		ApkUtils.setApkToolDir("D:\\develop-tools\\apktool");
		
		String targetDir = "D:\\develop-tools\\apktool\\all_1100_apps";
		String sourceDir = "E:\\market_20101102";
		
		List<File> allFiles = Files.getFileListing(new File(sourceDir));
		int i = 0;
		for(File file : allFiles) {
			if(file.getName().endsWith(".apk")) {
				i++;
				//process the apk file
				String extractDir = targetDir
				    + file.getParent().substring(sourceDir.length())
				    + Globals.fileSep + file.getName();
				File newDir = new File(extractDir);
				if(!newDir.exists()) {
					newDir.mkdirs();
				}
				Utils.checkDirExistence(newDir.getAbsolutePath());
				System.out.println("Processing: " + extractDir);
				try {
				  String resultDir = ApkUtils.decryptXMFiles(file.getAbsolutePath(), extractDir);
				  assertEquals(resultDir, extractDir);
				} catch (Throwable e) {
					Log.logln("error in processing: " + extractDir);
					Log.logln("   get exception: " + e);
				}
			}
		}
		System.out.println("Number of files processed: " + i);
		
		assertEquals(1100, i);
		
		ApkUtils.restoreToDefault();
	}

}
