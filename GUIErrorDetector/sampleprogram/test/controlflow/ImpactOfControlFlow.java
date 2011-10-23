package test.controlflow;

public class ImpactOfControlFlow {

	public void makeMethodCall(boolean fork) {
		if(fork) {
			error();
		} else {
			nonerror();
		}
	}
	
	public void nonerror() {
		
	}
	
	public void error() {
		//issue a bug
	}
	
	public static void main(String[] args) {
		ImpactOfControlFlow flow = new ImpactOfControlFlow();
		flow.makeMethodCall(false);
	}
	
}