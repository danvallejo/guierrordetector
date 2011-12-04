package edu.washington.cs.detector.experiments.swt;

import java.io.IOException;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.printing.PrintDialog;
import org.eclipse.swt.printing.Printer;
import org.eclipse.swt.printing.PrinterData;

import edu.washington.cs.detector.AnomalyCallChain;
import edu.washington.cs.detector.CGBuilder;
import edu.washington.cs.detector.CallChainFilter;
import edu.washington.cs.detector.TestCommons;
import edu.washington.cs.detector.UIAnomalyDetector;
import edu.washington.cs.detector.CGBuilder.CG;
import edu.washington.cs.detector.experiments.filters.MergeSameEntryToStartPathStrategy;
import edu.washington.cs.detector.experiments.filters.MergeSameTailStrategy;
import edu.washington.cs.detector.util.Globals;
import edu.washington.cs.detector.util.Utils;
import edu.washington.cs.detector.util.WALAUtils;
import junit.framework.TestCase;

public class TestFaceItEditor extends TestCase {

	/**
	 * It reveals a fixed bug in JFace:
	 * 
	 * The TextViewer#print() method in JFace 3.0.1 indicates:
	 * protected void print() {
		
		final PrintDialog dialog= new PrintDialog(fTextWidget.getShell(), SWT.PRIMARY_MODAL);
		final PrinterData data= dialog.open();
		
		if (data != null) {
			
			final Printer printer= new Printer(data);
			final Runnable styledTextPrinter= fTextWidget.print(printer);
	
			Thread printingThread= new Thread("Printing") { //$NON-NLS-1$
				public void run() {
					styledTextPrinter.run();
					printer.dispose();
				}
			};
			printingThread.start();
		   }
        }
	 * 
	 * but that is fixed without using a thread in the later version:
	 * 
	 * protected void print() {
		StyledTextPrintOptions options= new StyledTextPrintOptions();
		options.printTextFontStyle= true;
		options.printTextForeground= true;
		print(options);
       }
	 * 
	 * The statistic numbers ar;
	 *   Number of anomaly call chains: 676
     *   No of chains after removing common tails: 338
     *   No of chains after removing same entry to start path: 1
	 * */
	public void testFaceItEditor() throws IOException {
		String path = "D:\\research\\guierror\\subjects\\faceit.jar"
				+ Globals.pathSep
				+ Utils.conToPath(Utils
						.getJars("C:\\Users\\szhang\\Downloads\\FaceItVersion1_1\\lib"));

		// initialize a UI anomaly detector
		UIAnomalyDetector detector = new UIAnomalyDetector(path);
		// configure the call graph builder, use 1-CFA as default
		CGBuilder builder = new CGBuilder(path);
		builder.setCGType(CG.OneCFA);
		builder.buildCG();
		// dump debugging information
		WALAUtils.dumpClasses(builder.getClassHierarchy(),
				"./logs/loaded_classes.txt");
		Utils.dumpCollection(
				WALAUtils.getUnloadedClasses(builder.getClassHierarchy(),
						TestCommons.getJarsFromPath(path)),
				"./logs/unloaded_classes.txt");
		// finding UI anomaly chain
		List<AnomalyCallChain> chains = detector.detectUIAnomaly(builder);
		System.out.println("Number of anomaly call chains: " + chains.size());
		
		CallChainFilter filter = new CallChainFilter(chains);
		chains = filter.apply(new MergeSameTailStrategy());
		System.out.println("No of chains after removing common tails: " + chains.size());

		filter = new CallChainFilter(chains);
		chains = filter.apply(new MergeSameEntryToStartPathStrategy());
		System.out.println("No of chains after removing same entry to start path: " + chains.size());
		
		Utils.dumpAnomalyCallChains(chains,
				"./logs/faceit-editor-anomalies.txt");
	}

}
