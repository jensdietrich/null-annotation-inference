package nz.ac.wgtn.nullannoinference.sanitizer.examples.example1;
// non-deprecated !
public class Class2 {

    @Deprecated
    private Object f1 = null;
    private Object f2 = null;

    @Deprecated
    public Object m1() {return null;}
    public Object m2() {return null;}
}