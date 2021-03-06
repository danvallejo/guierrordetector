package edu.washington.cs.detector.experiments.swing;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.swing.ImageIcon;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.callgraph.impl.DefaultEntrypoint;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;

import edu.washington.cs.detector.UIAnomalyDetector;
import edu.washington.cs.detector.CGBuilder.CG;
import edu.washington.cs.detector.util.Log;
import edu.washington.cs.detector.util.WALAUtils;
import junit.framework.Test;
import junit.framework.TestSuite;

public class TestSudokuPuzzleSolver extends AbstractSwingTest {
	
	public static Test suite() {
		return new TestSuite(TestSudokuPuzzleSolver.class);
	}

	private String appPath =
		"D:\\research\\guierror\\subjects\\swing-programs\\sudoku_puzzle_solver.jar";
	
	//2 bugs found, one is in AboutBox, another is in Implementor
	public void testSudokuPuzzleSolver() throws IOException, ClassHierarchyException {
		Log.logConfig("./log.txt");
		String[] packages = new String[]{"sudoku"};
		
		UIAnomalyDetector.DEBUG = true;
		super.setCGType(CG.RTA);
//		super.setCGType(CG.ZeroCFA);
//		super.setCGType(CG.OneCFA);
//		super.setCGType(CG.TempZeroCFA);
		super.setCGType(CG.OneCFA);
		
		UIAnomalyDetector.setToUseDFS(); //use DFS
		
		super.setNondefaultCG(true);
		super.setAddExtraEntrypoints(true);
		
	    super.checkCallChainNumber(-1, appPath, packages);
	    UIAnomalyDetector.DEBUG = false;
	}
	
	//new Aboutbox().setVisible(true);
	
	// ObjectViewer ov = new ObjectViewer();
    // ov.setLocationRelativeTo(null);
    //ov.setVisible(true);
	
	//
//    newPuzzle= new Sudoku_Puzzle_Solver();
//    
//    newPuzzle.setLocationRelativeTo(null);
//    newPuzzle.setIconImage(new ImageIcon(getClass().getResource("/sudoku/resources/ico_alpha_Categories_24x24.png") ).getImage() );
//    newPuzzle.setVisible(true);
	//
	@Override
	protected Iterable<Entrypoint> getAdditonalEntrypoints(ClassHierarchy cha) {
		
		Set<String> extraMethodSigs = new HashSet<String>();
		extraMethodSigs.add("sudoku.Aboutbox.<init>");
		extraMethodSigs.add("sudoku.ObjectViewer.<init>");
		extraMethodSigs.add("sudoku.Sudoku_Puzzle_Solver.<init>");
		extraMethodSigs.add("sudoku.Aboutbox$3.actionPerformed");
		extraMethodSigs.add("sudoku.Sudoku_Puzzle_Solver$165.actionPerformed");
		
		Collection<Entrypoint> extraEPs = new HashSet<Entrypoint>();
		for(IClass c : cha) {
			for(IMethod m : c.getDeclaredMethods()) {
				String fullName = WALAUtils.getFullMethodName(m);
				if(extraMethodSigs.contains(fullName)) {
					Entrypoint defaultEP = new DefaultEntrypoint(m, cha);
					extraEPs.add(defaultEP);
					System.err.println("Add extra entry point: " + defaultEP);
				}
			}
		}
		
		return extraEPs;
	}
}

//The 0-th chain
//Node: < Application, Lsudoku/Aboutbox$3, actionPerformed(Ljava/awt/event/ActionEvent;)V > Context: Everywhere, line: 74
// -> Node: < Application, Lsudoku/Aboutbox, access$2(Lsudoku/Aboutbox;Ljava/awt/event/ActionEvent;)V > Context: Everywhere, line: 147
// -> Node: < Application, Lsudoku/Aboutbox, jButton2ActionPerformed(Ljava/awt/event/ActionEvent;)V > Context: Everywhere, line: 148
// -> Node: < Application, Lsudoku/Aboutbox, performDesktop(I)V > Context: Everywhere, line: 156
// -> Node: synthetic < Primordial, Ljava/lang/Thread, start()V > Context: Everywhere, line: -1
// -> Node: < Application, Lsudoku/Implementor, run()V > Context: Everywhere, line: 12
// -> Node: < Application, Lsudoku/Sudoku_Puzzle_Solver, solution([[I)V > Context: Everywhere, line: 2927
// -> Node: < Primordial, Ljavax/swing/text/JTextComponent, setText(Ljava/lang/String;)V > Context: Everywhere, line: -1
//
//The 1-th chain
//Node: < Application, Lsudoku/Sudoku_Puzzle_Solver$165, actionPerformed(Ljava/awt/event/ActionEvent;)V > Context: Everywhere, line: 2337
// -> Node: < Application, Lsudoku/Sudoku_Puzzle_Solver, access$5(Lsudoku/Sudoku_Puzzle_Solver;Ljava/awt/event/ActionEvent;)V > Context: Everywhere, line: 2819
// -> Node: < Application, Lsudoku/Sudoku_Puzzle_Solver, tbSolvebtnActionPerformed(Ljava/awt/event/ActionEvent;)V > Context: Everywhere, line: 2821
// -> Node: < Application, Lsudoku/Implementor, execute([[I)V > Context: Everywhere, line: 22
// -> Node: synthetic < Primordial, Ljava/lang/Thread, start()V > Context: Everywhere, line: -1
// -> Node: < Application, Lsudoku/Implementor, run()V > Context: Everywhere, line: 12
// -> Node: < Application, Lsudoku/Sudoku_Puzzle_Solver, solution([[I)V > Context: Everywhere, line: 2927
// -> Node: < Primordial, Ljavax/swing/text/JTextComponent, setText(Ljava/lang/String;)V > Context: Everywhere, line: -1
//
//The 2-th chain
//Node: < Application, Lsudoku/Aboutbox$1, actionPerformed(Ljava/awt/event/ActionEvent;)V > Context: Everywhere, line: 51
// -> Node: < Application, Lsudoku/Aboutbox, access$0(Lsudoku/Aboutbox;Ljava/awt/event/ActionEvent;)V > Context: Everywhere, line: 151
// -> Node: < Application, Lsudoku/Aboutbox, jButton3ActionPerformed(Ljava/awt/event/ActionEvent;)V > Context: Everywhere, line: 152
// -> Node: < Application, Lsudoku/Aboutbox, performDesktop(I)V > Context: Everywhere, line: 156
// -> Node: synthetic < Primordial, Ljava/lang/Thread, start()V > Context: Everywhere, line: -1
// -> Node: < Application, Lsudoku/Implementor, run()V > Context: Everywhere, line: 12
// -> Node: < Application, Lsudoku/Sudoku_Puzzle_Solver, solution([[I)V > Context: Everywhere, line: 2927
// -> Node: < Primordial, Ljavax/swing/text/JTextComponent, setText(Ljava/lang/String;)V > Context: Everywhere, line: -1
