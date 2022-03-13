package nz.ac.wgtn.nullannoinference.refiner.lsp;

import com.google.common.collect.Multimap;
import com.google.common.graph.Graph;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import com.google.common.graph.Traverser;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.function.Predicate;

/**
 * Uses bytecode analysis to extract override relationships. Note that this computes the transitive closure.
 * @author jens dietrich
 */
public class OverrideExtractor {

    public static Graph<OwnedMethod> extractOverrides (Predicate<String> typeFilter, Collection<File> classFiles) throws IOException {
        MutableGraph<OwnedMethod> graph = GraphBuilder.directed().allowsSelfLoops(false).build();
        Graph<String> typeGraph = ClassHierarchyBuilder.buildTypeGraph(typeFilter,classFiles);
        Multimap<Method,String> methodOwnership = MethodOwnershipExtractor.extractMethodOwnership(classFiles);

        for (Method method:methodOwnership.keySet()) {
            Collection<String> owners = methodOwnership.get(method);
            for (String owner:owners) {
                OwnedMethod m1 = new OwnedMethod(owner,method.getName(),method.getDescriptor());
                graph.addNode(m1);
                Traverser.forGraph(typeGraph)
                    .breadthFirst(owner)
                    .forEach(cl->{
                        if (!owner.equals(cl) && owners.contains(cl)) {
                            OwnedMethod m2 = new OwnedMethod(cl,method.getName(),method.getDescriptor());
                            graph.addNode(m2);
                            graph.putEdge(m1,m2);
                        }
                    });
            }
        }

        return graph;

    }

}
