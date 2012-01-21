package edu.washington.cs.detector.experiments.straightforward;

import java.io.IOException;
import java.util.Collection;

import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.util.io.FileProvider;

import edu.washington.cs.detector.CGBuilder;
import edu.washington.cs.detector.CGEntryManager;
import edu.washington.cs.detector.CGBuilder.CG;
import edu.washington.cs.detector.UIAnomalyDetector;
import edu.washington.cs.detector.experiments.straightforward.UnsafeUIAccessMethodFinder.GUI;
import edu.washington.cs.detector.experiments.swing.TestS3Dropbox;
import edu.washington.cs.detector.util.Globals;
import edu.washington.cs.detector.util.SwingUtils;
import edu.washington.cs.detector.util.Utils;
import junit.framework.TestCase;

public class TestS3DropboxNaive extends TestS3Dropbox {

	private String jarDir = "D:\\research\\guierror\\eclipsews\\S3dropbox\\jars\\";
	private String getJar(String jarName) {
		return jarDir + jarName;
	}
	
	public void testSwingS3Dropbox() throws ClassHierarchyException, IOException {
		String appPath =
			"D:\\research\\guierror\\subjects\\swing-programs\\s3dropbox1.6-trunk.jar" + Globals.pathSep +
			getJar("aws-java-sdk-1.2.15.jar") + Globals.pathSep + 
			getJar("commons-cli-1.2.jar") + Globals.pathSep +
			getJar("commons-codec-1.3.jar") + Globals.pathSep +
			getJar("commons-io-1.4.jar") + Globals.pathSep +
			getJar("commons-lang-2.4.jar") + Globals.pathSep +
			getJar("forms-1.0.6.jar") + Globals.pathSep +
			getJar("httpclient-4.1.1.jar") + Globals.pathSep +
			getJar("httpcore-4.1.jar") + Globals.pathSep +
			getJar("jcl-over-slf4j-1.6.1.jar") + Globals.pathSep +
			getJar("joda-time-1.5.2.jar") + Globals.pathSep +
			getJar("looks-2.0.1.jar") + Globals.pathSep +
			getJar("slf4j-api-1.6.1.jar") + Globals.pathSep +
			getJar("slf4j-simple-1.6.1.jar") + Globals.pathSep
			;
		String[] packages = new String[]{"com.tomczarniecki.s3."};
		
		CGBuilder builder = new CGBuilder(appPath, FileProvider.getFile(UIAnomalyDetector.EXCLUSION_FILE_SWING));
		builder.setCGType(CG.RTA);
		
		builder.makeScopeAndClassHierarchy();
		Iterable<Entrypoint> eps = com.ibm.wala.ipa.callgraph.impl.Util.makeMainEntrypoints(builder.getAnalysisScope(),
				builder.getClassHierarchy());
		Iterable<Entrypoint> eventHandlers = SwingUtils.getAllAppEventhandlingMethodsAsEntrypoints(builder.getClassHierarchy(), packages);
		eps = CGEntryManager.mergeEntrypoints(eps, eventHandlers);
		eps = CGEntryManager.mergeEntrypoints(eps, getAdditonalEntrypoints(builder.getClassHierarchy()));
		
		//start build call graph
		builder.buildCG(eps);
		
		//check the straightforward approach result
		UnsafeUIAccessMethodFinder finder = new UnsafeUIAccessMethodFinder(builder.getAppCallGraph(), packages);
		finder.setClassHierarchy(builder.getClassHierarchy());
		finder.setUIAccessDecider(GUI.Swing);
		Collection<IMethod> unsafe = finder.finalAllUnsafeUIAccessMethods();
		
		System.out.println("No of unsafe: " + unsafe.size());
		Utils.dumpCollection(unsafe, "./logs/s3dropboxnaive.txt");
		
		//result 210, 
		
	}
	
}
