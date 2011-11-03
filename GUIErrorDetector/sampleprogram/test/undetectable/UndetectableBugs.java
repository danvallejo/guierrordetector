package test.undetectable;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class UndetectableBugs {
    /**
     * The bug inside foo() can not be detected if only
     * Bar.bar() is treated as entry point
     * */
//	public static void main(String[] args) {
	public void objectCreation() {
	    IFoo foo = new Foo();
	    Bridge b = new Bridge();
	    AbBar bar = new Bar();
	}
}

class AbBar {
	Bridge bridge = null;
	public void setBridge(Bridge bridge) {
		this.bridge = bridge;
	}
	void bar() {}
}
class Bar extends AbBar {
	public void bar() {
		IFoo foo = this.bridge.getFoo();
		foo.foo();
	}
}
//a bridge class, like a Foo wrapper
class Bridge {
	IFoo foo = null;
	public void setFoo(IFoo foo) { this.foo = foo; }
	public IFoo getFoo() {return this.foo; }
}
//
interface IFoo { void foo(); }
class Foo implements IFoo {
	public void foo() { 
		Display display = new Display ();
		Shell shell = new Shell(display);
		shell.setLayout(new RowLayout(SWT.VERTICAL));
		final Text helloWorldTest = new Text(shell, SWT.NONE);
		
		Thread t = new Thread(new Runnable() {
			public void run() {
				helloWorldTest.setText("change!");
			}
		});
		t.start();
	}
}