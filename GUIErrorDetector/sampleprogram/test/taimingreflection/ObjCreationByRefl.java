package test.taimingreflection;

public class ObjCreationByRefl  {
	
	A a;// = new A();
	
	/**
	 * Here are a few options:
	 * 1. given a list of classes,
	 * 2. choose all subset? and decide which to use
	 * 3. how to parse the XML file
	 * */
	
	public void doThis() {
		A a = createObject(); //add the object A to its call site
		a.foo();
	}
	
	public void doThat() {
		B b = (B)createObject();
		b.foo();
	}

	public static void main(String[] args) {
		ObjCreationByRefl obj = new ObjCreationByRefl();
		obj.doThis();
		obj.doThat();
	}
	
	public A createObject() {
	    return a.findAByID();
	}
}

 class A {
	public void foo() {}
	
	public  A findAByID() {
		return this;
//		String x = "ss";
//		if(x.equals("yy")) {
//			return new A();
//		} else {
//			return new B();
//		}
	}
}

class B extends A {
	public void foo() {}
}