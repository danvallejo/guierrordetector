package test.cgiwthoutnew;

public class CGWithoutConstructor {
	
	public static A createA() {
//		return new A();
//		new A();
//		return bar();
		return (A)(new Object());
	}
	
	public static A bar() {
		return new A();
	}
	
	public static void main(String[] args) {
		A a = createA();
		a.foo();
	}
}

class A {
	public A() {}
	public void foo() {}
}