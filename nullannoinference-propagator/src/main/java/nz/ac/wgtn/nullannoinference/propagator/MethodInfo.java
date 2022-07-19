package nz.ac.wgtn.nullannoinference.propagator;

import java.util.Objects;

/**
 * Simple representation of a Java method. By design it EXCLUDES ownership.
 * @see OwnedMethodInfo
 * @author jens diietrich
 */
class MethodInfo implements Comparable<MethodInfo> {
    private String name = null;
    private String descriptor = null;

    public MethodInfo(String name, String descriptor) {
        this.name = name;
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
        return Objects.equals(name, method.name) && Objects.equals(descriptor, method.descriptor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, descriptor);
    }

    @Override
    public String toString() {
        return name + descriptor;
    }
}
