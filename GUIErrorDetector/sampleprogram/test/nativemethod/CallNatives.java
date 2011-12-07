package test.nativemethod;

public class CallNatives {

	public native int native_method();
	
	public void foo() { native_method(); }
	
	public static void main(String[] args) {
		CallNatives cn = new CallNatives();
		cn.foo();
	}
	
}