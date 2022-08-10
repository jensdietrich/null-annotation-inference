package nz.ac.wgtn.nullannoinference.sanitizer.deprecation;

import nz.ac.wgtn.nullannoinference.commons.ProjectType;
import nz.ac.wgtn.nullannoinference.sanitizer.negtests.Junit4Test;
import org.junit.jupiter.api.*;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class ExtractDeprecatedElementsTest {

    private File project = new File(Junit4Test.class.getResource("/project-with-deprecations").getFile());

    @BeforeEach
    public void setup() {
        Assumptions.assumeTrue(project.exists());
        assumeTrue(new File(project,"target/classes").exists(),"project containing test data (resources/junit4-project) has not been built, build  projects with \"mvn compile\"");
    }

    @Test
    public void testAllDeprecated () throws IOException {
        List<String> deprecated = ExtractDeprecatedElements.findDeprecatedElements(ProjectType.MVN,project,null);
        assertEquals(3,deprecated.size());
    }
    @Test
    public void testDeprecatedClasses () throws IOException {
        List<String> deprecated = ExtractDeprecatedElements.findDeprecatedElements(ProjectType.MVN,project,null);
        assertTrue(deprecated.contains("nz.ac.wgtn.nullannoinference.sanitizer.examples.deprecated.Class1"));
    }
    @Test
    public void testDeprecatedMethods () throws IOException {
        List<String> deprecated = ExtractDeprecatedElements.findDeprecatedElements(ProjectType.MVN,project,null);
        assertTrue(deprecated.contains("nz.ac.wgtn.nullannoinference.sanitizer.examples.deprecated.Class1::m1()V"));
    }
    @Test
    public void testDeprecatedFields () throws IOException {
        List<String> deprecated = ExtractDeprecatedElements.findDeprecatedElements(ProjectType.MVN,project,null);
        assertTrue(deprecated.contains("nz.ac.wgtn.nullannoinference.sanitizer.examples.deprecated.Class1::f1Ljava/lang/Object;"));
    }



}
