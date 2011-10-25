package edu.washington.cs.detector.util;

import java.util.Collection;
import java.util.Iterator;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.ipa.callgraph.AnalysisCache;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.impl.Util;
import com.ibm.wala.ipa.callgraph.propagation.LocalPointerKey;
import com.ibm.wala.ipa.callgraph.propagation.SSAPropagationCallGraphBuilder;
import com.ibm.wala.ipa.callgraph.propagation.cfa.nCFABuilder;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.TypeName;
import com.ibm.wala.util.WalaException;
import com.ibm.wala.util.collections.CollectionFilter;
import com.ibm.wala.util.collections.Filter;
import com.ibm.wala.util.graph.Graph;
import com.ibm.wala.util.graph.GraphSlicer;

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
}