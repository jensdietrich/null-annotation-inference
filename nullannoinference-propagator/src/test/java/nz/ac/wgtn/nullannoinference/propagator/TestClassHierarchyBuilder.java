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

public class TestClassHierarchyBuilder {

    @Test
    public void test() throws IOException {
        File project = new File(TestClassHierarchyBuilder.class.getResource("/test-project").getFile());
        assumeTrue(project.exists());
        assumeTrue(new File(project, "target/classes").exists(), "project containing test data (resources/test-project) has not been built, build test projects with mvn compile");
        Collection<File> classFiles = FileUtils.listFiles(new File(project, "target/classes"),new String[]{"class"},true);
        Graph<String> subtypeGraph = ClassHierarchyBuilder.buildTypeGraph(t -> t.startsWith("foo."),classFiles);

        assertEquals(4,subtypeGraph.nodes().size());
        assertTrue(subtypeGraph.nodes().contains("foo.A"));
        assertTrue(subtypeGraph.nodes().contains("foo.B"));
        assertTrue(subtypeGraph.nodes().contains("foo.C"));
        assertTrue(subtypeGraph.nodes().contains("foo.D"));

        assertEquals(2,subtypeGraph.edges().size());
        assertTrue(subtypeGraph.hasEdgeConnecting("foo.B","foo.A"));
        assertTrue(subtypeGraph.hasEdgeConnecting("foo.C","foo.B"));
    }


}
