package nz.ac.wgtn.nullannoinference.commons;

import java.util.List;
import java.util.Objects;

/**
 * Nullable issue encountered.
 * @author jens dietrich
 */
public class Issue {

    public enum IssueType {RETURN_VALUE, ARGUMENT,FIELD}

    private String className = null;
    private String methodName = null;
    private String descriptor = null;
    private IssueType kind = null;
    private int argsIndex = -1;
    private String context = null;
    private List<String> stacktrace = null;
    private String trigger = null;  // root context , requires sanitisation of stacktrace to be meaningful

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
        return argsIndex == issue.argsIndex && Objects.equals(className, issue.className) && Objects.equals(methodName, issue.methodName) && Objects.equals(descriptor, issue.descriptor) && kind == issue.kind && Objects.equals(context, issue.context) && Objects.equals(trigger, issue.trigger);
    }

    @Override
    public int hashCode() {
        return Objects.hash(className, methodName, descriptor, kind, argsIndex, context, trigger);
    }

    @Override
    public String toString() {
        return "Issue{" +
                "className='" + className + '\'' +
                ", methodName='" + methodName + '\'' +
                ", descriptor='" + descriptor + '\'' +
                ", context='" + context + '\'' +
                ", kind=" + kind +
                ", argsIndex=" + argsIndex +
                '}';
    }
}
