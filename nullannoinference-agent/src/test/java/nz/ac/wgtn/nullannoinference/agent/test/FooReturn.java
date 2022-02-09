package nz.ac.wgtn.nullannoinference.agent.test;

// class used for testing -- static methods, return values
public class FooReturn {
    public static void main (String[] args) {
        m1("hello");
        m2("hello");
    }

    private static void m1(Object obj) {
        System.out.println(obj);
    }

    private static Object m2(Object obj) {
        System.out.println(obj);
        return null;
    }

}
