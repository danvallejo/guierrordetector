package edu.washington.cs.detector.experiments;

public class TestJReversePro extends AbstractSwingTest {
    public String appPath
        = "D:\\research\\guierror\\subjects\\swing-programs\\jreversepro.jar";
	
	public void testJReversePro() {
		super.checkCallChainNumber(0, appPath, new String[]{"jreversepro"});
	}
}
