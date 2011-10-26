package test.callgraphentries;

public class CGEntries {
	
	public void foo() {}
	
	public void bar() { foo(); }
	
	public void moo() {}
	
	public static void main(String[] args) {
		CGEntries entries = new CGEntries();
		entries.bar();
	}
	
}