package edu.washington.cs.detector.util;

import java.io.IOException;

import com.ibm.wala.core.tests.callGraph.CallGraphTestUtil;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.util.config.AnalysisScopeReader;
import com.ibm.wala.util.io.FileProvider;

import edu.washington.cs.detector.SWTAppUIErrorMain;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class TestPropertyReader extends TestCase {
	
	public static Test suite() {
		return new TestSuite(TestPropertyReader.class);
	}

	public void testDetectorProperty() {
		PropertyReader reader = PropertyReader.createInstance("./src/detector.properties");
		this.checkAndShowEntryNumber(3, reader);
	}
	
	public void testTestProperty() {
		PropertyReader reader = PropertyReader.createInstance("./tests/tests.properties");
		this.checkAndShowEntryNumber(8, reader);
	}
	
	private void checkAndShowEntryNumber(int expectedNum, PropertyReader reader) {
		assertEquals(expectedNum, reader.getKeys().size());
		for(Object key : reader.getKeys()) {
			System.out.println("key: " + key + "; value: " + reader.getProperty((String)key));
		}
	}
	
    
    public void testLoadedJar() throws IOException, ClassHierarchyException {
    	AnalysisScope scope = AnalysisScopeReader.makeJavaBinaryAnalysisScope(SWTAppUIErrorMain.swtJar,
				FileProvider.getFile(CallGraphTestUtil.REGRESSION_EXCLUSIONS));
		ClassHierarchy cha = ClassHierarchy.make(scope);
		
    	assertEquals(1, WALAUtils.getUnloadedClassNum(cha, SWTAppUIErrorMain.swtJar));
    }
}
