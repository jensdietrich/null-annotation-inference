package nz.ac.wgtn.nullannoinference.annotator;

import com.google.gson.annotations.SerializedName;

import java.util.Objects;

/**
 * Nullable issue spec.
 * @author jens dietrich
 */
public class NullableSpec {

    enum Kind {RETURN_VALUE, ARGUMENT}

    @SerializedName("class")
    private String className = null;
    @SerializedName("method")
    private String methodName = null;
    private String descriptor = null;
    private Kind kind = null;
    private int index = -1;

    public NullableSpec(String className, String methodName, String descriptor, Kind kind) {
        this.className = className;
        this.methodName = methodName;
        this.descriptor = descriptor;
        this.kind = kind;
    }

    public NullableSpec(String className, String methodName, String descriptor, Kind kind, int index) {
        this.className = className;
        this.methodName = methodName;
        this.descriptor = descriptor;
        this.kind = kind;
        this.index = index;
    }

    public NullableSpec() {
    }

    public String getClassName() {
        return className;
    }

    public String getMethodName() {
        return methodName;
    }

    public Kind getKind() {
        return kind;
    }

    public int getIndex() {
        return index;
    }

    public String getDescriptor() {
        return descriptor;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NullableSpec issue = (NullableSpec) o;
        return index == issue.index && Objects.equals(className, issue.className) && Objects.equals(methodName, issue.methodName) && Objects.equals(descriptor, issue.descriptor) && kind == issue.kind;
    }

    @Override
    public int hashCode() {
        return Objects.hash(className, methodName, descriptor, kind, index);
    }

    @Override
    public String toString() {
        return "Issue{" +
                "className='" + className + '\'' +
                ", methodName='" + methodName + '\'' +
                ", descriptor='" + descriptor + '\'' +
                ", kind=" + kind +
                ", index=" + index +
                '}';
    }
}
