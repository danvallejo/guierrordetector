package edu.washington.cs.detector.util;

import junit.framework.TestCase;

public class TestApkUtils extends TestCase {
	
	public void testExtractXML() {
		ApkUtils.setApkToolDir("D:\\develop-tools\\apktool");
		
		String apkFile = "D:\\develop-tools\\apktool\\androidschool.cartoon.apk";
		String extractDir = "D:\\develop-tools\\apktool\\tmp";
		
		String resultDir = ApkUtils.decryptXMFiles(apkFile, extractDir);
		
		assertEquals(resultDir, extractDir);
	}

}
