package foo;

public class B extends A {

    @Override
    public String foo(Object arg) {
        return "B";
    }
    public String foo(Object arg1,int arg2) {
        return "B";
    }
}
