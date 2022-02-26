package nz.ac.wgtn.nullannoinference.commons;

import java.util.List;
import java.util.Objects;

/**
 * Nullable issue encountered.
 * @author jens dietrich
 */
public class Issue {

    public enum IssueType {RETURN_VALUE, ARGUMENT,FIELD}
    public enum ProvenanceType {COLLECTED, INFERRED}

    private String className = null;
    private String methodName = null;
    private String descriptor = null;
    private IssueType kind = null;
    private ProvenanceType provenanceType = ProvenanceType.COLLECTED;
    private int argsIndex = -1;
    private String context = null;
    private List<String> stacktrace = null;
    private String trigger = null;  // root context , requires sanitisation of stacktrace to be meaningful
    private Issue parent = null;

    public Issue(String className, String methodName, String descriptor, String context, IssueType kind) {
        this.className = className;
        this.methodName = methodName;
        this.descriptor = descriptor;
        this.context = context;
        this.kind = kind;
    }

    public Issue(String className, String methodName, String descriptor, String context,IssueType kind, int argsIndex) {
        this.className = className;
        this.methodName = methodName;
        this.descriptor = descriptor;
        this.kind = kind;
        this.argsIndex = argsIndex;
        this.context = context;
    }

    public Issue getParent() {
        return parent;
    }

    public void setParent(Issue parent) {
        this.parent = parent;
        this.provenanceType = ProvenanceType.INFERRED;
    }

    public ProvenanceType getProvenanceType() {
        return provenanceType;
    }

    public void setProvenanceType(ProvenanceType provenanceType) {
        this.provenanceType = provenanceType;
    }

    public String getClassName() {
        return className;
    }

    public String getMethodName() {
        return methodName;
    }

    public IssueType getKind() {
        return kind;
    }

    public int getArgsIndex() {
        return argsIndex;
    }

    public String getDescriptor() {
        return descriptor;
    }

    public List<String> getStacktrace() {
        return stacktrace;
    }

    public String getTrigger() {
        return trigger;
    }

    public String getContext() {
        return context;
    }

    public void setStacktrace(List<String> stacktrace) {
        this.stacktrace = stacktrace;
        if (this.stacktrace!=null && !this.stacktrace.isEmpty()) {
            this.trigger = this.stacktrace.get(this.stacktrace.size()-1);
        }
        else {
            this.trigger = null;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Issue issue = (Issue) o;
        return argsIndex == issue.argsIndex && Objects.equals(className, issue.className) && Objects.equals(methodName, issue.methodName) && Objects.equals(descriptor, issue.descriptor) && kind == issue.kind && provenanceType == issue.provenanceType && Objects.equals(context, issue.context) && Objects.equals(stacktrace, issue.stacktrace) && Objects.equals(trigger, issue.trigger);
    }


}
