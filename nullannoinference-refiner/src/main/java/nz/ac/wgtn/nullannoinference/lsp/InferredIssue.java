package nz.ac.wgtn.nullannoinference.lsp;

import nz.ac.wgtn.nullannoinference.commons.Issue;
import java.util.Objects;

/**
 * Nullable issue encountered.
 * @author jens dietrich
 */
public class InferredIssue extends Issue {


    private Issue parent = null;

    public InferredIssue(String className, String methodName, String descriptor, IssueType kind,  Issue parent) {
        super(className, methodName, descriptor, null,kind);
        this.parent = parent;
    }

    public InferredIssue(String className, String methodName, String descriptor, IssueType kind, int argsIndex, Issue parent) {
        super(className, methodName, descriptor,null, kind, argsIndex);
        this.parent = parent;
    }

    public Issue getParent() {
        return parent;
    }

    public void setParent(InferredIssue parent) {
        this.parent = parent;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        InferredIssue that = (InferredIssue) o;
        return Objects.equals(parent, that.parent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), parent);
    }
}
