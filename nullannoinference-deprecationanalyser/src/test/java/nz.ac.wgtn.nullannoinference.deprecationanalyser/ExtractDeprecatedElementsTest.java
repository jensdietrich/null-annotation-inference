package nz.ac.wgtn.nullannoinference.deprecationanalyser;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ExtractDeprecatedElementsTest {

    private Collection<File> classFiles = null;

    @BeforeEach
    public void setup() {
        File resourceFolder = new File(ExtractDeprecatedElementsTest.class.getResource("/").getFile());
        Assumptions.assumeTrue(resourceFolder.exists(),"folder containing test resources not found");
        File[] files = new File(resourceFolder,"pck").listFiles(f -> f.getName().endsWith(".class"));
        Assumptions.assumeTrue(files.length>0,"no class files found in resources, run \"javac pck/*.java\" in test/resources to compile classes used for testing");
        classFiles = Stream.of(files).collect(Collectors.toList());
    }

    @AfterEach
    public void tearDown() {
        classFiles = null;
    }

    @Test
    public void testAllDeprecated () throws IOException {
        List<String> deprecated = ExtractDeprecatedElements.findDeprecatedElements(classFiles);
        assertEquals(3,deprecated.size());
    }
    @Test
    public void testDeprecatedClasses () throws IOException {
        List<String> deprecated = ExtractDeprecatedElements.findDeprecatedElements(classFiles);
        assertTrue(deprecated.contains("pck.Class1"));
    }
    @Test
    public void testDeprecatedMethods () throws IOException {
        List<String> deprecated = ExtractDeprecatedElements.findDeprecatedElements(classFiles);
        assertTrue(deprecated.contains("pck.Class1::m1()V"));
    }
    @Test
    public void testDeprecatedFields () throws IOException {
        List<String> deprecated = ExtractDeprecatedElements.findDeprecatedElements(classFiles);
        assertTrue(deprecated.contains("pck.Class1::f1Ljava/lang/Object;"));
    }

}
