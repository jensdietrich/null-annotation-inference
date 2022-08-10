package nz.ac.wgtn.nullannoinference.sanitizer.examples.test_junit4;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class AnnotationTest {

    @Test
    public void testNoException() {}

    @Test(expected = NullPointerException.class)
    public void testNPE() {
        String s = null;
        s.toLowerCase();
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void testAIOBE() {
        int[] array = new int[]{1,2,3};
        assertEquals(3,array[3]);
    }
}