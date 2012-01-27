package test.openparameter;


public class OpenParam {
	
	private A a;
	
	public OpenParam(Object o) {
		a = new A(null);
	}
	
	public void callFoo() {
		a.foo();
	}
	
}

class Entry {
	
	public void callFoo2(OpenParam p) {
		p.callFoo();
	}
}

class A {
	public A(Object o) {}
	public void foo() {}
}