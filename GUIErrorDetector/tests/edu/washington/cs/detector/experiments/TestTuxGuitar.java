package edu.washington.cs.detector.experiments;

import java.io.IOException;
import java.util.List;

import com.ibm.wala.ipa.callgraph.CGNode;

import junit.framework.TestCase;

import edu.washington.cs.detector.AnomalyCallChain;
import edu.washington.cs.detector.CGBuilder;
import edu.washington.cs.detector.CallChainFilter;
import edu.washington.cs.detector.CGBuilder.CG;
import edu.washington.cs.detector.SWTAppUIErrorMain;
import edu.washington.cs.detector.UIAnomalyDetector;
import edu.washington.cs.detector.experiments.filters.MergeSameTailStrategy;
import edu.washington.cs.detector.experiments.filters.RemoveContainingNodeStrategy;
import edu.washington.cs.detector.experiments.filters.RemoveSystemCallStrategy;
import edu.washington.cs.detector.util.Utils;

/**
 * Produce a bunch of false positives.
 * Too many errors to verify!
 * */
public class TestTuxGuitar extends TestCase {
	
	public void testRunng() throws IOException {
		String appPath = "D:\\research\\guierror\\subjects\\tuxguitar-1.2.jar" + ";" +  SWTAppUIErrorMain.swtJar;
		UIAnomalyDetector detector = new UIAnomalyDetector(appPath);
		
		CGBuilder builder = new CGBuilder(appPath);
		builder.setCGType(CG.OneCFA);
		builder.buildCG();
		
		assertTrue(builder.getCallGraph().getEntrypointNodes().size() == 1);
		
		List<AnomalyCallChain> chains = detector.detectUIAnomaly(builder);
		
		System.out.println("Number of anomaly call chains: " + chains.size());
		
		CallChainFilter filter = new CallChainFilter(chains);
		chains = filter.apply(new RemoveSystemCallStrategy());
		System.out.println("No of chains after filtering system classes: " + chains.size());
		
		filter = new CallChainFilter(chains);
		chains = filter.apply(new RemoveContainingNodeStrategy("Lorg/eclipse/swt/graphics/Device, dispose()V"));
		System.out.println("No of chains after filtering dispose(): " + chains.size());
		
		//not safe
		filter = new CallChainFilter(chains);
		chains = filter.apply(new RemoveContainingNodeStrategy("Lorg/herac/tuxguitar/gui/helper/SyncThread$1, run()V"));
		System.out.println("No of chains after removing sync thread: " + chains.size());
		
		filter = new CallChainFilter(chains);
		chains = filter.apply(new RemoveContainingNodeStrategy("Lorg/herac/tuxguitar/gui/util/MessageDialog, errorMessage(Lorg/eclipse/swt/widgets/Shell;Ljava/lang/Throwable;)V"));
		System.out.println("No of chains after removing message dialog thread: " + chains.size());
		
//		filter = new CallChainFilter(chains);
//		chains = filter.apply(new RemoveSynchronizeTask("Lorg/herac/tuxguitar/util/TGSynchronizer$TGSynchronizerTask, run()V"));
//		System.out.println("No of chains after removing sync task dialog thread: " + chains.size());
		
		filter = new CallChainFilter(chains);
		chains = filter.apply(new MergeSameTailStrategy());
		System.out.println("No of chains after removing common tails: " + chains.size());
		
		
		Utils.dumpAnomalyCallChains(chains, "./logs/tuxguitar-1.2-anomalies.txt");
		
	}
	
	class RemoveSynchronizeTask extends RemoveContainingNodeStrategy {

		public RemoveSynchronizeTask(String sig) {
			super(sig);
		}
		
		@Override
		protected boolean remove(AnomalyCallChain c) {
			boolean hasSyncTask = false;
			for(CGNode node : c.getFullCallChain()) {
				if(node.toString().indexOf(this.sig) != -1) {
					hasSyncTask = true;
					break;
				}
			}
			//count how many thread start it has
			int threadstartcount = 0;
			for(CGNode node : c.getFullCallChain()) {
				if(node.toString().indexOf("Ljava/lang/Thread, start()V") != -1) {
					threadstartcount ++;
				}
			}
			
			return hasSyncTask && (threadstartcount < 2);
		}
		
	}
	
	
}