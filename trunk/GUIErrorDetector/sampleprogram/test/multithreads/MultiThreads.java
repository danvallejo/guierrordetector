package test.multithreads;

public class MultiThreads {
	
	public void runTask(Runnable runnable) {
		Thread t = new Thread(runnable);
		t.start();
	}
	
	public void doFirst() {
		runTask(new Runnable() {
			@Override
			public void run() {
				System.out.println();
			}
		});
	}
	
	public void doSecond() {
		runTask(new Runnable() {
			@Override
			public void run() {
				foo();
			}
		});
	}
	
	public void doThird() {
		runTask(new Runnable() {
			@Override
			public void run() {
				bar();
			}
		});
	}
	
	public void foo() { 	}
    public void bar() { 	}
	
	public static void main(String[] args) {
		MultiThreads mt = new MultiThreads();
		mt.doFirst();
		mt.doSecond();
		mt.doThird();
	}
}