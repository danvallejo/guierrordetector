package edu.washington.cs.detector.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.ibm.wala.ipa.callgraph.CGNode;

import edu.washington.cs.detector.AnomalyCallChain;
import edu.washington.cs.detector.CallChainNode;

public class Utils {
	
	public static String translateSlashToDot(String str) {
		assert str != null;
		return str.replace('/', '.');
	}
	
	public static String translateDotToSlash(String str) {
		assert str != null;
		return str.replace('.', '/');
	}
	
	public static void checkDirExistence(String dir) {
		File f = new File(dir);
		if(!f.isDirectory()) {
			throw new RuntimeException("File: " + f + " is not a dir");
		}
		if(!f.exists()) {
			throw new RuntimeException("Dir: " + f + " does not exist");
		}
	}
	
	public static void checkFileExistence(String dir) {
		File f = new File(dir);
		if(f.isDirectory()) {
			throw new RuntimeException("File: " + f + " is  a dir");
		}
		if(!f.exists()) {
			throw new RuntimeException("File: " + f + " does not exist");
		}
	}
	
	public static <T> void checkNoNull(T[] ts) {
		for(int i = 0; i < ts.length; i++) {
			if(ts[i] == null) {
				throw new RuntimeException("The " + i + "-th element is null.");
			}
		}
	}
	
	public static void checkPathEntryExistence(String path) {
		String[] entries = path.split(Globals.pathSep);
		for(String entry : entries) {
			File f = new File(entry);
			if(!f.exists()) {
				throw new RuntimeException("The entry: " + entry + " does not exist.");
			}
		}
	}
	//find all jar files
	public static List<String> getJars(String dir) {
		List<String> files = Files.findFilesInDir(dir, null, ".jar");
		List<String> fullPaths = new LinkedList<String>();
		for(String file : files) {
			fullPaths.add(dir + Globals.fileSep + file);
		}
		//System.out.println(fullPaths);
		return fullPaths;
	}
	
	public static Collection<String> extractClassFromPluginXML(String pluginJarFile) throws IOException {
		if(!pluginJarFile.endsWith(".jar")) {
			throw new RuntimeException("The input file: " + pluginJarFile + " is not a jar file.");
		}
		String content = getPluginXMLContent(pluginJarFile);
		if(content != null) {
			return extractClasses(content);
		} else {
		    return Collections.<String>emptySet(); 
		}
	}
	
	//be aware, this can return null
	public static String getPluginXMLContent(String jarFilePath) throws IOException {
		ZipFile jarFile = new ZipFile(jarFilePath);
		ZipEntry entry = jarFile.getEntry("plugin.xml");
		if(entry == null) {
			return null;
		}
		BufferedReader in = new BufferedReader(
				new InputStreamReader(jarFile.getInputStream(entry)));
		StringBuilder sb = new StringBuilder();
		String line = in.readLine();
		while(line != null) {
		    sb.append(line);
		    sb.append(Globals.lineSep);
		    line = in.readLine();
		}
		return sb.toString();
	}
	
	public static Collection<String> extractClasses(String xmlContent) {
		final Set<String> classList = new LinkedHashSet<String>();
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();
			DefaultHandler handler = new DefaultHandler() {
				public void startElement(String uri, String localName,
						String qName, Attributes attributes) throws SAXException {
					if(attributes != null) {
					    for(int i = 0; i < attributes.getLength(); i++) {
						    if(attributes.getQName(i).equals("class")) {
							    classList.add(attributes.getValue(i));
						    }
					    }
					}
				}
			};
			byte[] bytes = xmlContent.getBytes("UTF8");
			InputStream inputStream = new ByteArrayInputStream(bytes);
			InputSource source = new InputSource(inputStream);
			saxParser.parse(source, handler);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return classList;
	}
	
	public static String conToPath(List<String> strs) {
		StringBuilder sb = new StringBuilder();
		int count = 0;
		for(String str : strs) {
			if(count != 0) {
				sb.append(Globals.pathSep);
			}
			sb.append(str);
			count++;
		}
		return sb.toString();
	}
	
	public static <T> boolean includedIn(T target, T[] array) {
		if(target == null) {
			throw new RuntimeException("target can not be null.");
		}
		for(T elem : array) {
			if(elem != null && elem.equals(target)) {
				return true;
			}
		}
		return false;
 	}
	
	public static <T> Collection<T> iterableToCollection(Iterable<T> ts) {
		Collection<T> collection = new LinkedList<T>();
		for(T t : ts) {
			collection.add(t);
		}
		return collection;
 	}
	
	//check if every element of its is included in all
	public static <T> boolean includedIn(Iterable<T> its, Iterable<T> all) {
		Collection<T> collection_its = iterableToCollection(its);
		Collection<T> collection_all = iterableToCollection(its);
		return collection_all.containsAll(collection_its);
	}
	
	/** This project-specific methods */
	public static <T> int countIterable(Iterable<T> c) {
		int count = 0;
		for(T t: c) {
			count++;
		}
		return count;
	}
	public static <T> void dumpCollection(Iterable<T> c, PrintStream out) {
		out.println(dumpCollection(c));
	}
	public static <T> void dumpCollection(Iterable<T> c, String fileName) {
		try {
			Files.writeToFile(dumpCollection(c), fileName);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	public static <T> String dumpCollection(Iterable<T> c) {
		StringBuilder sb = new StringBuilder();
		int num = 0;
		for(T t : c) {
			sb.append(t);
			sb.append(Globals.lineSep);
			num ++;
		}
		sb.append("Num in total: " + num);
		return sb.toString();
	}
	
	public static void dumpAnomalyCallChains(List<AnomalyCallChain> chains, String fileName) {
		dumpAnomalyCallChains(chains, new File(fileName));
	}
	
    public static void dumpAnomalyCallChains(List<AnomalyCallChain> chains, File file) {
		StringBuilder sb = new StringBuilder();
		
		int count = 0;
		for(AnomalyCallChain c : chains) {
			sb.append(count++ + "-th anomaly call chain");
			sb.append(Globals.lineSep);
			sb.append(c.getFullCallChainAsString());
			sb.append(Globals.lineSep);
			sb.append(Globals.lineSep);
		}
		
		try {
			Files.writeToFile(sb.toString(), file);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
    
    /**
     * The only difference is this method append each anomaly call chain to a file
     * without caching in a string buffer.
     * */
    public static void dumpLargeAnomalyCallChains(Collection<AnomalyCallChain> chains, File f) throws IOException {
		int count = 0;
		for(AnomalyCallChain chain : chains) {
			String str = count++ + "-th anomaly call chain" + Globals.lineSep;
			str = str + chain.getFullCallChainAsString() + Globals.lineSep;
			Files.writeToFile(str, f, true);
		}
    }
    
    /**
     * Remove call chain repetitions by comparing their text format
     * */
    public static List<CallChainNode> removeRedundantCallChains(Collection<CallChainNode> nodeList) {
		List<CallChainNode> uniqueNodeList = new LinkedList<CallChainNode>();
		//keep track of unique list in the string form
		Set<String> chainStrSet = new HashSet<String>();
		//iterate through each call chain node
		for(CallChainNode node : nodeList) {
			//only comparing the string format
			String chainStr = node.getChainToRootAsStr();
			if(chainStrSet.contains(chainStr)) {
				continue;
			} else {
				uniqueNodeList.add(node);
				chainStrSet.add(chainStr);
			}
		}
		return uniqueNodeList;
	}
    
    /**
     * Remove redundant call chain node
     * */
    public static List<CallChainNode> removeNodeRepetition(Collection<CallChainNode> nodes) {
		List<CallChainNode> uniqueNodeList = new LinkedList<CallChainNode>();
		Set<CGNode> uniqueNodes = new HashSet<CGNode>();
		for(CallChainNode n : nodes) {
			if(uniqueNodes.contains(n.getNode())) {
				continue;
			} else {
				uniqueNodeList.add(n);
				uniqueNodes.add(n.getNode());
			}
		}
		return uniqueNodeList;
	}
    
    public static List<AnomalyCallChain> removeRedundantAnomalyCallChains(Collection<AnomalyCallChain> chains) {
    	List<AnomalyCallChain> uniqueCallChains = new LinkedList<AnomalyCallChain>();
		Set<String> chainStr = new HashSet<String>();
		for(AnomalyCallChain c : chains) {
			if(chainStr.contains(c.getFullCallChainAsString())) {
				continue;
			} else {
				uniqueCallChains.add(c);
				chainStr.add(c.getFullCallChainAsString());
			}
		}
		return uniqueCallChains;
    }
}