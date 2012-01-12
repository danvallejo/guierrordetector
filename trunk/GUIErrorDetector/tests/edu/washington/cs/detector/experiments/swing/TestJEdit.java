package edu.washington.cs.detector.experiments.swing;

import java.io.IOException;

import com.ibm.wala.ipa.cha.ClassHierarchyException;

/**
 * Test a swing program, too large, can not finish.
 * It can not complete in 2 hours
 * */
public class TestJEdit extends AbstractSwingTest {

	public String appPath = "D:\\research\\guierror\\subjects\\swing-programs\\jedit4.4.2.jar";
	
	public void testJEdit() throws IOException, ClassHierarchyException {
		super.checkCallChainNumber(0, appPath, new String[]{"org"});
	}
}
