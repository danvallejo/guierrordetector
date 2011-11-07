package test.multipaths;

public class MultiPathToStart {

	Thread thread = new Thread(
		    new Runnable() {
				public void run() {
					System.out.println();
			    }
			}
		);
	
	public void startThread1() {
			thread.start();
	}
	
	public void startThread2() {
		goToThread2();
	}
	
	public void goToThread2() {
			thread.start();
	}
	
	public static void main(String[] args) {
		MultiPathToStart mps = new MultiPathToStart();
		mps.startThread1();
		mps.startThread2();
	}
	
}