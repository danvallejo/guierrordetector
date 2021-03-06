package edu.washington.cs.detector;

import java.io.IOException;
import java.util.List;

import com.ibm.wala.ipa.cha.ClassHierarchyException;

import edu.washington.cs.detector.experiments.filters.RemoveSystemCallStrategy;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class TestSimpleEclipsePlugins extends TestCase {
	
	public static Test suite() {
		return new TestSuite(TestSimpleEclipsePlugins.class);
	}
	
	public void testSimplePlugin() throws ClassHierarchyException, IOException {
		EclipsePluginUIErrorMain.APP_PATH = TestCommons.plugintest_bin_dir;
		EclipsePluginUIErrorMain.UI_CLASSES_FILE = "./tests/uiclasses_plugintest.txt";
		
		EclipsePluginUIErrorMain main = new EclipsePluginUIErrorMain();
		List<AnomalyCallChain> chains = main.reportUIErrors();
		
		assertEquals(4, chains.size());
		
		CallChainFilter filter = new CallChainFilter(chains);
		List<AnomalyCallChain> afterFilter = filter.apply(new RemoveSystemCallStrategy());
		System.out.println("Number of chain after filtering: " + afterFilter.size());
		
		for(AnomalyCallChain chain : afterFilter) {
			System.out.println("---- after filtering----");
			System.out.println(chain.getFullCallChainAsString());
		}
		
		assertEquals(1, afterFilter.size());
	}

}
