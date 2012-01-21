package edu.washington.cs.detector.experiments.straightforward;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.util.graph.Graph;

import edu.washington.cs.detector.SwingUIMethodEvaluator;
import edu.washington.cs.detector.experiments.straightforward.UnsafeUIAccessMethodFinder.GUI;
import edu.washington.cs.detector.util.WALAUtils;

public class UnsafeUIAccessMethodFinder {
	
	enum GUI {SWT, Android, Swing}
	public final Graph<CGNode> callgraph;
	public final String[] packages;
	public UIAccessDecider decider = null;
	public ClassHierarchy cha = null;
	
	public UnsafeUIAccessMethodFinder(Graph<CGNode> cg, String[] packages) {
		this.callgraph = cg;
		this.packages = packages;
	}
	public void setUIAccessDecider(GUI type) {
		this.decider = UIAccessDecider.getAccessDecider(type);
	}
	
	public void setClassHierarchy(ClassHierarchy cha) {
		this.cha = cha;
	}
	
	public Collection<IMethod> finalAllUnsafeUIAccessMethods() {
		Collection<IMethod> allUIAccessMethods = this.findAllUIAccessMethod();
		Collection<IMethod> allSafeMethods = this.alwaysCalledBySafeMethods();
		allUIAccessMethods.removeAll(allSafeMethods);
		return allUIAccessMethods;
	}
	
	private Collection<IMethod> findAllUIAccessMethod() {
		if(this.decider == null) {
		    throw new RuntimeException("Must set UIAccessDecider first.");	
		}
		//all directly ui accessing methods
		Collection<CGNode> directAccessNodes = new HashSet<CGNode>();
		for(CGNode node : callgraph) {
			Iterator<CGNode> itNode = this.callgraph.getSuccNodes(node);
			//check if every node has
			while(itNode.hasNext()) {
				CGNode next = itNode.next();
				if(decider.isUIAccessMethod(cha, next.getMethod())) {
					directAccessNodes.add(next); //break out the while loop
					break;
				}
			}
			//continue to iterate through the node list
		}
		
		//propagate through the whole call graph
		Collection<CGNode> infectedNodes = new HashSet<CGNode>();
		//avoid infinite loop
		Collection<CGNode> visited = new HashSet<CGNode>();
		//iterate until a fixed point has been reached
		//use BFS with reversed edges
		List<CGNode> queue = new LinkedList<CGNode>();
		queue.addAll(directAccessNodes);
		while(!queue.isEmpty()) {
			CGNode pop = queue.remove(0);
			infectedNodes.add(pop);
			
			Iterator<CGNode> itNode = this.callgraph.getPredNodes(pop);
			while(itNode.hasNext()) {
				CGNode caller = itNode.next();
				if(visited.contains(caller)) {
					continue;
				} else {
					visited.add(caller);
					queue.add(0, caller); //add to the BFS queue
				}
			}
		}
		
		//create the infected method collection
		Collection<IMethod> infectedMethods = new HashSet<IMethod>();
		for(CGNode node : infectedNodes) {
			IMethod m = node.getMethod();
			if(packages != null) {
				if(WALAUtils.isClassInPackages(m.getDeclaringClass(), packages)) {
					infectedMethods.add(m);
				}
			}
		}
		return infectedMethods;
	}

	
	private Collection<IMethod> alwaysCalledBySafeMethods() {
		if(this.decider == null) {
		    throw new RuntimeException("Must set UIAccessDecider first.");	
		}
		String[] safeMethodSigs = decider.getSafeMethods();
		
		Collection<CGNode> safeNodes = new HashSet<CGNode>();
		for(CGNode node : callgraph) {
			if(this.isArrayContains(node.getMethod().getSignature(), safeMethodSigs)) {
				safeNodes.add(node);
			}
		}
		
		//do traverse
		Collection<CGNode> safeCalledNodes = new HashSet<CGNode>();
		safeCalledNodes.addAll(safeNodes);
		
		Collection<CGNode> uncertainNodes = new HashSet<CGNode>();
		Collection<CGNode> visited = new HashSet<CGNode>();
		
		List<CGNode> queue = new LinkedList<CGNode>();
		queue.addAll(safeNodes);
		
		while(!queue.isEmpty()) {
			CGNode pop = queue.remove(0);
			
			//check to see if all pop's caller are safe nodes
			if(safeCalledNodes.contains(pop)) {
				//ok
			} else {
				boolean isSafeNode = true;
				Iterator<CGNode> predIt = this.callgraph.getPredNodes(pop);
				while(predIt.hasNext()) {
					CGNode next = predIt.next();
					if(!safeCalledNodes.contains(next)) {
						isSafeNode = false;
						break;
					}
				}
				if(!isSafeNode) {
					continue;
				} else {
					safeCalledNodes.add(pop);
				}
			}
			
			Iterator<CGNode> itNode = this.callgraph.getSuccNodes(pop);
			while(itNode.hasNext()) {
				CGNode next = itNode.next();
				if(visited.contains(next)) {
					continue;
				} else {
					visited.add(next);
					queue.add(queue.size(), next);
				}
			}
		}
		
		//create the infected method collection
		Collection<IMethod> result = new HashSet<IMethod>();
		for(CGNode node : safeCalledNodes) {
			IMethod m = node.getMethod();
			if(packages != null) {
				if(WALAUtils.isClassInPackages(m.getDeclaringClass(), packages)) {
					result.add(m);
				}
			}
		}
		return result;
	}
	
	private boolean isArrayContains(String dest, String[] srcs) {
		boolean contain = false;
		for(String src : srcs) {
			if(src.indexOf(dest) != -1) {
				contain = true;
				break;
			}
		}
		return contain;
	}
	
}

abstract class UIAccessDecider {
	abstract public boolean isUIAccessMethod(ClassHierarchy cha,IMethod m);
	abstract public String[] getSafeMethods();
	public static UIAccessDecider getAccessDecider(GUI type) {
		if(type == GUI.SWT) {
			return new SWTDecider();
		}
		if(type == GUI.Swing) {
			return new SwingDecider();
		}
		if(type == GUI.Android) {
			return new AndroidDecider();
		}
		throw new RuntimeException("Unsupported: " + type);
	}
}

class SWTDecider extends UIAccessDecider {

	@Override
	public boolean isUIAccessMethod(ClassHierarchy cha,IMethod m) {
		String sig = m.getSignature();
		return sig.indexOf("org.eclipse.swt.widgets.Widget.checkWidget()V") != -1 
		    || sig.indexOf("org.eclipse.swt.widgets.Display.checkDevice()V") != -1;
	}

	@Override
	public String[] getSafeMethods() {
		return new String[] {
				"Lorg/eclipse/swt/widgets/Display, syncExec(Ljava/lang/Runnable;)V",
				"Lorg/eclipse/swt/widgets/Display, asyncExec(Ljava/lang/Runnable;)V",
		};
	}
	
}

class SwingDecider extends UIAccessDecider {

	@Override
	public boolean isUIAccessMethod(ClassHierarchy cha, IMethod m) {
		if(cha == null) {
			throw new RuntimeException("Please set up the class hierarchy first.");
		}
		SwingUIMethodEvaluator evaluator = new SwingUIMethodEvaluator();
		return evaluator.isThreadUnsafeMethod(cha, m);
	}

	@Override
	public String[] getSafeMethods() {
		return new String[]{
				"javax.swing.SwingUtilities.invokeLater",
				"javax.swing.SwingUtilities.invokeAndWait"
		};
	}
}

class AndroidDecider extends UIAccessDecider {

	@Override
	public boolean isUIAccessMethod(ClassHierarchy cha,IMethod m) {
		String sig = m.getSignature();
		return sig.indexOf("android.view.ViewRoot.checkThread()V") != -1;
	}

	@Override
	public String[] getSafeMethods() {
		return new String[]{
				"android.app.Activity.runOnUiThread(Ljava/lang/Runnable;)V",
				"android.widget.ProgressBar.refreshProgress(IIZ)V",
				"android.view.View.post(Ljava/lang/Runnable;)Z",
				"android.view.View.postDelayed(Ljava/lang/Runnable;J)Z"
		};
	}
	
}