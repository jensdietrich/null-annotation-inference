package nz.ac.wgtn.nullannoinference.lsp;

import com.google.common.graph.Graph;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import org.apache.commons.io.FileUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.function.Predicate;

public class ClassHierarchyBuilder {

    public static Graph<String> buildTypeGraph (Predicate<String> typeFilter, File... classLocations) throws IOException {
        MutableGraph<String> graph = GraphBuilder.directed().allowsSelfLoops(false).build();
        for (File projectFolder:classLocations) {
            add(typeFilter,graph,projectFolder);
        }
        return graph;
    }

    private static void add(Predicate<String> typeFilter, MutableGraph<String> graph,File project) throws IOException {
        File compiledTestClasses = new File(project,"target/classes");
        if (!compiledTestClasses.exists()) {
            throw new IllegalStateException("project must be built before analysis can be found (mvn class)");
        }
        Collection<File> classFiles = FileUtils.listFiles(compiledTestClasses,new String[]{"class"},true);
        if (classFiles.isEmpty()) {
            throw new IllegalStateException("No .class files found, make sure that the project has been built");
        }
        for (File classFile:FileUtils.listFiles(project,new String[]{"class"},true)) {
            // System.out.println("Analysing: " + classFile);
            try (InputStream in = new FileInputStream(classFile)) {
                new ClassReader(in).accept(new SubtypeRelationshipExtractor(typeFilter,graph), 0);
            }
        }
    }

    static class SubtypeRelationshipExtractor extends ClassVisitor {
        private MutableGraph<String> graph = null;
        private Predicate<String> typeFilter = null;

        public SubtypeRelationshipExtractor(Predicate<String> typeFilter,MutableGraph<String> graph) {
            super(Opcodes.ASM9);
            this.graph = graph;
            this.typeFilter = typeFilter;
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            super.visit(version, access, name, signature, superName, interfaces);

            name = name.replace('/','.');
            superName = superName.replace('/','.');
            assert typeFilter.test(name);
            graph.addNode(name);
            if (typeFilter.test(superName)) {
                graph.putEdge(name, superName);
            }
            for (String itrfc:interfaces) {
                if (typeFilter.test(itrfc)) {
                    graph.putEdge(name, itrfc);
                }
            }
        }
    }
}
