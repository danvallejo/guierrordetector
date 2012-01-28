package edu.washington.cs.detector.experiments.search;

public class SimpleClock {
	
	static long startTime = -1;
	
	static long budget = -1;
	
	public static void start() {
		if(startTime > 0) {
			throw new RuntimeException();
		}
		System.err.println("Clock started...");
		startTime = System.currentTimeMillis();
	}
	
	public static void setBudget(long b) {
		System.err.println(" budget set, max millis: " + b);
		budget = b;
	}
	
	public static void reset() {
		System.err.println("Clock reset...");
		startTime = -1;
		budget = -1;
	}
	
	public static boolean finish() {
		if(startTime < 0) {
			throw new RuntimeException();
		}
		return System.currentTimeMillis() - startTime > budget;
	}
	
}