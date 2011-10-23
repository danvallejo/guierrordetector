package test.exceptionhandling;

public class ExceptionHandliing {

	public void callMethod() {
		try {
			error();
		} catch (Exception e) {
			
		}
	}
	
	public void error() {
		throw new RuntimeException();
	}
	
	public static void main(String[] args) {
		ExceptionHandliing eh = new ExceptionHandliing();
		eh.callMethod();
	}
	
}