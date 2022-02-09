package nz.ac.wgtn.nullannoinference.agent.test;

// class used for testing -- static methods, return values
public class FooReturn2 {
    public static void main (String[] args) {
        m1(new String[]{"hello"});
        m2(new String[][]{new String[]{"foo"}});
    }

    private static void m1(String[] obj) {
        System.out.println(obj);
    }

    private static Object m2(String[][] obj) {
        System.out.println(obj);
        return null;
    }

}
