package edu.washington.cs.detector;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.util.graph.Graph;

import edu.washington.cs.detector.CGBuilder.CG;
import edu.washington.cs.detector.util.Globals;
import edu.washington.cs.detector.util.Log;
import edu.washington.cs.detector.util.WALAUtils;
import junit.framework.TestCase;

public class TestSimpleAndroidExamples extends TestCase {
	
	public String appPath =
		"D:\\research\\guierror\\eclipsews\\TestAndroid\\bin\\classes\\test\\android"
		+ Globals.pathSep +
		"D:\\Java\\android-sdk-windows\\platforms\\android-8\\android.jar"
		+ Globals.pathSep
		+ "D:\\Java\\android-sdk-windows\\platforms\\android-8\\data\\layoutlib.jar";
	
	public void testAndroidApp() throws IOException, ClassHierarchyException {
		CGBuilder builder = new CGBuilder(appPath,UIAnomalyDetector.EXCLUSION_FILE_SWING);
	    builder.makeScopeAndClassHierarchy();
	    
	    //find all UI classes
	    ClassHierarchy cha = builder.getClassHierarchy();
	    int i = 0;
	    List<String> uiClasses = new LinkedList<String>();
	    for(IClass c : cha) {
	    	if(c.toString().indexOf("test/android/TestAndroidActivity") != -1) {
	    		uiClasses.add(WALAUtils.getJavaFullClassName(c));
	    	}
	    }
	    Iterable<Entrypoint> entries = CGEntryManager.getAllPublicMethods(builder, uiClasses);
		int size = 0;
		for(Entrypoint entry : entries) {
			assertTrue(entry != null);
			System.out.println(" -- " + entry );
			size++;
		}
		System.out.println("Number of entries for building CG: " + size);
		builder.setCGType(CG.RTA);
		builder.buildCG(entries);
		
		System.out.println("number of entry node in the built CG: " + builder.getCallGraph().getEntrypointNodes().size());
		System.out.println("CG node num: " + builder.getCallGraph().getNumberOfNodes());
		System.out.println("App CG node num: " + builder.getAppCallGraph().getNumberOfNodes());
		
		Graph<CGNode> graph = builder.getAppCallGraph();
		Log.logConfig("./log.txt");
		WALAUtils.logCallGraph(graph);
	}

}