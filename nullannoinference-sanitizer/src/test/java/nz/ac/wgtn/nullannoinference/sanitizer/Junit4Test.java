package nz.ac.wgtn.nullannoinference.sanitizer;

import nz.ac.wgtn.nullannoinference.commons.ProjectType;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.io.IOException;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class Junit4Test {

    @Test
    public void test () throws IOException {
        File project = new File(Junit4Test.class.getResource("/junit4-project").getFile());
        assumeTrue(project.exists());
        assumeTrue(new File(project,"target/test-classes").exists(),"project containing test data (resources/junit4-project) has not been built, build test projects with \"mvn test-compile\" or \"mvn test\"");
        Set<MethodInfo> methods = IdentifyNegativeTests.findNegativeTests(ProjectType.MVN,project);
        assertEquals(2,methods.size());
        assertTrue(methods.contains(new MethodInfo("nz.ac.wgtn.ecs.semdiff.nullable.negativetestanalysis.test_junit4.AnnotationTest","testAIOBE","()V")));
        assertTrue(methods.contains(new MethodInfo("nz.ac.wgtn.ecs.semdiff.nullable.negativetestanalysis.test_junit4.AnnotationTest","testNPE","()V")));

    }
}
