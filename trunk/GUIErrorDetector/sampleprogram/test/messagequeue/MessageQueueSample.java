package test.messagequeue;

import java.util.LinkedList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class MessageQueueSample {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new MessageQueueSample().run();
	}
	
	Display display;
	Shell shell;
	
	Label label;
	
	LinkedList<Runnable> queue;
	
	public void run() {
		
		queue = new LinkedList<Runnable>();
		
		Thread testThread = new Thread(new Runnable() {
			@Override
			public void run() {
				while (!Thread.currentThread().isInterrupted()) {
					Runnable runnable = null;
					synchronized (queue) {
						if (!queue.isEmpty()) {
							runnable = queue.remove();
						}
					}
					
					if (runnable == null) {
						Thread.yield();
					} else {
//						if (display != null && !display.isDisposed()) {
//							display.asyncExec(runnable);
//						}
						
						runnable.run();
					}
				}
			}
		});
		
		testThread.start();
		
		display = new Display();
		shell = new Shell(display);
		
		shell.setLayout(new RowLayout(SWT.VERTICAL));
		
		label = new Label(shell, SWT.NONE);
		label.setText("Nothing");
		
		Button button = new Button(shell, SWT.PUSH);
		button.setText("Test");
		
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				synchronized (queue) {
					queue.add(new Runnable() {
						@Override
						public void run() {
							if (label != null && !label.isDisposed()) {
								label.setText("Success");
							}
						}
					});
				}
			}
		});
		
		shell.pack();
		shell.open();
		
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		
		testThread.interrupt();
		display.dispose();
	}
}
