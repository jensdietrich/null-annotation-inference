package nz.ac.wgtn.nullannoinference.agent.test;

public class FooStatic {

    private static String F1 = null;
    private static String F2 = "not-null";
    private static String F3 = "not-null";

    static {
        F2 = null;
    }

    public static void foo() {
        // to trigger execution of static block
        System.out.println("class initialised: " + FooStatic.class.getName());
    }

}
