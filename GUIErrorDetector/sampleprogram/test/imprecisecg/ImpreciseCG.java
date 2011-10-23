package test.imprecisecg;

class A {
	public void foo() { }
}

class B extends A {
    public void foo() { err(); }
    public void err() {}
}

class C extends A {
    public void foo() { }
}

public class ImpreciseCG {

	public void dispatchCalls(A a) {
		a.foo();
	}
	
	public void dispatchViaB() {
		B b = new B();
		dispatchCalls(b);
	}
	
    public void dispatchViaC() {
    	C b = new C();
		dispatchCalls(b);
	}
    
    public void main(String[] args) {
    	ImpreciseCG icg = new ImpreciseCG();
    	icg.dispatchViaB();
    	icg.dispatchViaC();
    }
}