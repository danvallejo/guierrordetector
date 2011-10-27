package test.callgraphentries;

public abstract class CGEntries {
	public void foo() {}
	public void bar() { foo(); }
	public abstract void moo();
}

class NoAbstract {
	public void foo1() {}
	public void foo2() {foo1();}
	public void foo3() {foo2();}
	public void foo4(PrivateClass s) {}
	public void foo5(CGEntries s) { s.bar(); }
}

class PrivateClass {
	private PrivateClass() {}
}

class SubEntries extends CGEntries {
	@Override
	public void moo() {
		bar();
	}
}

class SubSubEntries extends SubEntries {
	@Override
	public void moo() {
		foo();
	}
}