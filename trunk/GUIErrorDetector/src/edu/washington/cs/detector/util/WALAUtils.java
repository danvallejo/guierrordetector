package edu.washington.cs.detector.util;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
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
import com.ibm.wala.ssa.SSAInstruction;
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
	
//	public static void viewCallGraph(Graph<CGNode> g, boolean flag) {
//		if(flag) {
//			logCallGraph(g);
//		}
//	}
	
	public static void logCallGraph(Graph<CGNode> g, boolean DEBUG) {
		if(DEBUG) {
			logCallGraph(g);
		}
	}
	
	public static void logCallGraph(Graph<CGNode> g) {
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
	
	  //FIXME buyer aware
	  public static Graph<CGNode> copy(Graph<CGNode> g) throws WalaException {
		  return pruneGraph(g, new AcceptAllFilter());
	  }
	  private static class AcceptAllFilter implements Filter<CGNode> {
			public boolean accepts(CGNode o) {
				return true;
			}
		}

	public static Graph<CGNode> pruneForAppLoader(CallGraph g)
			throws WalaException {
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
	  
	  //give method full name like a.b.c.ClassName.method, return the CGNode
	  public static Collection<CGNode> lookupCGNode(Graph<CGNode> cg, String fullName) {
		  Collection<CGNode> nodes = new LinkedHashSet<CGNode>();
		  
		  for(CGNode node : cg) {
			  String fullMethodName = WALAUtils.getFullMethodName(node.getMethod());
			  if(fullName.equals(fullMethodName)) {
				  nodes.add(node);
			  }
		  }
		  
		  return nodes;
	  }
	  
	  //given class name like a.b.c.d
	  public static IClass lookupClass(ClassHierarchy cha, String classFullName) {
		  for(IClass c : cha) {
				String fullName = WALAUtils.getJavaFullClassName(c);
				if(fullName.equals(classFullName)) {
					return c;
				}
			}
			return null;
	  }
	  
	  //return a.b.c.d.MethodName
	  public static String getFullMethodName(IMethod method) {
		  String className = getJavaFullClassName(method.getDeclaringClass());
		  return className + "." + method.getName().toString();
	  }
		
		//return like a.b.c.d
		public static String getJavaFullClassName(IClass clazz) {
			TypeName tn = clazz.getName();
			String packageName = (tn.getPackage() == null ? "" : tn.getPackage().toString() + ".");
			String clazzName = tn.getClassName().toString();
			return Utils.translateSlashToDot(packageName) + clazzName;
		}
		
		//return like a.b.c  or "" for default package
		public static String getJavaPackageName(IClass clazz) {
			TypeName tn = clazz.getName();
			String packageName = tn.getPackage() == null ? "" : tn.getPackage().toString();
			return Utils.translateSlashToDot(packageName);
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
		
		//also remove repetition
		public static Collection<String> convertIClassToStrings(Collection<IClass> coll) {
			Collection<String> strs = new LinkedHashSet<String>();
			for(IClass ic : coll) {
				strs.add(WALAUtils.getJavaFullClassName(ic));
			}
			return strs;
		}
		
		public static Set<String> getUnloadedClasses(ClassHierarchy cha, Collection<String> jars) throws IOException {
			Set<String> unloadedClasses = new LinkedHashSet<String>();
			for(String jarFile : jars) {
				unloadedClasses.addAll(getUnloadedClasses(cha, jarFile));
			}
			return unloadedClasses;
		}
		
		public static Set<String> getUnloadedClasses(ClassHierarchy cha, String jarFile) throws IOException {
			assert (jarFile != null && jarFile.endsWith(".jar"));
	    	
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
		    Set<String> unloadedClasses = new HashSet<String>();
		    for(String cj : classInJar) {
		    	if(!loadedClasses.contains(cj)) {
		    		unloadedClasses.add(cj);
		    	}
		    }
	        return unloadedClasses;
		}

	    //check all class in a given jar are all loaded by Wala
	    public static int getUnloadedClassNum(ClassHierarchy cha, String jarFile) throws IOException {
	    	return getUnloadedClasses(cha, jarFile).size();
	    }
	    
	    public static String iclassToClassName(IClass c) {
	    	TypeName tn = c.getName();
	    	String packageName = Utils.translateSlashToDot(tn.getPackage() == null ? "" : tn.getPackage().toString() + ".");
	    	String className = tn.getClassName().toString();
	    	return packageName + className;
	    }
	    
	    public static String javaClassToWalaClass(String javaFullClassName) {
	    	return "L" + Utils.translateDotToSlash(javaFullClassName);
	    }
	    
	    //utilities for ir
	    public static String getAllIRAsString(CGNode node) {
	    	StringBuilder sb = new StringBuilder();
	    	Iterator<SSAInstruction> it = node.getIR().iterateAllInstructions();
	    	while(it.hasNext()) {
	    		SSAInstruction ssa = it.next();
	    		sb.append(ssa);
	    		sb.append(Globals.lineSep);
	    	}
	    	return sb.toString();
	    }
	    
	    /**
	     * check if a given class is a Runnable
	     * */
	    private static IClass RUNNABLE = null;
	    public static IClass getRunnable(ClassHierarchy cha) {
	    	if(RUNNABLE == null) {
	    		RUNNABLE = WALAUtils.lookupClass(cha, "java.lang.Runnable");
	    	}
	    	if(RUNNABLE == null) {
	    		throw new Error("No runnable loaded.");
	    	}
	    	return RUNNABLE;
	    }
	    public static boolean isRunnable(ClassHierarchy cha, IClass c) {
	    	IClass runnable = getRunnable(cha);
	    	return cha.isAssignableFrom(runnable, c);
	    }
	    public static Collection<IClass> getRunnablesInApp(ClassHierarchy cha) {
	    	Collection<IClass> cs = new LinkedHashSet<IClass>();
	    	//only count client code
	    	for(IClass c : cha) {
	    		if(c.getClassLoader().getReference().equals(ClassLoaderReference.Application)) {
	    			cs.add(c);
	    		}
	    	}
	    	return cs;
	    }
	    
	    /**
	     * filter a collection node by package names
	     * */
	    public static Collection<CGNode> filterCGNodeByPackages(Collection<CGNode> nodes, String[] packages) {
	    	if(packages == null) {
	    		throw new RuntimeException("The package name can not be null.");
	    	}
	    	Collection<CGNode> filteredNodes = new HashSet<CGNode>();
	    	for(CGNode node : nodes) {
	    		IMethod method = node.getMethod();
	    		String packageName = getJavaPackageName(method.getDeclaringClass());
	    		boolean retained = false;
	    		for(String pName : packages) {
	    			if(packageName.startsWith(pName)) {
	    				retained = true;
	    				break;
	    			}
	    		}
	    		if(retained) {
	    			filteredNodes.add(node);
	    		}
	    	}
	    	
	    	return filteredNodes;
	    }
}