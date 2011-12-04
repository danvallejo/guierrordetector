package edu.washington.cs.detector.experiments.swing;

import java.io.IOException;

public class TestJReversePro extends AbstractSwingTest {
    public String appPath
        = "D:\\research\\guierror\\subjects\\swing-programs\\jreversepro.jar";
	
	public void testJReversePro() throws IOException {
		super.checkCallChainNumber(0, appPath, new String[]{"jreversepro"});
	}
}
