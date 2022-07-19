package nz.ac.wgtn.nullannoinference.propagator;

import java.util.Objects;

/**
 * Simple representation of a Java method. DBy design EXCLUDES ownership.
 * @author jens dietrich
 */
class OwnedMethodInfo {
    private String owner = null;
    private String name = null;
    private String descriptor = null;

    public OwnedMethodInfo(String owner, String name, String descriptor) {
        this.owner = owner;
        this.name = name;
        this.descriptor = descriptor;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
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
        OwnedMethodInfo that = (OwnedMethodInfo) o;
        return Objects.equals(owner, that.owner) && Objects.equals(name, that.name) && Objects.equals(descriptor, that.descriptor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(owner, name, descriptor);
    }
}
