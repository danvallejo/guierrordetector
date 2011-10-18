package test.paflowsensitive;

public class TestFlowSensitivity {
	Foo f;
	public static void main(String[] args) {
		TestFlowSensitivity tfs = new TestFlowSensitivity();
		tfs.f = new Foo1();
		tfs.f = new Foo2();
		tfs.f.bar();
	}
}
class Foo {
	public void bar() {}
}
class Foo1 extends Foo {
	public void bar() {}
}
class Foo2 extends Foo {
	public void bar() {}
}