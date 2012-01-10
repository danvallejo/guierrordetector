package edu.washington.cs.detector;

import java.io.IOException;

import com.ibm.wala.core.tests.callGraph.CallGraphTestUtil;
import com.ibm.wala.ipa.callgraph.propagation.SSAPropagationCallGraphBuilder;
import com.ibm.wala.util.io.FileProvider;

import edu.washington.cs.detector.CGBuilder.CG;
import edu.washington.cs.detector.util.PDFViewer;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class TestTamingReflection extends TestCase {
	
	public static Test suite() {
		return new TestSuite(TestTamingReflection.class);
	}
	
	public void testReflection() throws IOException {
		
		SSAPropagationCallGraphBuilder.turnOnDebug();
		
		String appPath =  TestCommons.testfolder + "taimingreflection";
		CGBuilder builder = new CGBuilder(appPath, FileProvider.getFile(CallGraphTestUtil.REGRESSION_EXCLUSIONS));
		
		builder.setByPassFile("samplereflection.xml");
		
//		builder.setCGType(CG.OneCFA);
		builder.setCGType(CG.ZeroCFA);
//		builder.setCGType(CG.CFA);
//		builder.setCFAPrecision(4);
		builder.buildCG();
		
		PDFViewer.viewCG("reflection.pdf", builder.getAppCallGraph());
	}

}
