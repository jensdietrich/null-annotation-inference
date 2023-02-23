package nz.ac.wgtn.nullinference.extractor;

public class Foo {

    private Foo(Void arg1, String arg2) {}

    private Void field1 = null;
    private String field2 = "";

    public Void m1(String arg1,Void arg2) {
        return null;
    }

    public String m2() {
        return "";
    }

}
