package edu.washington.cs.detector.experiments.straightforward;

import java.io.IOException;
import java.util.Collection;

import com.ibm.wala.classLoader.IMethod;

import edu.washington.cs.detector.CGBuilder;
import edu.washington.cs.detector.UIAnomalyDetector;
import edu.washington.cs.detector.CGBuilder.CG;
import edu.washington.cs.detector.experiments.straightforward.UnsafeUIAccessMethodFinder.GUI;
import edu.washington.cs.detector.util.Globals;
import edu.washington.cs.detector.util.Utils;
import junit.framework.TestCase;

public class TestSwtNaive extends TestCase {

	public void testCelearFTP() throws IOException {
		String path = "C:\\Users\\szhang\\Downloads\\celerftp0.0.1alpha.jar"
			+ Globals.pathSep + "C:\\Users\\szhang\\Downloads\\celerftp0.0.1alpha\\celerftp0.0.1alpha\\lib\\swt-windows\\swt.jar";
		
		//initialize a UI anomaly detector
        UIAnomalyDetector detector = new UIAnomalyDetector(path);
		//configure the call graph builder, use 1-CFA as default
		CGBuilder builder = new CGBuilder(path);
		builder.setCGType(CG.RTA);
		builder.buildCG();
		
		String[] packages = new String[]{"org.celerftp"};
		
		//check the straightforward approach result
		UnsafeUIAccessMethodFinder finder = new UnsafeUIAccessMethodFinder(builder.getAppCallGraph(), packages);
		finder.setClassHierarchy(builder.getClassHierarchy());
		finder.setUIAccessDecider(GUI.SWT);
		Collection<IMethod> unsafe = finder.finalAllUnsafeUIAccessMethods();
		
		System.out.println("No of unsafe: " + unsafe.size());
		Utils.dumpCollection(unsafe, "./logs/celerftpnaive.txt");
	}
	
	public void testFileBunker() throws IOException {
		String path = Utils.conToPath(Utils.getJars("C:\\Users\\szhang\\Downloads\\FileBunker-1.1.2-win32\\FileBunker-1.1.2-win32\\Resources\\lib"))
	    + Globals.pathSep + "C:\\Users\\szhang\\Downloads\\FileBunker-1.1.2-win32\\FileBunker-1.1.2-win32\\Resources\\lib\\win32\\swt.jar";

		// initialize a UI anomaly detector
		UIAnomalyDetector detector = new UIAnomalyDetector(path);
		// configure the call graph builder, use 1-CFA as default
		CGBuilder builder = new CGBuilder(path);
		builder.setCGType(CG.RTA);
		builder.buildCG();
		
        String[] packages = new String[]{"com.toubassi"};
		
		//check the straightforward approach result
		UnsafeUIAccessMethodFinder finder = new UnsafeUIAccessMethodFinder(builder.getAppCallGraph(), packages);
		finder.setClassHierarchy(builder.getClassHierarchy());
		finder.setUIAccessDecider(GUI.SWT);
		Collection<IMethod> unsafe = finder.finalAllUnsafeUIAccessMethods();
		
		System.out.println("No of unsafe: " + unsafe.size());
		Utils.dumpCollection(unsafe, "./logs/filebunkernaive.txt");
	}
	
}
