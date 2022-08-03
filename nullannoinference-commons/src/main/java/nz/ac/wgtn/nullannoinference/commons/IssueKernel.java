package nz.ac.wgtn.nullannoinference.commons;

import java.util.Objects;

/**
 * Representation of a class of equivalent (i.e. to be aggregated) issues.
 * Two equivalent issues describe nullability of the same program element, but for different reasons
 * (e.g. different stack traces, inferred vs collected).
 * @author jens dietrich
 */
public class IssueKernel extends AbstractIssue {
    private String className = null;
    private String methodName = null;
    private String descriptor = null;
    private Issue.IssueType kind = null;
    private int argsIndex = -1;

    // only to be instantiated by Issue
    IssueKernel(String className, String methodName, String descriptor, Issue.IssueType kind, int argsIndex) {
        this.className = className;
        this.methodName = methodName;
        this.descriptor = descriptor;
        this.kind = kind;
        this.argsIndex = argsIndex;
    }

    public String getClassName() {
        return className;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getDescriptor() {
        return descriptor;
    }

    public Issue.IssueType getKind() {
        return kind;
    }

    public int getArgsIndex() {
        return argsIndex;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IssueKernel that = (IssueKernel) o;
        return argsIndex == that.argsIndex && Objects.equals(className, that.className) && Objects.equals(methodName, that.methodName) && Objects.equals(descriptor, that.descriptor) && kind == that.kind;
    }

    @Override
    public int hashCode() {
        return Objects.hash(className, methodName, descriptor, kind, argsIndex);
    }
}
