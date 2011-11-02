package edu.washington.cs.detector.findjars;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class FindJars {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			
			String[] jars = new FindJars()
					.findDependentJars(
							new String[] {
									"/Users/hlv/tmp/plugins/plugintest_1.0.0.201111021521.jar",
									"/Users/hlv/bin/eclipse/plugins/org.eclipse.jdt.core_3.7.1.v_B76_R37x.jar" },
							new String[] {
									"/Users/hlv/bin/eclipse/plugins/org.eclipse.debug.ui_3.7.101.v20110817_r371.jar",
									"/Users/hlv/bin/eclipse/plugins/org.eclipse.swt.cocoa.macosx.x86_64_3.7.1.v3738a.jar",
									"/Users/hlv/bin/eclipse/plugins/org.eclipse.swt_3.7.1.v3738a.jar",
									"/Users/hlv/bin/eclipse/plugins/org.eclipse.jface_3.7.0.I20110522-1430.jar",
									"/Users/hlv/bin/eclipse/plugins/org.eclipse.core.commands_3.6.0.I20110111-0800.jar",
									"/Users/hlv/bin/eclipse/plugins/org.eclipse.ui.workbench_3.7.0.I20110519-0100.jar",
									"/Users/hlv/bin/eclipse/plugins/org.eclipse.core.runtime_3.7.0.v20110110.jar",
									"/Users/hlv/bin/eclipse/plugins/org.eclipse.osgi_3.7.1.R37x_v20110808-1106.jar",
									"/Users/hlv/bin/eclipse/plugins/org.eclipse.equinox.common_3.6.0.v20110523.jar",
									"/Users/hlv/bin/eclipse/plugins/org.eclipse.core.jobs_3.5.100.v20110404.jar",
							});
			
			for (String jar : jars) {
				System.out.println(jar);
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	static Pattern REQUIRE_BUNDLE_ENTRY_LINE_PATTERN = Pattern.compile("^Require-Bundle:(.*)$");
	static Pattern ENTRY_LINE_PATTERN = Pattern.compile("^[^ ]*:.*$");
	
	static Pattern BUNDLE_SPEC_PATTERN = Pattern.compile("^(([^,\"]|\"[^\"]*\")*)(,|$)");
	static Pattern BUNDLE_SPEC_ENTRY_PATTERN = Pattern.compile("^(([^;\"]|\"[^\"]*\")*)(;|$)");
	
	static Pattern BUNDLE_MINMAX_VER_ENTRY_PATTERN = Pattern.compile("^bundle-version=\"(\\[|\\()([0-9.]*),([0-9.]*)(\\]|\\))\"$");
	static Pattern BUNDLE_MIN_VER_ENTRY_PATTERN = Pattern.compile("^bundle-version=\"([0-9.]*)\"$");
	static Pattern BUNDLE_OPTIONAL_ENTRY_PATTERN = Pattern.compile("^resolution:=optional$");
	static Pattern BUNDLE_REEXPORT_ENTRY_PATTERN = Pattern.compile("^visibility:=reexport$");

	String[] findDependentJars(String[] jas, String[] jbs) throws IOException {
		
		boolean[] flags = new boolean[jbs.length];
		Arrays.fill(flags, false);
		
		for (String filename : jas) {
			ZipFile jarFile = new ZipFile(filename);
			ZipEntry entry = jarFile.getEntry("META-INF/MANIFEST.MF");
			if (entry == null || entry.isDirectory()) {
				System.out.println("IS NULL");
				continue;
			}
			
			BufferedReader in = new BufferedReader(new InputStreamReader(jarFile.getInputStream(entry)));
			String line = null;
			
			StringBuffer buffer = new StringBuffer();
			
			boolean flag = false;
			
			while (!flag) {
				line = in.readLine();
				if (line == null) {
					break;
				}
				
				Matcher m = REQUIRE_BUNDLE_ENTRY_LINE_PATTERN.matcher(line);
				if (m.find()) {
					flag = true;
					
					buffer.append(m.group(1).trim());
					break;
				}
			}
			
			if (flag) {
				while (true) {
					line = in.readLine();
					if (line == null) {
						break;
					}
					
					Matcher m = ENTRY_LINE_PATTERN.matcher(line);
					if (m.find()) {
						break;
					}
					
					buffer.append(line.trim());
				}
			}
			
			in.close();

			ArrayList<RequiredBundleInfo> infos = parseBundles(buffer.toString());

//			for (RequiredBundleInfo info : infos) {
//				System.out.println(info.bundleName);
//
//				if (info.minVersion != null && info.maxVersion == null) {
//					System.out.println(String.format("minVersion : %s", info.minVersion));
//				} else if (info.maxVersion != null) {
//					System.out.println(String.format("minVersion (%s): %s", info.minVersionInclusive ? "inclusive" : "exclusive", info.minVersion));
//					System.out.println(String.format("maxVersion (%s): %s", info.maxVersionInclusive ? "inclusive" : "exclusive", info.maxVersion));
//				}
//				
//				if (info.optional) {
//					System.out.println("Optional");
//				}
//				
//				if (info.reexport) {
//					System.out.println("Reexport");
//				}
//				
//				System.out.println();
//			}
			
			for (RequiredBundleInfo info : infos) {
				if (info.bundleName != null) {
					for (int i = 0; i < flags.length; ++i) {
						File f = new File(jbs[i]);
						if (f.isDirectory()) {
							continue;
						}
						
						if (f.getName().startsWith(info.bundleName + "_")) {
							flags[i] = true;
						}
					}
				}
			}
			
		}
		
		ArrayList<String> result = new ArrayList<String>();
		
		for (int i = 0; i <flags.length; ++i) {
			if (flags[i]) {
				result.add(jbs[i]);
			}
		}
		
		return result.toArray(new String[]{});
	}
	
	public ArrayList<RequiredBundleInfo> parseBundles(String bundleSpecs) {
		ArrayList<RequiredBundleInfo> result = new ArrayList<RequiredBundleInfo>();
		
		while (bundleSpecs.length() > 0) {
			Matcher m = BUNDLE_SPEC_PATTERN.matcher(bundleSpecs);
			if (m.find()) {
				result.add(parseBundle(m.group(1)));
				bundleSpecs = bundleSpecs.substring(m.group(0).length());
			} else {
				throw new RuntimeException("BAD SPEC");
			}
		}
		
		return result;
	}
	
	public RequiredBundleInfo parseBundle(String bundleSpec) {
		RequiredBundleInfo info = new RequiredBundleInfo();
		
		while (bundleSpec.length() > 0) {
			Matcher m = BUNDLE_SPEC_ENTRY_PATTERN.matcher(bundleSpec);
			if (m.find()) {
				if (info.bundleName == null) {
					info.bundleName = m.group(1);
				} else {
					parseEntry(info, m.group(1));
				}
				bundleSpec = bundleSpec.substring(m.group(0).length());
			} else {
				throw new RuntimeException("BAD ENTRY");
			}
		}
		
		return info;
	}
	
	public void parseEntry(RequiredBundleInfo info, String entry) {
		Matcher m = BUNDLE_MINMAX_VER_ENTRY_PATTERN.matcher(entry);
		if (m.find()) {
			info.minVersion = m.group(2);
			info.maxVersion = m.group(3);
			info.minVersionInclusive = m.group(1).equals("[");
			info.maxVersionInclusive = m.group(4).equals("]");
			return;
		}
		
		m = BUNDLE_MIN_VER_ENTRY_PATTERN.matcher(entry);
		if (m.find()) {
			info.minVersion = m.group(1);
			info.minVersionInclusive = true;
			info.maxVersion = null;
			info.maxVersionInclusive = false;
			return;
		}
		
		m = BUNDLE_OPTIONAL_ENTRY_PATTERN.matcher(entry);
		if (m.find()) {
			info.optional = true;
			return;
		}
		
		m = BUNDLE_REEXPORT_ENTRY_PATTERN.matcher(entry);
		if (m.find()) {
			info.reexport = true;
			return;
		}
	}
}

class RequiredBundleInfo {
	public String bundleName;
	
	public String minVersion;
	public boolean minVersionInclusive;
	
	public String maxVersion;
	public boolean maxVersionInclusive;
	
	public boolean optional;
	public boolean reexport;
	
	public RequiredBundleInfo() {
	}
}
