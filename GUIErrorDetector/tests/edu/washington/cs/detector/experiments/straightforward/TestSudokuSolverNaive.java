package edu.washington.cs.detector.experiments.straightforward;

import java.io.IOException;
import java.util.Collection;

import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.util.io.FileProvider;

import edu.washington.cs.detector.CGBuilder;
import edu.washington.cs.detector.CGEntryManager;
import edu.washington.cs.detector.UIAnomalyDetector;
import edu.washington.cs.detector.CGBuilder.CG;
import edu.washington.cs.detector.experiments.straightforward.UnsafeUIAccessMethodFinder.GUI;
import edu.washington.cs.detector.experiments.swing.TestSudokuPuzzleSolver;
import edu.washington.cs.detector.util.SwingUtils;
import edu.washington.cs.detector.util.Utils;

public class TestSudokuSolverNaive extends TestSudokuPuzzleSolver {
	
	private String appPath =
		"D:\\research\\guierror\\subjects\\swing-programs\\sudoku_puzzle_solver.jar";
	
	private String[] packages = new String[]{"sudoku"};
	
	public void testSolver() throws ClassHierarchyException, IOException {
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
		Utils.dumpCollection(unsafe, "./logs/sokudosolver.txt");
		
		//output: 356
	}
	
}
