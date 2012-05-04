package test.tamingreflectioncontext;

public class ObjCreation {

	public static void main(String[] args) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		A a = //new A(); 
			(A)A.createByRefl("test.tamingreflectioncontext.A"); 
		//A.create(R.main);
		a.foo();
//		a.bar();
	}
}

class A {
	public static A create(int a) {
		return null; //new A();
	}
	public static A createByRefl(String str) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		Class<?> c = Class.forName(str);
		return (A)c.newInstance();
	}
	public void foo() {
		
	}
	
	public void bar() {
		A a = create(R.a);
		a.foo();
	}
}

class B extends A {
	public void foo() {}
}

class R {
	static int main = 1;
	static int a = 2;
	static String aname = "test.tamingreflectioncontext.A";
}