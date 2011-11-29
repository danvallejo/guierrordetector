package edu.washington.cs.detector.util;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

public class JarViewer {
	
	public static Collection<ZipEntry> getContents(String jarFilePath) throws ZipException, IOException {
		return getContents(new File(jarFilePath));
	}
	
    public static Collection<ZipEntry> getContents(File f) throws ZipException, IOException {
    	Collection<ZipEntry> entries = new LinkedHashSet<ZipEntry>();
    	ZipFile jarFile = new ZipFile(f);
		Enumeration<? extends ZipEntry> e = jarFile.entries();
		while(e.hasMoreElements()) {
			ZipEntry ze = e.nextElement();
			entries.add(ze);
		}
		return entries;
	}
	
    public static Collection<String> getContentsAsStr(File f) throws ZipException, IOException {
    	Collection<ZipEntry> entries = getContents(f);
    	Collection<String> strs = new LinkedHashSet<String>();
    	for(ZipEntry e : entries) {
    		strs.add(e.toString());
    	}
    	return strs;
    }
}