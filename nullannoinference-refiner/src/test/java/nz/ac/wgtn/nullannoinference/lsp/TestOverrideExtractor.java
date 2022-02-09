package nz.ac.wgtn.nullannoinference.lsp;

import com.google.common.graph.Graph;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class TestOverrideExtractor {

    @Test
    public void test() throws IOException {
        File project = new File(TestOverrideExtractor.class.getResource("/test-project").getFile());
        assumeTrue(project.exists());
        assumeTrue(new File(project, "target/classes").exists(), "project containing test data (resources/test-project) has not been built, build test projects with mvn compile");
        Graph<OwnedMethod> graph = OverrideExtractor.extractOverrides(t -> t.startsWith("foo."),project);

        String descriptor1 = "(Ljava/lang/Object;)Ljava/lang/String;";
        String descriptor2 = "(Ljava/lang/Object;I)Ljava/lang/String;";
        OwnedMethod foo1A = new OwnedMethod("foo.A","foo",descriptor1);
        OwnedMethod foo1B = new OwnedMethod("foo.B","foo",descriptor1);
        OwnedMethod foo1C = new OwnedMethod("foo.C","foo",descriptor1);
        OwnedMethod foo1D = new OwnedMethod("foo.D","foo",descriptor1);
        OwnedMethod foo2B = new OwnedMethod("foo.B","foo",descriptor2);

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
