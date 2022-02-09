package nz.ac.wgtn.ecs.semdiff.nullable.negativetestanalysis.test_junit5;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

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
}
