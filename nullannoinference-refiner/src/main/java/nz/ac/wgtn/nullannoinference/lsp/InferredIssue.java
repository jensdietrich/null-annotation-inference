package nz.ac.wgtn.nullannoinference.lsp;

import java.util.Objects;

/**
 * Nullable issue encountered.
 * @author jens dietrich
 */
public class InferredIssue extends Issue {

    enum Inference {NONE, PROPAGATE_NULLABLE_RETURN_TO_OVERRIDEN_METHOD, PROPAGATE_ARGUMENT_TO_OVERRIDING_METHOD}

    private Inference inference = Inference.NONE;

    private Issue parent = null;

    public InferredIssue(String className, String methodName, String descriptor, IssueType kind, Inference inference, Issue parent) {
        super(className, methodName, descriptor, kind);
        this.inference = inference;
        this.parent = parent;
    }

    public InferredIssue(String className, String methodName, String descriptor, IssueType kind, int argsIndex, Inference inference, Issue parent) {
        super(className, methodName, descriptor, kind, argsIndex);
        this.inference = inference;
        this.parent = parent;
    }

    public Inference getInference() {
        return inference;
    }

    public void setInference(Inference inference) {
        this.inference = inference;
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
        return inference == that.inference && Objects.equals(parent, that.parent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), inference, parent);
    }
}
