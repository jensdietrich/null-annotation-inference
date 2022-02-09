package nz.ac.wgtn.nullannoinference.agent.test;

// class used for testing -- static methods, no return
public class FooNoReturn {
    public static void main (String[] args) {
        m1("hello");
        m2(null);
        m3("",null);
    }

    private static void m1(Object obj) {
        System.out.println(obj);
    }

    private static void m2(Object obj) {
        System.out.println(obj);
    }

    private static boolean m3(Object arg1,Object arg2 ) {
        System.out.println(""+arg1+arg2);
        return true;
    }
}
