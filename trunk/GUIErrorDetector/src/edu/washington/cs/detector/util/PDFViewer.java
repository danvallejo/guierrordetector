package edu.washington.cs.detector.util;

import java.io.File;
import java.util.Properties;

import com.ibm.wala.examples.drivers.PDFTypeHierarchy;
import com.ibm.wala.examples.properties.WalaExamplesProperties;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.properties.WalaProperties;
import com.ibm.wala.util.WalaException;
import com.ibm.wala.util.debug.Assertions;
import com.ibm.wala.util.graph.Graph;
import com.ibm.wala.viz.DotUtil;
import com.ibm.wala.viz.PDFViewUtil;

public class PDFViewer {
	
	public static void viewCG(String pdfFileName, Graph<CGNode> g) {
		try {
			viewCGAsPDF(pdfFileName, g);
		} catch (WalaException e) {
			throw new RuntimeException(e);
		}
	}

	private static void viewCGAsPDF(String pdfFileName, Graph<CGNode> g) throws WalaException {
		Properties p = null;
		try {
			p = WalaExamplesProperties.loadProperties();
			p.putAll(WalaProperties.loadProperties());
		} catch (WalaException e) {
			e.printStackTrace();
			Assertions.UNREACHABLE();
		}
		String pdfFile = p.getProperty(WalaProperties.OUTPUT_DIR)
				+ File.separatorChar + pdfFileName;
		String dotExe = p.getProperty(WalaExamplesProperties.DOT_EXE);
		DotUtil.dotify(g, null, PDFTypeHierarchy.DOT_FILE, pdfFile, dotExe);
		String gvExe = p.getProperty(WalaExamplesProperties.PDFVIEW_EXE);
		PDFViewUtil.launchPDFView(pdfFile, gvExe);
	}
	
}