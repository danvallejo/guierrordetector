package edu.washington.cs.detector.experiments.android;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.util.graph.Graph;

import edu.washington.cs.detector.AnomalyCallChain;
import edu.washington.cs.detector.CGBuilder;
import edu.washington.cs.detector.CGEntryManager;
import edu.washington.cs.detector.CGBuilder.CG;
import edu.washington.cs.detector.UIAnomalyDetector;
import edu.washington.cs.detector.guider.CGTraverseAndroidGuider;
import edu.washington.cs.detector.guider.CGTraverseExploreClientRunnableStrategy;
import edu.washington.cs.detector.guider.CGTraverseGuider;
import edu.washington.cs.detector.guider.CGTraverseOnlyClientRunnableStrategy;
import edu.washington.cs.detector.util.Globals;
import edu.washington.cs.detector.util.Utils;

public class TestMozillaFirefox extends AbstractAndroidTest {

	@Override
	protected String getAppPath() {
		String appPath = 
			//"D:\\research\\guierror\\subjects\\android-programs\\extracted\\mozilla-android-base\\classes\\org\\mozilla"
			"D:\\research\\guierror\\subjects\\android-programs\\extracted\\mozilla-android-abstract-removed\\classes\\org\\mozilla"
			+ Globals.pathSep
			+ "D:\\research\\guierror\\eclipsews\\GUIErrorDetector\\exp-subjects\\original-android.jar";
//		    + "D:\\Java\\android-sdk-windows\\platforms\\android-14\\android.jar";
		
		return appPath;
	}

	@Override
	protected String getDirPath() {
		return "D:\\research\\guierror\\subjects\\android-programs\\extracted\\mozilla-android-base";
	}
	
	@Override
	protected Collection<Entrypoint> getExtraEntrypoints(CGBuilder builder) {
		if(builder.getClassHierarchy() == null) {
			throw new RuntimeException("Should can makeClassHiearchyAndScope first!");
		}
		ClassHierarchy cha = builder.getClassHierarchy();
		List<String> otherClasses = new LinkedList<String>();
	    otherClasses.add("android.view.ViewRoot");
	    otherClasses.add("org.mozilla.gecko.gfx.LayerRender");
	    otherClasses.add("org.mozilla.gecko.gfx.LayerController");
	    
	    otherClasses.add("org.mozilla.gecko.gfx.GeckoSoftwareLayerClient");
	    
	    otherClasses.add("android.app.Activity");
//	    otherClasses.add("android.view.View");
//	    otherClasses.add("org.mozilla.gecko.GeckoApp");
	    
	    
	    Iterable<Entrypoint> otherClassMethods = CGEntryManager.getAllPublicMethods(builder, otherClasses, false);
	    //the extra entrypoint collection
	    Collection<Entrypoint> extraEntrypoints = new HashSet<Entrypoint>();
	    for(Entrypoint ep : otherClassMethods) {
	    	extraEntrypoints.add(ep);
//	    	System.out.println("eeextra entrypoint: " + ep);
	    }
//	    System.exit(0);
	    return extraEntrypoints;
	}
	
	
//	@Override
//	protected Iterable<Entrypoint> getQuerypoints(CGBuilder builder, Iterable<Entrypoint> entries) {
//		Collection<Entrypoint> result = new HashSet<Entrypoint>();
//		
//		for(Entrypoint ep : entries) {
//			if(ep.toString().indexOf("Lorg/mozilla/gecko/gfx/LayerView, <init>") != -1) {
//				result.add(ep);
//			}
//		}
//		
//		System.err.println("Number of entry points: " + result.size());
//		Utils.logCollection(result);
//		
//		return result;
//	}
	
//	@Override
//	protected Iterable<Entrypoint> removeRedundantEntries(Iterable<Entrypoint> points) {
//		//remove redundant entries
//		Set<String> sigs = new HashSet<String>();
//		Collection<Entrypoint> nonRedundant = new LinkedList<Entrypoint>();
//		for(Entrypoint point : points) {
////			String methodSig = point.getMethod().getSignature();
////			if(!sigs.contains(methodSig)) {
////				nonRedundant.add(point);
////				sigs.add(methodSig);
////			}
//			
//			String methodSig = point.getMethod().getSignature();
//			if(sigs.contains(methodSig)) {
//				continue;
//			}
//			if(point.toString().indexOf("GeckoApp, onCreate(Landroid/os/Bundle;)V") != -1) {
//				nonRedundant.add(point);
//				sigs.add(methodSig);
//				System.out.println(" +++ " + point);
//			}
//			if(point.toString().indexOf("/Activity,") != -1) {
//				nonRedundant.add(point);
//				sigs.add(methodSig);
//				System.out.println(" +++ " + point);
//			}
//		}
//		//
//		return nonRedundant;
//	}
	public void testFindErrors() {
		CGTraverseGuider ui2startGuider = new CGTraverseAndroidGuider();
		String[] arrays 
//		    = new String[]{"android.opengl."};
		= new String[]{};
		CGTraverseExploreClientRunnableStrategy start2checkGuider = new CGTraverseExploreClientRunnableStrategy(arrays);
//		start2checkGuider.addMethodGuidance("android.opengl.GLSurfaceView$GLThread.guardedRun", "org.mozilla.gecko.gfx.LayerRenderer");
		start2checkGuider.addExclusionGuidance("android.opengl.GLSurfaceView$GLThreadManager");
		start2checkGuider.addExclusionGuidance("android.opengl.GLSurfaceView$GLThread.stop");
		start2checkGuider.addExclusionGuidance("android.opengl.GLSurfaceView$EglHelper");
		
		try {
//			super.setRunnaiveApproach(true);
			super.setPackageNames(new String[]{"org.mozilla"});
//			super.setByfileName("fennec.xml");
			
			//this finds bugs
			CG type = CG.TempZeroCFA;
			type = CG.OneCFA;
//			type = CG.RTA;
	
//			UIAnomalyDetector.setToUseDFS();
			
			long startT = System.currentTimeMillis();
		    List<AnomalyCallChain> chains = super.findErrorsInAndroidApp(type, ui2startGuider, start2checkGuider);
		    long endT = System.currentTimeMillis();
		    
		    System.out.println("Total time cost: " + (endT - startT));
		    
//		    List<AnomalyCallChain> chains = super.findErrorsInAndroidApp(CG.ZeroCFA, ui2startGuider, start2checkGuider);
		    
//		    List<AnomalyCallChain> chains = super.findErrorsInAndroidApp(CG.OneCFA, ui2startGuider, start2checkGuider);
		    
		    int i = 0;
		    for(AnomalyCallChain c : chains) {
		    	System.out.println("The " + i++ + "-th chain:");
			    System.out.println(c.getFullCallChainAsString());
		    }
		    System.out.println("Number of chains: " + chains.size());
		    Utils.dumpAnomalyCallChains(chains, "./output_chains.txt");
		} catch (Throwable e) {
			e.printStackTrace();
		}
		
		//check the path
//		if(AbstractAndroidTest.appCallGraph != null) {
//			Graph<CGNode> cg = AbstractAndroidTest.appCallGraph;
//
//			CGNode startNode = null;
//			String startSig = "Landroid/opengl/GLSurfaceView$GLThread, run()V";
//			
//			CGNode endNode = null;
//			String endSig = "Landroid/view/ViewRoot, checkThread()V";
//			
//			for(CGNode node : cg) {
//				if(node.toString().indexOf(startSig) != -1) {
//					if(startNode != null) {
//						throw new Error("start sig: " + startSig);
//					}
//					startNode = node;
//				}
//			}
//			
//			for(CGNode node : cg) {
//				if(node.toString().indexOf(endSig) != -1) {
//					if(endNode != null) {
//						throw new Error("end sig: " + endSig);
//					}
//					endNode = node;
//				}
//			}
//			
//			//print
//			System.out.println("start: " + startSig);
//			System.out.println("end: " + endSig);
//			System.out.println("The chain: ");
//			List<CGNode> nodeList = AnomalyCallChain.extractPathByStartEnd(cg, startNode, endNode, "java.");
//			for(int i = 0; i < nodeList.size(); i++) {
//				if(i != 0) {
//					System.out.print(" -- >");
//				}
//				System.out.println( nodeList.get(i));
//			}
//		}
	}
}
