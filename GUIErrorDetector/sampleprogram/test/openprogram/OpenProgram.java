package test.openprogram;

/**
 * To mimic eclipse's invocation. Suppose entryMethod is the entry
 * in an eclipse plugin class
 * */
public class OpenProgram {
	
	public void entryMethod(Foo f, Bar b) {
		f.foo();
		b.bar();
	}
}

class Foo {
	public void foo() {}
}

class FooSub extends Foo {
	public void foo() {};
}

class Bar {
	public void bar() {}
}

class BarSub extends Bar {
	public void bar() {}
}