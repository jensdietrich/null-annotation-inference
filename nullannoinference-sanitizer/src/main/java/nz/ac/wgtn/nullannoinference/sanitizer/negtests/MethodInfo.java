package nz.ac.wgtn.nullannoinference.sanitizer.negtests;

import java.util.Objects;

/**
 * Simple representation of a Java method.
 * @author jens diietrich
 */
public class MethodInfo implements Comparable<MethodInfo> {
    private String name = null;
    private String className = null;
    private String descriptor = null;

    public MethodInfo(String className, String name, String descriptor) {
        this.name = name;
        this.className = className;
        this.descriptor = descriptor;
    }

    @Override
    public int compareTo(MethodInfo o) {
        return this.toString().compareTo(o.toString());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getDescriptor() {
        return descriptor;
    }

    public void setDescriptor(String descriptor) {
        this.descriptor = descriptor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MethodInfo method = (MethodInfo) o;
        return Objects.equals(name, method.name) && Objects.equals(className, method.className) && Objects.equals(descriptor, method.descriptor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, className, descriptor);
    }

    @Override
    public String toString() {
        return className + "::" + name + descriptor;
    }
}
