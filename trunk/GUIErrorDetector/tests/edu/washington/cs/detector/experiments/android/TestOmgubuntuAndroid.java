package edu.washington.cs.detector.experiments.android;

import java.io.IOException;
import java.util.List;

import com.ibm.wala.ipa.cha.ClassHierarchyException;

import edu.washington.cs.detector.AnomalyCallChain;
import edu.washington.cs.detector.CallChainFilter;
import edu.washington.cs.detector.CGBuilder.CG;
import edu.washington.cs.detector.experiments.filters.RemoveContainingNodeBeforeStartStrategy;
import edu.washington.cs.detector.experiments.filters.RemoveContainingNodeStrategy;
import edu.washington.cs.detector.experiments.filters.RemoveNoClientClassStrategy;
import edu.washington.cs.detector.experiments.filters.RemoveNonClientHeadStrategy;
import edu.washington.cs.detector.guider.CGTraverseAndroidGuider;
import edu.washington.cs.detector.guider.CGTraverseAndroidSafeMethodGuider;
import edu.washington.cs.detector.guider.CGTraverseGuider;
import edu.washington.cs.detector.util.Globals;

public class TestOmgubuntuAndroid extends AbstractAndroidTest {

	@Override
	protected String getAppPath() {
		String appPath = "D:\\research\\guierror\\subjects\\android-programs\\extracted\\omgubuntuandroid.jar"
			+ Globals.pathSep +
			"D:\\research\\guierror\\subjects\\android-programs\\extracted\\AdWhirlSDK_Android_3.1.1.jar"
			+ Globals.pathSep +
			"D:\\research\\guierror\\eclipsews\\GUIErrorDetector\\exp-subjects\\original-android.jar";
		
		return appPath;
	}

	@Override
	protected String getDirPath() {
		return "D:\\research\\guierror\\eclipsews\\omgubuntuandroid";
	}
	
	public void testFindErrors() throws ClassHierarchyException, IOException {
		CGTraverseGuider ui2startGuider = new CGTraverseAndroidGuider();
		CGTraverseGuider start2checkGuider = new CGTraverseAndroidSafeMethodGuider();
//		    new CGTraverseOnlyClientRunnableStrategy();
		try {
		  List<AnomalyCallChain> chains = super.findErrorsInAndroidApp(CG.RTA, ui2startGuider, start2checkGuider);
		  
		  
		  System.out.println("Before filtering: " + chains.size());
		  chains = CallChainFilter.filter(chains, new RemoveNoClientClassStrategy(new String[]{"com.echo.omgubuntu"}));
		  System.out.println("After filtering: " + chains.size());
		  chains = CallChainFilter.filter(chains, new RemoveNonClientHeadStrategy(new String[]{"com.echo.omgubuntu"}));
		  System.out.println("After checking the first entry: " + chains.size());
		  
		  int count = 0;
		  for(AnomalyCallChain chain : chains) {
			 System.out.println("The " + count++ + "-th call chain:");
			 System.out.println(chain.getFullCallChainAsString());
		  }
		  
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

}
