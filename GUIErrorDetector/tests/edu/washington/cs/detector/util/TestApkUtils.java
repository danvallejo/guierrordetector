package edu.washington.cs.detector.util;

import java.io.File;
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

}
