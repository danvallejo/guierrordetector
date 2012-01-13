package edu.washington.cs.detector.experiments.swing;

import java.io.IOException;

import com.ibm.wala.ipa.cha.ClassHierarchyException;

import edu.washington.cs.detector.CGBuilder.CG;
import edu.washington.cs.detector.util.Log;

/**
 * Test a swing program, too large, can not finish.
 * It can not complete in 2 hours, if using RTA, it outputs too many warnings
 * bugs: http://jedit.9.n6.nabble.com/jedit-Bugs-2946031-Drop-file-make-Swing-threading-rule-violation-td1781987.html
 * */
public class TestJEdit extends AbstractSwingTest {

	public String appPath = "D:\\research\\guierror\\subjects\\swing-programs\\jedit4.4.2.jar";
	
	public void testJEdit() throws IOException, ClassHierarchyException {
		Log.logConfig("./log.txt");
		
		super.setCGType(CG.RTA);
//		super.setCGType(CG.ZeroCFA);
//		super.setAddHandlers(true);
		super.checkCallChainNumber(0, appPath, new String[]{"org.gjt.sp."});
	}
}
