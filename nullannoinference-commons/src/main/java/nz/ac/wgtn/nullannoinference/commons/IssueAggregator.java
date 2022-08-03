package nz.ac.wgtn.nullannoinference.commons;

import java.util.*;
import java.util.stream.Collectors;

/**
 * An utility that takes a collection of issues, and returns a new collection of issues that is a subset of the first one,
 * with all duplicates removed. Duplicate detection is based on the equivalence of issues defined by the following properties:
 * className, methodName, descriptor, kind, argsIndex
 * I.e. provenance related attributes are ignored. For instance, if two tests reveal the same issue (say the nullability of the return
 * value of Foo::foo, then only one of them will be included in the result.
 * No particular heuristics is used to pick the representative of the equivalence class included.
 * @author jens dietrich
 */
public class IssueAggregator {

    public static Set<IssueKernel> aggregate (Set<? extends Issue> issues) {
        return issues.parallelStream()
            .map(issue -> issue.getKernel())
            .collect(Collectors.toSet());
    }


    // alternative implementation of aggregate suitable for debugging (to reveal equivalence)
    public static Set<IssueKernel> aggregateDebuggable(Set<? extends Issue> issues) {
        Map<IssueKernel,Issue> aggregated = new HashMap<>();
        for (Issue issue:issues) {
            IssueKernel kernel = issue.getKernel();
            Issue issue2 = aggregated.get(kernel);
            if (issue2 == null) {
                aggregated.put(kernel,issue);
            }
            else {
                // put breakpoint here
                System.out.println("equivalent issues detected: ");
                System.out.println("\tissue1: " + issue);
                System.out.println("\tissue2: " + issue2);
            }
        }
        return aggregated.keySet();
    }

}
