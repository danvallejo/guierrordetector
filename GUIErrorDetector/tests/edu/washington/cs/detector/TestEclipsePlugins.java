package edu.washington.cs.detector;

import java.io.IOException;
import java.util.List;

import com.ibm.wala.ipa.cha.ClassHierarchyException;

import junit.framework.TestCase;

public class TestEclipsePlugins extends TestCase {
	
	public void testSimplePlugin() throws ClassHierarchyException, IOException {
		EclipsePluginUIErrorMain.APP_PATH = "D:\\research\\guierror\\eclipsews\\plugintest\\bin";
		EclipsePluginUIErrorMain.UI_CLASSES_FILE = "./tests/uiclasses_plugintest.txt";
		
		EclipsePluginUIErrorMain main = new EclipsePluginUIErrorMain();
		List<AnomalyCallChain> chains = main.reportUIErrors();
		
		CallChainFilter filter = new CallChainFilter(chains);
		List<AnomalyCallChain> afterFilter = filter.apply(new RemoveSystemCallStrategy());
		System.out.println("Number of chain after filtering: " + afterFilter.size());
		
		for(AnomalyCallChain chain : afterFilter) {
			System.out.println("---- after filtering----");
			System.out.println(chain.getFullCallChainAsString());
		}
	}

}
