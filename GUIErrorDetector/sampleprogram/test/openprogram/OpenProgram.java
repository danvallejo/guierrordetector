package test.openprogram;

/**
 * To mimic eclipse's invocation. Suppose entryMethod is the entry
 * in an eclipse plugin class
 * */
public class OpenProgram {
	
	public OpenProgram(Foo f) {
		f.foo();
	}
	
	public void entryMethod(Foo f, Bar b, Moo m, Noo n) {
		f.foo();
		b.bar();
		m.moo();
		n.noo();
	}
}

abstract class Foo {
	public Foo(Object obj) { }
	public abstract void foo();
}

class FooSub extends Foo {
	public FooSub(Object obj) { super(obj);	}
	public void foo() {};
}

abstract class Bar {
	public Bar(Object obj) { }
	public abstract void bar();
}

class BarSub extends Bar {
	public BarSub(Object obj) { super(obj); }
	public void bar() {}
}

class Moo {
	public Moo(Object obj) {}
	public void moo() {}
}

class MooSub extends Moo {
	public MooSub(Object obj) { super(obj); }
	public void moo() {}
}

interface Noo {
	public void noo();
}

class NooSub implements Noo {
	public NooSub(Object obj) {}
	public void noo() {};
}