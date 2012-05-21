package test.testsamplecrash;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;


import junit.framework.TestCase;

public class TestCrash extends TestCase {
	
	private static Text helloWorldTest;

	private static Shell shell;

	private static Display display;

	
	public static void main(String[] args) {
		display = new Display();
		
		
		
		
		Thread t = new Thread() {
            public void run () {
            	Image offScreenImage = new Image (display, 1, 1);
            	GC offScreenImageGC = new GC (offScreenImage);
//            	display.getSystemFont();
            	offScreenImageGC.drawImage(offScreenImage, 1, 2);
//            	helloWorldTest.setText("hello");
            }
		};
        t.start();
	}
	
}
