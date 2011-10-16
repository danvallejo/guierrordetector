package test.helloworld;

public class HelloWorld {
	
	public static void sayHello1() {
		System.out.println("hello world");
	}
	
	public static void sayHello2() {
		System.out.println("hello world");
	}
	
	public static void main(String[] args) {
		sayHello1();
		callThread();
	}
	
	public static void callThread() {
		Runnable r1 = new Runnable() {
			public void run() {
				sayHello1();
			}
		};
		
		Runnable r2 = new Runnable() {
			public void run() {
				sayHello2();
			}
		};
		
		Runnable r = r1;
		if(System.currentTimeMillis() > 10000L) {
		    r = r2;
		}
		
		execute(r);
	}
	
	public static void execute(Runnable r) {
		Thread t = new Thread(r);
		t.start();
	}
}
