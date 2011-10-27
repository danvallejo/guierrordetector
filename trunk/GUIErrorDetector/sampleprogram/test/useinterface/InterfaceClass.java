package test.useinterface;

interface Inter {
	void foo();
}

class A implements Inter {
	public void foo() {}
}

class B extends A {
	A a = new B();
	public void foo() { }
	public A getB() {
		return a;
	}
	B b =  new B();
}

public class InterfaceClass {
	B b =  new B();
	public void bar(Inter i) {
		i.foo();
	}
//	public static void main(String[] args) {
//		InterfaceClass c = new InterfaceClass();
//		A 
//	}
}