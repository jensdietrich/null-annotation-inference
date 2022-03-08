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

}
