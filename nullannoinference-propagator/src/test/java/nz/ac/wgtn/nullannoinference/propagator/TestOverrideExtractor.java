package nz.ac.wgtn.nullannoinference.propagator;

import com.google.common.graph.Graph;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class TestOverrideExtractor {

    @Test
    public void test() throws IOException {
        File project = new File(TestOverrideExtractor.class.getResource("/test-project").getFile());
        assumeTrue(project.exists());
        assumeTrue(new File(project, "target/classes").exists(), "project containing test data (resources/test-project) has not been built, build test projects with mvn compile");
        Collection<File> classFiles = FileUtils.listFiles(new File(project, "target/classes"),new String[]{"class"},true);
        Graph<OwnedMethodInfo> graph = OverrideExtractor.extractOverrides(t -> t.startsWith("foo."),classFiles);

        String descriptor1 = "(Ljava/lang/Object;)Ljava/lang/String;";
        String descriptor2 = "(Ljava/lang/Object;I)Ljava/lang/String;";
        OwnedMethodInfo foo1A = new OwnedMethodInfo("foo.A","foo",descriptor1);
        OwnedMethodInfo foo1B = new OwnedMethodInfo("foo.B","foo",descriptor1);
        OwnedMethodInfo foo1C = new OwnedMethodInfo("foo.C","foo",descriptor1);
        OwnedMethodInfo foo1D = new OwnedMethodInfo("foo.D","foo",descriptor1);
        OwnedMethodInfo foo2B = new OwnedMethodInfo("foo.B","foo",descriptor2);

        assertEquals(5,graph.nodes().size());
        assertTrue(graph.nodes().contains(foo1A));
        assertTrue(graph.nodes().contains(foo1B));
        assertTrue(graph.nodes().contains(foo1C));
        assertTrue(graph.nodes().contains(foo1D));
        assertTrue(graph.nodes().contains(foo2B));

        assertEquals(3,graph.edges().size());
        assertTrue(graph.hasEdgeConnecting(foo1B,foo1A));
        assertTrue(graph.hasEdgeConnecting(foo1C,foo1B));
        assertTrue(graph.hasEdgeConnecting(foo1C,foo1A)); // computes the transitive closure
    }


}
