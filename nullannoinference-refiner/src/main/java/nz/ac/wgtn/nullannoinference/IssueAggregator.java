package nz.ac.wgtn.nullannoinference;

import nz.ac.wgtn.nullannoinference.commons.Issue;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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

    public static Collection <Issue> aggregate (Collection<? extends Issue> issues) {
        Map<IssueCore,Issue> index = new HashMap<>();
        for (Issue issue:issues) {
            IssueCore key = new IssueCore(issue.getClassName(),issue.getMethodName(),issue.getDescriptor(),issue.getKind(),issue.getArgsIndex());
            index.put(key,issue);
        }
        return index.values();
    }

    static class IssueCore {
        private String className = null;
        private String methodName = null;
        private String descriptor = null;
        private Issue.IssueType kind = null;
        private int argsIndex = -1;

        public IssueCore(String className, String methodName, String descriptor, Issue.IssueType kind, int argsIndex) {
            this.className = className;
            this.methodName = methodName;
            this.descriptor = descriptor;
            this.kind = kind;
            this.argsIndex = argsIndex;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            IssueCore issueCore = (IssueCore) o;
            return argsIndex == issueCore.argsIndex && Objects.equals(className, issueCore.className) && Objects.equals(methodName, issueCore.methodName) && Objects.equals(descriptor, issueCore.descriptor) && kind == issueCore.kind;
        }

        @Override
        public int hashCode() {
            return Objects.hash(className, methodName, descriptor, kind, argsIndex);
        }
    }
}
