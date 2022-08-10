package nz.ac.wgtn.nullannoinference.sanitizer.negtests;

import nz.ac.wgtn.nullannoinference.commons.ProjectType;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.io.IOException;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class Junit5Test {

    @Test
    public void test () throws IOException {
        File project = new File(Junit5Test.class.getResource("/junit5-project").getFile());
        assumeTrue(project.exists());
        assumeTrue(new File(project,"target/test-classes").exists(),"project containing test data (resources/junit5-project) has not been built, build test projects with \"mvn test-compile\" or \"mvn test\"");
        Set<MethodInfo> methods = IdentifyNegativeTests.findNegativeTests(ProjectType.MVN,project,null);
        assertEquals(2,methods.size());
        assertTrue(methods.contains(new MethodInfo("nz.ac.wgtn.nullannoinference.sanitizer.examples.test_junit5.AnnotationTest","testAIOBE","()V")));
        assertTrue(methods.contains(new MethodInfo("nz.ac.wgtn.nullannoinference.sanitizer.examples.test_junit5.AnnotationTest","testNPE","()V")));
    }
}
