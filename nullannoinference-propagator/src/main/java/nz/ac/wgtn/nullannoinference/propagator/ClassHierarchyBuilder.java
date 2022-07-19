package nz.ac.wgtn.nullannoinference.propagator;

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

    public static Graph<String> buildTypeGraph (Predicate<String> typeFilter, Collection<File> classFiles) throws IOException {
        MutableGraph<String> graph = GraphBuilder.directed().allowsSelfLoops(false).build();
        add(typeFilter,graph,classFiles);
        return graph;
    }

    private static void add(Predicate<String> typeFilter, MutableGraph<String> graph,Collection<File> classFiles) throws IOException {
        for (File classFile:classFiles) {
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
