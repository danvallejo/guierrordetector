package test.openparameter;



abstract public class B {
	public void foo()  {}
	public static B b;
	public void onCreate() {
		b = this;
	}
}

class A {
	C c;
	
	public A() {
		c = new C(this);
	}
	
	public void callFoo() {
		B.b.foo();
	}
	
	public void callAnother() {
		c.callback();
	}
}

class C {
	A a;
	public C(A a) {
		this.a = a;
	}
	
	public void callback() {
		a.callFoo();
	}
}