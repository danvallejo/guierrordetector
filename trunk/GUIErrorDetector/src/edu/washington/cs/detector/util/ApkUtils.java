package edu.washington.cs.detector.util;

import java.io.File;

/**
 * Some utility methods to process the Apk files
 * */
public class ApkUtils {

	/**
	 * The dir containing apktool: http://code.google.com/p/android-apktool/
	 * */
	private static String apkToolDir = null;
	
	/**
	 * The dir containing tool for decompiling apk files
	 * */
	private static String decompilerDir = null;
	
	public static void setApkToolDir(String apkDir) {
		Utils.checkDirExistence(apkDir);
		apkToolDir = apkDir;
	}
	
	public static void setDecompilerDir(String dedDir) {
		Utils.checkDirExistence(dedDir);
		decompilerDir = dedDir;
	}
	
	/**
	 * Return the  dir that contains the res folder
	 * */
	public static String decryptXMFiles(String apkFile, String extractDir) {
		//first check if apkToolDir is properly set up
		if(apkToolDir == null) {
			throw new RuntimeException("Need to set up the apkTooDir first by calling: "
					+ "setApkToolDir(String).");
		}
		//check the output dir
		File f = new File(extractDir);
		if(!f.exists()) {
			f.mkdirs();
		}
		Utils.checkDirExistence(extractDir);
		
		//command to decrypt xml files: apktool d HelloWorld.apk ./HelloWorld
		String[] command = new String[] {
		    "cmd.exe",
		    "/c", 
			apkToolDir + Globals.fileSep + "apktool",
			"d",
			"-f", /*override the existing data*/
			apkFile,
			extractDir
		};
		//execute the command
		String flatCmd = "\"" + Utils.concatenate(command, " ") + "\"";
		Command.runCommand(command, ">>", true, "Executing " + flatCmd
				+ " to decrypt xml files.", true);
		return extractDir;
	}
	
	/**
	 * reset to default
	 * */
	public static void restoreToDefault() {
		apkToolDir = null;
		decompilerDir = null;
	}
}
