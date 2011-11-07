package test.multichecks;

import org.eclipse.swt.widgets.Text;

public class MultiChecks {
	
	private static Text helloWorldTest = new Text(null, 1);
	
	Thread thread = new Thread(
		    new Runnable() {
				public void run() {
					helloWorldTest.setText("Hello World SWT");
			    }
			}
		);
	
	public void startThread1() {
			thread.start();
	}
	
	public void startThread2() {
		goToThread2();
	}
	
	public void goToThread2() {
			thread.start();
	}
	
	public static void main(String[] args) {
		MultiChecks mcs = new MultiChecks();
		mcs.startThread1();
		mcs.startThread2();
	}
}
