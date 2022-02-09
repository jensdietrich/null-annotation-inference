package nz.ac.wgtn.nullannoinference.negtests;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Objects;

/**
 * Nullable issue encountered.
 * Note that we used context.
 * @author jens dietrich
 */
public class Issue {

    enum IssueType {RETURN_VALUE, ARGUMENT}

    @SerializedName("class")
    private String className = null;
    @SerializedName("method")
    private String methodName = null;
    private String descriptor = null;
    private IssueType kind = null;
    private int argsIndex = -1;
    private String context = null;

    // by design not included in equal/hashcode -- additional property for provenance
    private List<String> stacktrace = null;

    public Issue(String className, String methodName, String descriptor, String context, IssueType kind) {
        this.className = className;
        this.methodName = methodName;
        this.descriptor = descriptor;
        this.kind = kind;
        this.context = null;
        this.context = context;
    }

    public Issue(String className, String methodName, String descriptor,String context, IssueType kind, int argsIndex) {
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

    public void setStacktrace(List<String> stacktrace) {
        this.stacktrace = stacktrace;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Issue issue = (Issue) o;
        return argsIndex == issue.argsIndex && Objects.equals(className, issue.className) && Objects.equals(methodName, issue.methodName) && Objects.equals(descriptor, issue.descriptor) && kind == issue.kind && Objects.equals(context, issue.context) && Objects.equals(stacktrace, issue.stacktrace);
    }

    @Override
    public int hashCode() {
        return Objects.hash(className, methodName, descriptor, kind, argsIndex, context, stacktrace);
    }

    @Override
    public String toString() {
        return "Issue{" +
                "className='" + className + '\'' +
                ", methodName='" + methodName + '\'' +
                ", descriptor='" + descriptor + '\'' +
                ", kind=" + kind +
                ", argsIndex=" + argsIndex +
                '}';
    }
}
