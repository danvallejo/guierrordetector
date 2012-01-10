package test.taimingreflection;

public class ObjCreationByRefl  {
	
	SuperA a;// = new A();
	
	/**
	 * Here are a few options:
	 * 1. given a list of classes,
	 * 2. choose all subset? and decide which to use
	 * 3. how to parse the XML file
	 * */
	
	public void doThis() {
		SuperA a = 
			(SuperA)createObject(1);
//			createObjWithArgs(1); //add the object A to its call site
		a.foo();
		
		//a.bar();
	}
	
	public void doThat() {
//		SuperA a = createRealObject();
//		a.foo();
		
		B b = (B)createObject(2);
		b.foo();
		
//		B b = (B)createObjWithArgs(2);
//		b.foo();
	}
	
	public static SuperA createObjectWithArgs_1() {
		return new SuperA(null);
	}
	
	public static SuperA createObjectWithArgs_2() {
		return new B(null);
	}

	public static void main(String[] args) {
		ObjCreationByRefl obj = new ObjCreationByRefl();
		obj.doThis();
		obj.doThat();
	}
	
	public Object createObject(int id) {
	    return null;//a.findAByID();
	}
	
	public SuperA createRealObject() {
		return new SuperA(null);
	}
	
	public SuperA createObjectA() {
		return new SuperA(null);
	}
	
	public SuperA createObjectB() {
		return new B(null);
	}
	
	public SuperA createObjWithArgs(int i) {
		switch (i) {
		    case 1:
			    return new SuperA(null);
		    case 2:
			    return new B(null);
		}
		return null;
	}
}

 class SuperA {
	 
	C c = null;
	
//	public SuperA() { c = new C();	}
	
	public SuperA(Object x) {
		c = new C();
	}
	 
	public void foo() { 
		bar(); 
		}
	
	public void bar() {}
	
	public  SuperA findAByID() {
		return this;
//		String x = "ss";
//		if(x.equals("yy")) {
//			return new A();
//		} else {
//			return new B();
//		}
	}
}

class B extends SuperA {
	public void foo() {}
	
//	public B() { }
	
	public B(Object x) {
		super(x);
		foo();
	}
}

class C {
	public void bar() {}
}