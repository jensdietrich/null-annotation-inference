package nz.ac.wgtn.nullannoinference.sanitizer.examples.test_junit5;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

public class AnnotationTest {

    @Test
    public void testNoException() {}

    @Test
    public void testNPE() {
        assertThrows(NullPointerException.class,() -> {
            String s = null;
            s.toLowerCase();
        });
    }

    @Test
    public void testAIOBE() {
        assertThrows(ArrayIndexOutOfBoundsException.class,() -> {
            int[] array = new int[]{1, 2, 3};
            int v = array[3];
        });
    }

    @Test
    public void testExplicit() {
        try {
            String s = null;
            s.toLowerCase();
            fail();
        }
        catch (NullPointerException x) {
            System.out.println("as expected");
        }
    }
}