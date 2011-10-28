package test.useinterface;

interface Inter {
	void foo();
}

class A implements Inter {
	public void foo() {}
}

class B extends A {
	A a = new B();
	public void foo() { }
	public A getB() {
		return a;
	}
	B b =  new B();
}

public class InterfaceClass {
	B b =  new B();
	public void bar(Inter i) {
		i.foo();
	}
}

//========== mimic the problem ============

class SHandler {
	DStore _dstore;
	public void setDStore(DStore d) {
		this._dstore = d;
	}
	public void notifyEvent() {}
}

class Handler extends SHandler {
	public void notifyEvent() {
		System.out.println("notify event");
		IStore istore = this._dstore.getIStore();
		istore.fire();
	}
}

final class DStore {
	IStore istore = null;
	public void setIStore(IStore is) {
		this.istore = is;
	}
	public IStore getIStore() {
		return this.istore;
	}
}

interface IStore {
	public void fire();
}
class SubStore implements IStore {
	public void fire() {foo();
	};
	public void foo() {}
}

class Main {
	public static void main(String[] args) {
		IStore istore = new SubStore();
		DStore dstore = new DStore();
		dstore.setIStore(istore);
		SHandler shandler = new Handler();
		shandler.setDStore(dstore);
		shandler.notifyEvent();
	}
}

//class bar and its super class
class AbBar {
	Bridge bridge = null;
	public void setBridge(Bridge bridge) {
		this.bridge = bridge;
	}
	void bar() {}
}
class Bar extends AbBar {
	public void bar() {
		IFoo foo = this.bridge.getFoo();
		foo.foo();
	}
//	public static void main(String[] args) {
//	    //create a Foo object
//	    IFoo foo = new Foo();
//	    //create a bridge and set its foo field
//	    Bridge b = new Bridge();
//	    b.setFoo(foo);
//	    //create a bar, and set bridge
//	    AbBar bar = new Bar();
//	    bar.setBridge(b);
//	    //call bar()
//	    bar.bar();
//	}
}
//a bridge class, like a Foo wrapper
class Bridge {
	IFoo foo = null;
	public void setFoo(IFoo foo) { this.foo = foo; }
	public IFoo getFoo() {return this.foo; }
}
//
interface IFoo { void foo(); }
class Foo implements IFoo {
	public void foo() {}
}

//driver
class MainClass {
	public static void main(String[] args) {
	    //create a Foo object
	    IFoo foo = new Foo();
	    //create a bridge and set its foo field
	    Bridge b = new Bridge();
	    b.setFoo(foo);
	    //create a bar, and set bridge
	    AbBar bar = new Bar();
	    bar.setBridge(b);
	    //call bar()
	    bar.bar();
	}
}