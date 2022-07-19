package nz.ac.wgtn.nullannoinference.propagator;

import com.google.common.collect.Multimap;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class TestMethodOwnershipExtractor {

    @Test
    public void test() throws IOException {
        File project = new File(TestMethodOwnershipExtractor.class.getResource("/test-project").getFile());
        assumeTrue(project.exists());
        assumeTrue(new File(project, "target/classes").exists(), "project containing test data (resources/test-project) has not been built, build test projects with mvn compile");

        Collection<File> classFiles = FileUtils.listFiles(new File(project, "target/classes"),new String[]{"class"},true);
        Multimap<MethodInfo,String> ownership = MethodOwnershipExtractor.extractMethodOwnership(classFiles);

        String descriptor1 = "(Ljava/lang/Object;)Ljava/lang/String;";
        String descriptor2 = "(Ljava/lang/Object;I)Ljava/lang/String;";

        MethodInfo foo1 = new MethodInfo("foo",descriptor1);
        MethodInfo foo2 = new MethodInfo("foo",descriptor2);

        assertEquals(2,ownership.keySet().size());
        assertTrue(ownership.containsKey(foo1));
        assertTrue(ownership.containsKey(foo2));

        Collection<String> owners1 = ownership.get(foo1);
        assertEquals(4,owners1.size());
        assertTrue(owners1.contains("foo.A"));
        assertTrue(owners1.contains("foo.B"));
        assertTrue(owners1.contains("foo.C"));
        assertTrue(owners1.contains("foo.D"));

        Collection<String> owners2 = ownership.get(foo2);
        assertEquals(1,owners2.size());
        assertTrue(owners1.contains("foo.B"));
    }


}
