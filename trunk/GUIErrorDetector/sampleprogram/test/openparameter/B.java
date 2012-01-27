package test.openparameter;

class Entry {
	public void callFoo2(B p) {
		p.callFoo();
	}
}

public class B {
	private A a;
	public B(Object o) {
		a = new A(null);
	}
	public void callFoo() {
		a.foo();
	}
}
class A {
	public A(Object o) {}
	public void foo() {}
}