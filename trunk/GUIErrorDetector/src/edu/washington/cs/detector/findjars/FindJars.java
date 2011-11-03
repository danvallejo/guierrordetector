package edu.washington.cs.detector.findjars;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class FindJars {

	static Pattern ENTRY_LINE_PATTERN = Pattern.compile("^[^ ]*:.*$");
	static Pattern REQUIRE_BUNDLE_ENTRY_LINE_PATTERN = Pattern.compile("^(?:Require-Bundle|Import-Package|DynamicImport-Package):(.*)$");
	static Pattern BUNDLE_SYMBOLIC_NAME_LINE_PATTERN = Pattern.compile("^Bundle-SymbolicName: ([^ ;]*)(;.*)?$");
	static Pattern BUNDLE_VERSION_LINE_PATTERN = Pattern.compile("^Bundle-Version: (.*)$");
	
	static Pattern BUNDLE_SPEC_PATTERN = Pattern.compile("^(([^,\"]|\"[^\"]*\")*)(,|$)");
	static Pattern BUNDLE_SPEC_ENTRY_PATTERN = Pattern.compile("^(([^;\"]|\"[^\"]*\")*)(;|$)");
	
	static Pattern BUNDLE_MINMAX_VER_ENTRY_PATTERN = Pattern.compile("^bundle-version=\"(\\[|\\()([0-9.]*),([0-9.]*)(\\]|\\))\"$");
	static Pattern BUNDLE_MIN_VER_ENTRY_PATTERN = Pattern.compile("^bundle-version=\"([0-9.]*)\"$");
	static Pattern BUNDLE_OPTIONAL_ENTRY_PATTERN = Pattern.compile("^resolution:=optional$");
	static Pattern BUNDLE_REEXPORT_ENTRY_PATTERN = Pattern.compile("^visibility:=reexport$");

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
	
	String[] findDependentJars(String[] jas, String[] jbs) throws IOException {
		
		ArrayList<String> result = new ArrayList<String>();
		
		BundleInfo[] bas = new BundleInfo[jas.length];
		BundleInfo[] bbs = new BundleInfo[jbs.length];
		
		for (int i = 0; i < jas.length; ++i) {
			bas[i] = getBundleInfo(jas[i]);
		}
		
		for (int i = 0; i < jbs.length; ++i) {
			bbs[i] = getBundleInfo(jbs[i]);
		}
		
		boolean[] flags = new boolean[bbs.length];
		Arrays.fill(flags, false);
		
		LinkedList<BundleInfo> jcs = new LinkedList<BundleInfo>(Arrays.asList(bas));
		
		while (!jcs.isEmpty()) {
			BundleInfo bundleInfo = jcs.remove();

			if (bundleInfo.requiredBundles == null) {
				continue;
			}
			
			for (RequiredBundleInfo info : bundleInfo.requiredBundles) {
				
				if (info.symbolicName != null) {
					for (int i = 0; i < bbs.length; ++i) {
						if (flags[i]) {
							continue;
						}
						
						if (bbs[i].symbolicName.equals(info.symbolicName)) {
							
							// TODO check version
							
							flags[i] = true;
							
							jcs.add(bbs[i]);
							result.add(jbs[i]);
							
							break;
						}
					}
				}
			}
		}

		return result.toArray(new String[]{});
	}
	
	public BundleInfo getBundleInfo(String path) {
		try {
			ZipFile jarFile = new ZipFile(path);
		
			ZipEntry entry = jarFile.getEntry("META-INF/MANIFEST.MF");
			if (entry == null || entry.isDirectory()) {
				System.err.println("MANIFEST.MF not found!");
				return null;
			}
		
			BundleInfo bundleInfo = new BundleInfo();

			BufferedReader in = new BufferedReader(
					new InputStreamReader(jarFile.getInputStream(entry)));
			
			String line = null;
			String nextLine = in.readLine();
			
			ArrayList<RequiredBundleInfo> infos = new ArrayList<RequiredBundleInfo>();
			
			while (true) {
				
				line = nextLine;
				nextLine = in.readLine();
				
				if (line == null) {
					break;
				}
				
				Matcher m = REQUIRE_BUNDLE_ENTRY_LINE_PATTERN.matcher(line);
				if (m.find()) {
					StringBuffer buffer = new StringBuffer();
					buffer.append(m.group(1).trim());
					
					while (nextLine != null) {
						m = ENTRY_LINE_PATTERN.matcher(nextLine);
						if (m.find()) {
							break;
						}
						
						line = nextLine;
						nextLine = in.readLine();
						buffer.append(line.trim());
					}
					
					infos.addAll(parseBundles(buffer.toString()));
					
					continue;
				}
				
				m = BUNDLE_SYMBOLIC_NAME_LINE_PATTERN.matcher(line);
				if (m.find()) {
					bundleInfo.symbolicName = m.group(1);
					continue;
				}
				
				m = BUNDLE_VERSION_LINE_PATTERN.matcher(line);
				if (m.find()) {
					bundleInfo.version = m.group(1);
					continue;
				}
			}
			
			in.close();
			
			bundleInfo.requiredBundles = infos.toArray(new RequiredBundleInfo[]{});
			
//			System.out.println(path + " : " + bundleInfo.symbolicName);
//			System.out.println(Arrays.toString(bundleInfo.requiredBundles));
			
			return bundleInfo;
	
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
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
				if (info.symbolicName == null) {
					info.symbolicName = m.group(1);
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

class BundleInfo {
	public String symbolicName;
	public String version;
	
	public RequiredBundleInfo[] requiredBundles;
}

class RequiredBundleInfo {
	public String symbolicName;
	
	public String minVersion;
	public boolean minVersionInclusive;
	
	public String maxVersion;
	public boolean maxVersionInclusive;
	
	public boolean optional;
	public boolean reexport;
	
	public RequiredBundleInfo() {
	}
	
	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		
		buffer.append(symbolicName);

		if (minVersion != null && maxVersion == null) {
			buffer.append(String.format(";minVersion : %s", minVersion));
		} else if (maxVersion != null) {
			buffer.append(String.format(";minVersion (%s): %s", minVersionInclusive ? "inclusive" : "exclusive", minVersion));
			buffer.append(String.format(";maxVersion (%s): %s", maxVersionInclusive ? "inclusive" : "exclusive", maxVersion));
		}
		
		if (optional) {
			buffer.append(";optional");
		}
		
		if (reexport) {
			buffer.append(";reexport");
		}
		
		return buffer.toString();
	}
}
