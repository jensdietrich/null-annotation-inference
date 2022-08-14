package nz.ac.wgtn.nullannoinference.sanitizer.nonprivate;

import nz.ac.wgtn.nullannoinference.commons.ProjectType;
import nz.ac.wgtn.nullannoinference.sanitizer.negtests.Junit4Test;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.*;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class ExtractPrivateMethodsTest {

    private File project = new File(Junit4Test.class.getResource("/mvn-project").getFile());

    @BeforeEach
    public void setup() {
        Assumptions.assumeTrue(project.exists());
        assumeTrue(new File(project,"target/classes").exists(),"project containing test data (resources/mvn-project) has not been built, build  project with \"mvn compile\"");
    }


    @Test
    public void testPrivateMethodDetection () throws IOException {
        Set<String> privateMethods = ExtractPrivateMethods.findPrivateMethods(ProjectType.MVN,project,null);
        assertEquals(2,privateMethods.size());
        assertTrue(privateMethods.contains("nz.ac.wgtn.nullannoinference.sanitizer.examples.example1.Class2::m1()Ljava/lang/Object;"));
        assertTrue(privateMethods.contains("nz.ac.wgtn.nullannoinference.sanitizer.examples.example1.Class2::m2()Ljava/lang/Object;"));
    }
}
