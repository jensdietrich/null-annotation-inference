package nz.ac.wgtn.nullannoinference.agent.test;

public class FooField {

    private static String F1 = null;
    private static String F2 = "not-null";
    private static String F3 = "not-null";
    private static int F4 = 42;

    private  String f1 = null;
    private  String f2 = "not-null";
    private  String f3 = "not-null";
    private  int f4 = 42;
    private int[] f5 = null;

    static {
        F3 = null;
    }

    public FooField(String f1, String f2, String f3, int f4, int[] f5) {
        this.f1 = f1;
        this.f2 = f2;
        this.f3 = f3;
        this.f4 = f4;
        this.f5 = f5;
    }

    public FooField() {
    }
}
