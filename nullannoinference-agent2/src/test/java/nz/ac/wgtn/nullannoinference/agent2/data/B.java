package nz.ac.wgtn.nullannoinference.agent2.data;

public class B {

    private static String F1 = "before";
    private static Object F2 = "before";
    private static String[] F3 = new String[]{"before"};
    private static int[] F4 = new int[]{42};
    private static String F5 = "before";

    public static void resetF1() {
        F1 = null;
    }

    public static void resetF2() {
        F2 = null;
    }

    public static void resetF3() {
        F3 = null;
    }

    public static void resetF4() {
        F4 = null;
    }

    public static void dontResetF5() {
        F5 = "";
    }

    public static String getF1() {
        return F1;
    }

}
