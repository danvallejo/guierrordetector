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
	
	/** be aware it takes a long time */
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
	
	/**be aware it will takes days of time
	 * and only works for linux, and mac
	 * @throws FileNotFoundException 
	 * */
	public void testDecompileAll1100Apps() throws FileNotFoundException {
		String systemName = System.getProperty("os.name");
		if (systemName.indexOf("Windows") != -1) {
			System.err.println("Can not execute this unit test on: "
					+ systemName);
			return;
		}
		// ded can only be executed in linux or mac
		Log.logConfig("./log_decompile.txt");
		/**
		 * Modify the following according to your environment
		 * */
		String orig_dir = "/scratch/android-apps/market_20101102/";
		String target_dir = "/scratch/tools/ded/output";
		String jasJar = "/scratch/tools/ded/jasminclasses-2.4.0.jar";
		String dedpath = "/scratch/tools/ded/ded-0.7.1";

		List<File> list = Files.getFileListing(new File(orig_dir));

		for (int i = 0; i < list.size(); i++) {
			File f = list.get(i);
			if (f.getName().endsWith(".apk")) {
				System.out.println("--------------- process number: " + i);
				// relative path
				String path = f.getParent().substring(orig_dir.length());
				String newDir = target_dir + Globals.fileSep + path + Globals.fileSep + f.getName();
				
				System.out.println(f);
				System.out.println("   dir: " + path + ",  file names: "
						+ f.getName());
				System.out.println("   new dir: " + newDir);

				File newDirF = new File(newDir);
				if (!newDirF.exists()) {
					newDirF.mkdirs();
				}

				// do the decompilation
				String[] command = new String[] {dedpath, "-d", newDir, "-j",
						jasJar, f.getAbsolutePath() };
				try {
					Command.runCommand(command, ">>", true, "Decompiling: "
							+ newDir, true);
				} catch (Throwable e) {
					Log.logln("error in: " + f.getAbsolutePath());
					Log.logln("   error type: " + e);
				}

			}
		}
		System.out.println("total num processed: " + list.size());
	}
}
