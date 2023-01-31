package nz.ac.wgtn.nullannoinference.sanitizer.examples.test_junit4;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

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