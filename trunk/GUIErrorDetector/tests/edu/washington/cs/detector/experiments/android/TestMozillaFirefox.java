package edu.washington.cs.detector.experiments.android;

import java.util.List;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.util.graph.Graph;

import edu.washington.cs.detector.AnomalyCallChain;
import edu.washington.cs.detector.CGBuilder.CG;
import edu.washington.cs.detector.guider.CGTraverseAndroidGuider;
import edu.washington.cs.detector.guider.CGTraverseExploreClientRunnableStrategy;
import edu.washington.cs.detector.guider.CGTraverseGuider;
import edu.washington.cs.detector.guider.CGTraverseOnlyClientRunnableStrategy;
import edu.washington.cs.detector.util.Globals;
import edu.washington.cs.detector.util.Utils;

public class TestMozillaFirefox extends AbstractAndroidTest {

	@Override
	protected String getAppPath() {
		String appPath = "D:\\research\\guierror\\subjects\\android-programs\\extracted\\mozilla-android-base\\classes\\org\\mozilla"
			+ Globals.pathSep
			+ "D:\\research\\guierror\\eclipsews\\GUIErrorDetector\\exp-subjects\\original-android.jar";
//		    + "D:\\Java\\android-sdk-windows\\platforms\\android-14\\android.jar";
		
		return appPath;
	}

	@Override
	protected String getDirPath() {
		return "D:\\research\\guierror\\subjects\\android-programs\\extracted\\mozilla-android-base";
	}

	public void testFindErrors() {
		CGTraverseGuider ui2startGuider = new CGTraverseAndroidGuider();
		CGTraverseExploreClientRunnableStrategy start2checkGuider = new CGTraverseExploreClientRunnableStrategy(new String[]{"android.opengl."});
		start2checkGuider.addMethodGuidance("android.opengl.GLSurfaceView$GLThread.guardedRun", "org.mozilla.gecko.gfx.LayerRenderer");
		
		try {
		    List<AnomalyCallChain> chains = super.findErrorsInAndroidApp(CG.RTA, ui2startGuider, start2checkGuider);
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
