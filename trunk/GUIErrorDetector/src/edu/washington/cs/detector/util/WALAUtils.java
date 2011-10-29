package edu.washington.cs.detector.util;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.core.tests.callGraph.CallGraphTestUtil;
import com.ibm.wala.ipa.callgraph.AnalysisCache;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.impl.Util;
import com.ibm.wala.ipa.callgraph.propagation.LocalPointerKey;
import com.ibm.wala.ipa.callgraph.propagation.SSAPropagationCallGraphBuilder;
import com.ibm.wala.ipa.callgraph.propagation.cfa.nCFABuilder;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.TypeName;
import com.ibm.wala.util.WalaException;
import com.ibm.wala.util.collections.CollectionFilter;
import com.ibm.wala.util.collections.Filter;
import com.ibm.wala.util.config.AnalysisScopeReader;
import com.ibm.wala.util.graph.Graph;
import com.ibm.wala.util.graph.GraphSlicer;
import com.ibm.wala.util.io.FileProvider;

import edu.washington.cs.detector.SWTAppUIErrorMain;

public class WALAUtils {

	public static SSAPropagationCallGraphBuilder makeOneCFABuilder(
			AnalysisOptions options, AnalysisCache cache, IClassHierarchy cha,
			AnalysisScope scope) {
		return makeCFABuilder(1, options, cache, cha, scope);
	}
	
	public static SSAPropagationCallGraphBuilder makeCFABuilder(int n,
			AnalysisOptions options, AnalysisCache cache, IClassHierarchy cha,
			AnalysisScope scope) {

		if (options == null) {
			throw new IllegalArgumentException("options is null");
		}
		Util.addDefaultSelectors(options, cha);
		Util.addDefaultBypassLogic(options, scope, Util.class.getClassLoader(), cha);

		return new nCFABuilder(n, cha, options, cache, null, null);
	}
	
	public static void viewCallGraph(Graph<CGNode> g) {
		 StringBuilder sb = new StringBuilder();
		    for(CGNode node : g) {
		    	{
		    	   sb.append("node: " + node);
		    	   sb.append(Globals.lineSep);
		    	   Iterator<CGNode> cgit = g.getSuccNodes(node);
		    	   while(cgit.hasNext()) {
		    		   sb.append("  calls: " + cgit.next());
		    		   sb.append(Globals.lineSep);
		    	   }
		    	}
		    }
		    Log.log(sb.toString());
	}
	

	  public static Graph<CGNode> pruneForAppLoader(CallGraph g) throws WalaException {
	    return pruneGraph(g, new ApplicationLoaderFilter());
	  }
	  
	  public static <T> Graph<T> pruneGraph(Graph<T> g, Filter<T> f) throws WalaException {
		    Collection<T> slice = GraphSlicer.slice(g, f);
		    return GraphSlicer.prune(g, new CollectionFilter<T>(slice));
	  }
	  
	  private static class ApplicationLoaderFilter implements Filter<CGNode> {

	    public boolean accepts(CGNode o) {
	      if (o instanceof CGNode) {
	        CGNode n = (CGNode) o;
//	        System.out.println("processing: " + n.getMethod().getDeclaringClass());
	        return n.getMethod().getDeclaringClass().getClassLoader().getReference().equals(ClassLoaderReference.Application);
	      } else if (o instanceof LocalPointerKey) {
	        LocalPointerKey l = (LocalPointerKey) o;
	        return accepts(l.getNode());
	      } else {
	        return false;
	      }
	    }
	  }
		
		//return like a.b.c.d
		public static String getJavaFullClassName(IClass clazz) {
			TypeName tn = clazz.getName();
			String packageName = (tn.getPackage() == null ? "" : tn.getPackage().toString() + ".");
			String clazzName = tn.getClassName().toString();
			return Utils.translateSlashToDot(packageName) + clazzName;
		}
		
		//dump all classes
		public static void dumpClasses(ClassHierarchy cha, String fileName) {
			StringBuilder sb = new StringBuilder();
			int count = 0;
			for(IClass c : cha) {
				sb.append(c);
				sb.append(Globals.lineSep);
				count++;
			}
			sb.append("Number in total: " + count);
			try {
				Files.writeToFile(sb.toString(), fileName);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		

	    //check all class in a given jar are all loaded by Wala
	    public static int getUnloadedClassNum(ClassHierarchy cha, String jarFile) throws IOException {
	    	assert (jarFile != null && jarFile.endsWith(".jar"));
	    	//assert cha != null;
	    	
	    	Set<String> classInJar = new HashSet<String>();
		    JarFile file = new JarFile(new File(jarFile));
		    for (Enumeration<JarEntry> e = file.entries(); e.hasMoreElements();) {
		        ZipEntry Z = (ZipEntry) e.nextElement();
		        String entryName = Z.toString();
		        if(entryName.endsWith(".class")) {
		            String classFileName = Utils.translateSlashToDot(entryName);
		            String className = classFileName.substring(0, classFileName.length() - ".class".length());
		            classInJar.add(className);
		        }
		    }
		    
		    //all loaded class
		    Set<String> loadedClasses = new HashSet<String>();
		    for(IClass c : cha) {
		    	loadedClasses.add(iclassToClassName(c));
		    }
		    
//		    System.out.println("no in jar: " + classInJar.size());
//		    System.out.println("no of class loaded: " + loadedClasses.size());
		    int notloaded = 0;
		    for(String cj : classInJar) {
		    	if(!loadedClasses.contains(cj)) {
		    		System.out.println("not include: " + cj);
		    		notloaded ++;
		    	}
		    }
	        return notloaded;
	    }
	    
	    public static String iclassToClassName(IClass c) {
	    	TypeName tn = c.getName();
	    	String packageName = Utils.translateSlashToDot(tn.getPackage() == null ? "" : tn.getPackage().toString() + ".");
	    	String className = tn.getClassName().toString();
	    	return packageName + className;
	    }
}