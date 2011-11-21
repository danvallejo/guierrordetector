package edu.washington.cs.detector.experiments;

/**
 * Test a swing program, too large, can not finish.
 * */
public class TestJEdit extends AbstractSwingTest {

	public String appPath = "D:\\research\\guierror\\subjects\\swing-programs\\jedit4.4.2.jar";
	
	public void testJEdit() {
		super.checkCallChainNumber(0, appPath, new String[]{"org"});
	}
}
