package test.syncnoerror;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class SyncNoError {
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

			
			final Runnable r = new Runnable() {
				@Override
				public void run() {
					helloWorldTest.setText("I want to change the text.");
				}

			};
			Display.getDefault().asyncExec(r);
			Display.getDefault().syncExec(r);
		}
	}
}
