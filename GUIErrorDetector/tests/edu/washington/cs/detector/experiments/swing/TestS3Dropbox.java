package edu.washington.cs.detector.experiments.swing;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.callgraph.impl.DefaultEntrypoint;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;

import edu.washington.cs.detector.UIAnomalyDetector;
import edu.washington.cs.detector.CGBuilder.CG;
import edu.washington.cs.detector.util.Globals;
import edu.washington.cs.detector.util.Log;
import edu.washington.cs.detector.util.WALAUtils;
import junit.framework.Test;
import junit.framework.TestSuite;

public class TestS3Dropbox extends AbstractSwingTest {
	
	public static Test suite() {
		return new TestSuite(TestS3Dropbox.class);
	}

	private String jarDir = "D:\\research\\guierror\\eclipsews\\S3dropbox\\jars\\";
	private String getJar(String jarName) {
		return jarDir + jarName;
	}
	
	private String appPath =
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
	
	public void testS3Dropbox() throws IOException, ClassHierarchyException {
		Log.logConfig("./log.txt");
		String[] packages = new String[]{"com.tomczarniecki.s3."};
		
		UIAnomalyDetector.DEBUG = true;
		super.setCGType(CG.OneCFA);
		
		super.setNondefaultCG(true);
//		super.setAddHandlers(true);
		super.setAddExtraEntrypoints(true);
		super.setMatchRunWithInvoke(true);
		
	    super.checkCallChainNumber(-1, appPath, packages);
	    UIAnomalyDetector.DEBUG = false;
	}
	
	@Override
	protected Iterable<Entrypoint> getAdditonalEntrypoints(ClassHierarchy cha) {
		Collection<Entrypoint> extraEPs = new HashSet<Entrypoint>();
		//add some
		//com.tomczarniecki.s3.run()
		for(IClass c : cha) {
			for(IMethod m : c.getDeclaredMethods()) {
				String fullName = WALAUtils.getFullMethodName(m);
				if(fullName.equals("com.tomczarniecki.s3.GuiMain.run")) {
					Entrypoint defaultEP = new DefaultEntrypoint(m, cha);
					extraEPs.add(defaultEP);
					System.err.println("Add extra entry point: " + defaultEP);
				}
			}
		}
		
		return extraEPs;
	}
}
