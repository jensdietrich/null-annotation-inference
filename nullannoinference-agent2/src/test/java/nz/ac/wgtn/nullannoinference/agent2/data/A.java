package nz.ac.wgtn.nullannoinference.agent2.data;

public class A {

    private String f1 = "before";
    private Object f2 = "before";
    private String[] f3 = new String[]{"before"};
    private int[] f4 = new int[]{42};
    private String f5 = "before";

    public void resetf1() {
        this.f1 = null;
    }

    public void resetf2() {
        this.f2 = null;
    }

    public void resetf3() {
        this.f3 = null;
    }

    public void resetf4() {
        this.f4 = null;
    }

    public void dontResetf5() {
        this.f5 = "";
    }

    public String getF1() {
        return f1;
    }

}
