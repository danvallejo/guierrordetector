package test.sensitivecg;


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

public class ContextSensitiveCG {

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
    	ContextSensitiveCG icg = new ContextSensitiveCG();
    	icg.dispatchViaB();
    	icg.dispatchViaC();
//    	ContextSensitiveCG icg2 = new ContextSensitiveCG();
//    	icg2.dispatchViaB();
//    	icg2.dispatchViaC();
    }
}