package nz.ac.wgtn.nullannoinference.sanitizer.examples.example1;

import org.junit.Test;

public class Test1 {

    Object foo() {
        return null;
    }

    @Test(expected = NullPointerException.class)
    public void testNPE() {
        String s = null;
        s.toLowerCase();
    }
}
