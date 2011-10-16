package test.timererror;

import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class TimerError {
	private static Text helloWorldTest;

	private static Shell shell;

	private static Display display;

	public static void main(String[] args) {
		display = new Display();
		shell = new Shell(display);

		shell.setLayout(new RowLayout(SWT.VERTICAL));

		helloWorldTest = new Text(shell, SWT.NONE);
		helloWorldTest.setText("Hello World SWT");
		helloWorldTest.pack();

		Button button = new Button(shell, SWT.NONE);
		button.setText("Test");
		button.addSelectionListener(new NewThreadSelectionAdapter());

		shell.pack();
		shell.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}

	static class NewThreadSelectionAdapter extends SelectionAdapter {

		@Override
		public void widgetSelected(final SelectionEvent e) {

			Timer timer = new Timer();
			TimerTask ts = new TimerTask() {
				public void run() {
					helloWorldTest.setText("I want to set the text");
				}
			};
			timer.schedule(ts, 1000);
		}
	}
}
